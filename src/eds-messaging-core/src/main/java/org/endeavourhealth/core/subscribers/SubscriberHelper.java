package org.endeavourhealth.core.subscribers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.ExpiringCache;
import org.endeavourhealth.common.utility.ExpiringObject;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ServiceSubscriberAuditDalI;
import org.endeavourhealth.core.database.dal.usermanager.caching.ProjectCache;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.ProjectEntity;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SubscriberHelper {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriberHelper.class);

    private static Map<String, String> cachedEndpoints = new ConcurrentHashMap<>();

    private static Map<UUID, List<String>> cachedLatestSubscriberState = new HashMap<>();
    private static ExpiringObject<Boolean> cachedUseDsm = new ExpiringObject<>(60 * 1000);

    private static Map<String, UUID> odsCodeToServiceIdCache = new ExpiringCache<>(1000 * 60 * 5);

    public static List<String> getSubscriberConfigNamesForPublisher(UUID exchangeId, UUID serviceId, String odsCode) throws Exception {

        List<String> ret = getSubscriberConfiNamesImpl(serviceId, odsCode);

        //audit if this state has changed (only if we have an exchange ID, meaning we're being called from proper pipeline)
        if (exchangeId != null) {
            auditSubscriberStateChange(exchangeId, serviceId, ret);
        }

        return ret;
    }

    /**
     * if the subscribers have changed from what we last audited, then update the audit
     */
    private static void auditSubscriberStateChange(UUID exchangeId, UUID serviceId, List<String> subscribers) throws Exception {
        ServiceSubscriberAuditDalI dal = DalProvider.factoryServiceSubscriberAuditDal();

        List<String> latest = cachedLatestSubscriberState.get(serviceId);
        if (latest == null) {
            latest = dal.getLatestSubscribers(serviceId);
        }

        if (latest == null) {
            latest = new ArrayList<>();
        }

        if (!latest.equals(subscribers)) {
            dal.saveSubscribers(serviceId, subscribers);

            //send Slack message so we know something has changed
            ServiceDalI serviceDalI = DalProvider.factoryServiceDal();
            Service service = serviceDalI.getById(serviceId);
            String msg = "Subscriber state for " + service.getName() + " " + service.getLocalId() + " has changed on exchange " + exchangeId + ":\r\n"
                    + "[" + String.join(", ", latest) + "] -> [" + String.join(", ", subscribers) + "]";
            SlackHelper.sendSlackMessage(SlackHelper.Channel.QueueReaderAlerts, msg);
        }

        cachedLatestSubscriberState.put(serviceId, new ArrayList<>(subscribers));
    }

    private static List<String> getSubscriberConfiNamesImpl(UUID serviceId, String odsCode) throws Exception {

        if (useDsmForDSAs()) {
            return getSubscriberConfigNamesFromDsm(serviceId, odsCode);

        } else {
            return getSubscriberConfigNamesFromOldProtocols(serviceId, odsCode);
        }
    }

    /**
     * returns a sorted list of subscriber config names for a given publisher UUID using DSM as the source of information
     */
    public static List<String> getSubscriberConfigNamesFromDsm(UUID serviceId, String odsCode) throws Exception {

        //populate a set, so we can't end up with duplicates
        Set<String> ret = new HashSet<>();

        List<ProjectEntity> distributionProjects = ProjectCache.getValidDistributionProjectsForPublisher(odsCode);
        if (distributionProjects != null) {

            for (ProjectEntity distributionProject : distributionProjects) {
                String configName = distributionProject.getConfigName();
                if (!Strings.isNullOrEmpty(configName)) {
                    ret.add(configName);
                }
            }
        }

        List<String> list = new ArrayList<>(ret);
        list.sort(((o1, o2) -> o1.compareToIgnoreCase(o2))); //for consistency
        return list;
    }

    /**
     * returns a sorted list of subscriber config names for a given publisher UUID using DDS-UI protocols
     */
    public static List<String> getSubscriberConfigNamesFromOldProtocols(UUID serviceId, String odsCode) throws Exception {

        List<LibraryItem> protocols = getProtocolsForPublisherServiceOldWay(serviceId);

        //populate a set, so we can't end up with duplicates
        Set<String> ret = new HashSet<>();

        for (LibraryItem libraryItem: protocols) {
            Protocol protocol = libraryItem.getProtocol();

            //skip disabled protocols
            if (protocol.getEnabled() != ProtocolEnabled.TRUE) {
                continue;
            }

            //get only active subscriber service contracts
            List<ServiceContract> subscribers = protocol
                    .getServiceContract()
                    .stream()
                    .filter(sc -> sc.getType().equals(ServiceContractType.SUBSCRIBER))
                    .filter(sc -> sc.getActive() == ServiceContractActive.TRUE) //skip disabled service contracts
                    .collect(Collectors.toList());

            for (ServiceContract serviceContract: subscribers) {
                String subscriberConfigName = getSubscriberEndpoint(serviceContract);
                if (!Strings.isNullOrEmpty(subscriberConfigName)) {
                    ret.add(subscriberConfigName);
                }
            }
        }

        List<String> list = new ArrayList<>(ret);
        list.sort(((o1, o2) -> o1.compareToIgnoreCase(o2))); //for consistency
        return list;
    }


    private static String getSubscriberEndpoint(ServiceContract contract) throws PipelineException {

        try {
            UUID serviceId = UUID.fromString(contract.getService().getUuid());
            UUID technicalInterfaceId = UUID.fromString(contract.getTechnicalInterface().getUuid());

            String cacheKey = serviceId.toString() + ":" + technicalInterfaceId.toString();
            String endpoint = cachedEndpoints.get(cacheKey);
            if (endpoint == null) {

                ServiceDalI serviceRepository = DalProvider.factoryServiceDal();

                org.endeavourhealth.core.database.dal.admin.models.Service service = serviceRepository.getById(serviceId);
                List<ServiceInterfaceEndpoint> serviceEndpoints = service.getEndpointsList();
                for (ServiceInterfaceEndpoint serviceEndpoint : serviceEndpoints) {
                    if (serviceEndpoint.getTechnicalInterfaceUuid().equals(technicalInterfaceId)) {
                        endpoint = serviceEndpoint.getEndpoint();

                        //concurrent map can't store null values, so only add to the cache if non-null
                        if (endpoint != null) {
                            cachedEndpoints.put(cacheKey, endpoint);
                        }
                        break;
                    }
                }
            }

            return endpoint;

        } catch (Exception ex) {
            throw new PipelineException("Failed to get endpoint for contract", ex);
        }
    }

    /**
     * finds all old-style protocols that the given service is a publisher to
     */
    public static List<LibraryItem> getProtocolsForPublisherServiceOldWay(UUID serviceUuid) throws PipelineException {

        try {
            List<LibraryItem> ret = new ArrayList<>();

            String serviceIdStr = serviceUuid.toString();

            //the above fn will return is all protocols where the service is present, but we want to filter
            //that down to only ones where our service is an active publisher
            List<LibraryItem> libraryItems = LibraryRepositoryHelper.getProtocolsByServiceId(serviceUuid.toString(), null); //passing null means don't filter on system ID

            for (LibraryItem libraryItem: libraryItems) {
                Protocol protocol = libraryItem.getProtocol();
                if (protocol.getEnabled() == ProtocolEnabled.TRUE) { //added missing check

                    for (ServiceContract serviceContract : protocol.getServiceContract()) {
                        if (serviceContract.getType().equals(ServiceContractType.PUBLISHER)
                                && serviceContract.getService().getUuid().equals(serviceIdStr)
                                && serviceContract.getActive() == ServiceContractActive.TRUE) { //added missing check

                            ret.add(libraryItem);
                            break;
                        }
                    }
                }
            }

            return ret;

        } catch (Exception ex) {
            throw new PipelineException("Error getting protocols for service " + serviceUuid, ex);
        }
    }

    private static boolean useDsmForDSAs() throws Exception {
        Boolean b = cachedUseDsm.get();
        if (b == null) {

            JsonNode json = ConfigManager.getConfigurationAsJson("dsm");
            if (json == null
                    || !json.has("useDsmForDSAs")) {
                b = Boolean.FALSE;

            } else {
                boolean useDsm = json.get("useDsmForDSAs").asBoolean();
                b = Boolean.valueOf(useDsm);
            }
            LOG.debug("Refreshed cache to use DSM for DSAs = " + b);
            cachedUseDsm.set(b);
        }

        return b.booleanValue();
    }


    public static Set<UUID> findPublisherServiceIdsForSubscriber(String subscriberOdsCode, String headerProjectId, UUID serviceId, UUID systemId) throws Exception {

        Set<UUID> ret = null;
        if (useDsmForDSAs()) {
            ret = findPublisherServiceIdsForSubscriberNewWay(subscriberOdsCode, headerProjectId);

        } else {
            ret = findPublisherServiceIdsForSubscriberOldWay(serviceId, systemId);
        }

        if (LOG.isTraceEnabled()) {
            List<String> l = ret.stream().map(r -> r.toString()).collect(Collectors.toList());
            l.sort(((o1, o2) -> o1.compareTo(o2)));
            LOG.trace("[" + String.join(", ", l) + "]");
        }

        return ret;
    }

    private static Set<UUID> findPublisherServiceIdsForSubscriberNewWay(String headerOdsCode, String headerProjectId) throws Exception {

        Set<UUID> ret = new HashSet<>();

        ServiceDalI serviceDal = DalProvider.factoryServiceDal();

        List<String> publisherOdsCodes = ProjectCache.getAllPublishersForProjectWithSubscriberCheck(headerProjectId, headerOdsCode);
        LOG.trace("For ODS code [" + headerOdsCode + "] and project ID [" + headerProjectId + "] got " + publisherOdsCodes.size() + " publisher ODS codes");
        LOG.trace("" + publisherOdsCodes);

        for (String publisherOdsCode: publisherOdsCodes) {

            UUID publisherServiceId = odsCodeToServiceIdCache.get(publisherOdsCode);
            if (publisherServiceId == null) {
                org.endeavourhealth.core.database.dal.admin.models.Service publisherService = serviceDal.getByLocalIdentifier(publisherOdsCode);
                if (publisherService == null) {
                    //if the DSM is aware of a publisher that DDS isn't, then this is odd but possible
                    LOG.warn("Failed to find publisher service for publisher ODS code " + publisherOdsCode);
                    continue;
                }

                publisherServiceId = publisherService.getId();
                odsCodeToServiceIdCache.put(publisherOdsCode, publisherServiceId);
            }
            ret.add(publisherServiceId);
        }

        return ret;
    }

    private static Set<UUID> findPublisherServiceIdsForSubscriberOldWay(UUID serviceId, UUID systemId) throws Exception {

        //find protocol
        List<Protocol> protocols = getProtocolsForSubscriberService(serviceId.toString(), systemId.toString());
        if (protocols.isEmpty()) {
            throw new Exception("No valid subscriber agreement found for requesting ODS code and system");
        }

        //the below only works properly if there's a single protocol. To support multiple protocols,
        //it'll need to calculate the frailty against EACH subscriber DB and then return the one with the highest risk
        if (protocols.size() > 1) {
            throw new Exception("No support for multiple subscriber protocols in frailty calculation");
        }

        Protocol protocol = protocols.get(0);

        Set<UUID> ret = new HashSet<>();

        for (ServiceContract serviceContract : protocol.getServiceContract()) {
            if (serviceContract.getType().equals(ServiceContractType.PUBLISHER)
                    && serviceContract.getActive() == ServiceContractActive.TRUE) {

                org.endeavourhealth.core.xml.QueryDocument.Service service = serviceContract.getService();
                ret.add(UUID.fromString(service.getUuid()));
            }
        }

        return ret;
    }


    private static List<Protocol> getProtocolsForSubscriberService(String serviceUuid, String systemUuid) throws PipelineException {

        try {
            List<Protocol> ret = new ArrayList<>();

            List<LibraryItem> libraryItems = LibraryRepositoryHelper.getProtocolsByServiceId(serviceUuid, systemUuid);

            //the above fn will return is all protocols where the service and system are present, but we want to filter
            //that down to only ones where our service and system are an active publisher
            for (LibraryItem libraryItem: libraryItems) {
                Protocol protocol = libraryItem.getProtocol();
                if (protocol.getEnabled() == ProtocolEnabled.TRUE) { //added missing check

                    for (ServiceContract serviceContract : protocol.getServiceContract()) {
                        if (serviceContract.getType().equals(ServiceContractType.SUBSCRIBER)
                                && serviceContract.getService().getUuid().equals(serviceUuid)
                                && serviceContract.getSystem().getUuid().equals(systemUuid)
                                && serviceContract.getActive() == ServiceContractActive.TRUE) { //added missing check

                            ret.add(protocol);
                            break;
                        }
                    }
                }
            }

            return ret;

        } catch (Exception ex) {
            throw new PipelineException("Error getting protocols for service " + serviceUuid, ex);
        }
    }

}
