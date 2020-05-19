package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import org.endeavourhealth.common.fhir.schema.OrganisationType;
import org.endeavourhealth.common.ods.OdsOrganisation;
import org.endeavourhealth.common.ods.OdsWebService;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.common.utility.ExpiringObject;
import org.endeavourhealth.common.utility.XmlSerializer;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryDalI;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.ActiveItem;
import org.endeavourhealth.core.database.dal.admin.models.Item;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.*;
import org.endeavourhealth.core.database.dal.usermanager.caching.DataSharingAgreementCache;
import org.endeavourhealth.core.database.dal.usermanager.caching.OrganisationCache;
import org.endeavourhealth.core.database.dal.usermanager.caching.ProjectCache;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.DataSharingAgreementEntity;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.ProjectEntity;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.endeavourhealth.core.xml.QueryDocument.System;
import org.endeavourhealth.core.xml.QueryDocumentSerializer;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.transform.common.MessageFormat;
import org.endeavourhealth.ui.json.JsonOrganisationType;
import org.endeavourhealth.ui.json.JsonService;
import org.endeavourhealth.ui.json.JsonServiceSystemStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@Path("/service")
public final class ServiceEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceEndpoint.class);

    private static final ServiceDalI serviceRepository = DalProvider.factoryServiceDal();
    private static final UserAuditDalI userAuditRepository = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Service);
    private static final ExchangeDalI exchangeAuditRepository = DalProvider.factoryExchangeDal();

    private static ExpiringObject<List<String>> cachedTagNames = ExpiringObject.factoryFiveMinutes();
    private static ExpiringObject<List<String>> cachedPublisherNames = ExpiringObject.factoryFiveMinutes();
    private static ExpiringObject<List<String>> cachedCcgCodes = ExpiringObject.factoryFiveMinutes();
    private static ReentrantLock cacheLock = new ReentrantLock();

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "ServiceEndpoint.Post")
    @RequiresAdmin
    public Response post(@Context SecurityContext sc, JsonService service) throws Exception {
        super.setLogbackMarkers(sc);
        userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Service",
                "Service", service);

        //if no postcode etc. are set, see if we can find them via Open ODS
        //populateFromOds(service); //done on the front end now

        boolean refreshCaches = false;

        Service dbService = new Service();
        dbService.setId(service.getUuid());
        dbService.setName(service.getName());
        dbService.setLocalId(service.getLocalIdentifier());

        if (service.getPublisherConfigName() != null) {
            String publisherConfigName = service.getPublisherConfigName();
            dbService.setPublisherConfigName(publisherConfigName);

            //make sure config name is in the cache otherwise we'll need to refresh it
            List<String> configNames = cachedPublisherNames.get();
            if (configNames != null
                    && !configNames.contains(publisherConfigName)) {
                refreshCaches = true;
            }
        }

        dbService.setPostcode(service.getPostcode());

        if (service.getCcgCode() != null) {
            String ccgCode = service.getCcgCode();
            dbService.setCcgCode(ccgCode);

            //make sure config name is in the cache otherwise we'll need to refresh it
            List<String> ccgCodes = cachedCcgCodes.get();
            if (ccgCodes != null
                    && !ccgCodes.contains(ccgCode)) {
                refreshCaches = true;
            }
        }


        if (!Strings.isNullOrEmpty(service.getOrganisationTypeCode())) {
            dbService.setOrganisationType(OrganisationType.fromCode(service.getOrganisationTypeCode()));
        }
        dbService.setEndpointsList(service.getEndpoints());
        dbService.setAlias(service.getAlias());
        if (service.getTags() != null) {
            dbService.setTags(new HashMap<>(service.getTags()));

            //make sure all tags are in the cache otherwise we'll need to refresh it
            List<String> tagNames = cachedTagNames.get();
            if (tagNames != null) {
                for (String tagName: service.getTags().keySet()) {
                    if (!tagNames.contains(tagName)) {
                        refreshCaches = true;
                    }
                }
            }
        }

        //save
        UUID serviceId = serviceRepository.save(dbService);

        if (service.getUuid() == null) {
            service.setUuid(serviceId);
        }

        //if we've added a new tag or publisher, call this after the save
        if (refreshCaches) {
            LOG.debug("New tag, CCG code or publisher config, so will refresh caches");
            refreshServiceCaches(true);
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(service)
                .build();
    }


    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "ServiceEndpoint.validateService")
    @Path("/validateService")
    @RequiresAdmin
    public Response validateService(@Context SecurityContext sc, JsonService serviceToSave) throws Exception {
        super.setLogbackMarkers(sc);
        userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Validate",
                "Service", serviceToSave);

        //validate that the service can be saved OK
        String error = null;

        //ensure local ID isn't already in use
        String localId = serviceToSave.getLocalIdentifier();
        if (Strings.isNullOrEmpty(localId)) {
            error = "ODS code must be set";
        }

        ServiceDalI dal = DalProvider.factoryServiceDal();

        if (error == null) {
            Service serviceForOds = dal.getByLocalIdentifier(localId);
            if (serviceForOds != null
                    && (serviceToSave.getUuid() == null
                        || !serviceToSave.getUuid().equals(serviceForOds.getId()))) {
                error = "ODS code " + localId + " already in use";
            }
        }

        //ensure ODS code isn't changed
        /*if (error == null) {
            UUID existingId = serviceToSave.getUuid();
            if (existingId != null) {
                Service existingService = dal.getById(existingId);
                String existingLocalId = existingService.getLocalId();
                if (!Strings.isNullOrEmpty(existingLocalId)
                        && !existingLocalId.equals(localId)) {
                    error = "ODS code cannot be changed once set";
                }
            }
        }*/

        //if a publisher, ensure publisher config is set
        if (error == null) {
            String publisherConfig = serviceToSave.getPublisherConfigName();
            if (Strings.isNullOrEmpty(publisherConfig)) {

                for (ServiceInterfaceEndpoint endpoint : serviceToSave.getEndpoints()) {
                    String endpointStr = endpoint.getEndpoint();
                    if (endpointStr != null
                            && (endpointStr.equals(ServiceInterfaceEndpoint.STATUS_BULK_PROCESSING)
                            || endpointStr.equals(ServiceInterfaceEndpoint.STATUS_NORMAL)
                            || endpointStr.equals(ServiceInterfaceEndpoint.STATUS_AUTO_FAIL))) {

                        error = "Publisher config must be set if a publisher";
                        break;
                    }
                }
            }
        }

        UUID existingServiceId = serviceToSave.getUuid();
        Service existingService = null;
        if (existingServiceId != null) {
            existingService = dal.getById(existingServiceId);
        }

        //ensure don't change publisher state to normal or bulk when there's stuff in the queue already
        if (error == null
                && existingService != null) {

            for (ServiceInterfaceEndpoint endpoint : serviceToSave.getEndpoints()) {
                String endpointStr = endpoint.getEndpoint();
                UUID systemUuid = endpoint.getSystemUuid();

                if (endpointStr != null
                        && (endpointStr.equals(ServiceInterfaceEndpoint.STATUS_BULK_PROCESSING)
                        || endpointStr.equals(ServiceInterfaceEndpoint.STATUS_NORMAL))) {

                    //check the existing instance to see if changed
                    String existingEndpointStr = null;
                    for (ServiceInterfaceEndpoint existingEndpoint : existingService.getEndpointsList()) {
                        if (existingEndpoint.getSystemUuid().equals(systemUuid)) {
                            existingEndpointStr = existingEndpoint.getEndpoint();
                            break;
                        }
                    }

                    //if the publisher mode has changed, then validate that there's nothing in the queue
                    if (!endpointStr.equals(existingEndpointStr)) {

                        boolean inQueue = isAnythingInInboundQueue(existingServiceId, systemUuid);
                        if (inQueue) {
                            error = "Cannot change publisher mode while inbound messages are queued";
                            break;
                        }
                    }
                }
            }
        }

        //ensure can't change publisher config name if any data exists in the current DB
        if (error == null
                && existingService != null) {

            String originalPublisher = existingService.getPublisherConfigName();
            String currentPublisher = serviceToSave.getPublisherConfigName();
            if (!originalPublisher.equals(currentPublisher)) {

                //DDS-UI can't access EHR databases, so use the same validation fn as when trying to delete a service
                error = canDeleteService(existingService);

                /*ResourceDalI resourceDal = DalProvider.factoryResourceDal();
                boolean dataExists = resourceDal.dataExists(existingServiceId);
                if (dataExists) {
                    error = "Cannot change publisher config while data exists in database";
                }*/
            }
        }

        clearLogbackMarkers();

        if (error == null) {
            return Response
                    .ok()
                    .build();

        } else {
            return Response
                    .ok()
                    .entity(error)
                    .build();

        }
    }

    /**
     * works out if there's anything in the inbound queue for the given service
     * Note that this doesn't actually test RabbitMQ but looks at the transform audit of the most
     * recent exchange to infer whether it is still in the queue or not
     */
    public static boolean isAnythingInInboundQueue(UUID serviceId, UUID systemId) throws Exception {
        ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
        List<Exchange> mostRecentExchanges = exchangeDal.getExchangesByService(serviceId, systemId, 1);
        if (mostRecentExchanges.isEmpty()) {
            return false;
        }

        Exchange mostRecentExchange = mostRecentExchanges.get(0);

        //if the most recent exchange is flagged for not queueing, then we need to go back to the last one not flagged like that
        Boolean allowQueueing = mostRecentExchange.getHeaderAsBoolean(HeaderKeys.AllowQueueing);
        if (allowQueueing != null
                && !allowQueueing.booleanValue()) {

            mostRecentExchange = null;

            mostRecentExchanges = exchangeDal.getExchangesByService(serviceId, systemId, 100);
            for (Exchange exchange: mostRecentExchanges) {

                allowQueueing = exchange.getHeaderAsBoolean(HeaderKeys.AllowQueueing);
                if (allowQueueing == null
                        || allowQueueing.booleanValue()) {
                    mostRecentExchange = exchange;
                    break;
                }
            }

            //if we still didn't find one, after checking the last 100, then just assume we're OK
            if (mostRecentExchange == null) {
                return false;
            }
        }

        ExchangeTransformAudit latestTransform = exchangeDal.getLatestExchangeTransformAudit(serviceId, systemId, mostRecentExchange.getId());

        //if the exchange has never been transformed or the transform hasn't ended, we
        //can infer that it's in the queue
        if (latestTransform == null
                || latestTransform.getEnded() == null) {
            LOG.debug("Exchange " + mostRecentExchange.getId() + " has never been transformed or hasn't finished yet");
            return true;

        } else {
            Date transformFinished = latestTransform.getEnded();
            List<ExchangeEvent> events = exchangeDal.getExchangeEvents(mostRecentExchange.getId());
            if (!events.isEmpty()) {
                ExchangeEvent mostRecentEvent = events.get(events.size() - 1);
                String eventDesc = mostRecentEvent.getEventDesc();
                Date eventDate = mostRecentEvent.getTimestamp();

                if (eventDesc.startsWith("Manually pushed into")
                        && eventDate.after(transformFinished)) {

                    LOG.debug("Exchange " + mostRecentExchange.getId() + " latest event is being inserted into queue");
                    return true;
                }
            }
        }

        return false;
    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "ServiceEndpoint.DeleteService")
    @RequiresAdmin
    public Response deleteService(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Service",
                "Service Id", uuid);

        UUID serviceUuid = UUID.fromString(uuid);
        Service service = serviceRepository.getById(serviceUuid);

        String err = null;

        //validate that there's no data in the EHR repo before allowing a delete
        if (!Strings.isNullOrEmpty(service.getPublisherConfigName())) {
            //DDS-UI is no longer allowed to connect to the FHIR databases, so perform a similar check via
            //the audit DB which we can access
            err = canDeleteService(service);

            /*ResourceDalI resourceRepository = DalProvider.factoryResourceDal();
            if (resourceRepository.dataExists(serviceUuid)) {
                throw new BadRequestException("Cannot delete service without deleting data first");
            }*/
        }

        if (Strings.isNullOrEmpty(err)) {
            //if no error, let the service be deleted
            serviceRepository.delete(service);

            clearLogbackMarkers();
            return Response
                    .ok()
                    .build();

        } else {

            clearLogbackMarkers();
            return Response
                    .ok(err)
                    .build();
        }
    }

    /**
     * checks the audit trail for a service to make sure there's no data remaining
     * returns a String error message or NULL if OK to delete
     */
    private String canDeleteService(Service service) throws Exception {

        ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();

        //if no systems, then nothing to worry about
        List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
        if (systemIds.isEmpty()) {
            return null;
        }

        boolean deleteProcessedOk = false;
        boolean receivedExchanges = false;

        for (UUID systemId: systemIds) {

            List<Exchange> l = exchangeDal.getExchangesByService(service.getId(), systemId, 1);
            if (l.isEmpty()) {
                //if no exchanges, then nothing was received
                continue;
            }

            receivedExchanges = true;
            Exchange lastExchange = l.get(0);

            //if the last exchange WASN'T one for a delete, then that's wrong
            String lastExchangeSystem = lastExchange.getHeader(HeaderKeys.SourceSystem);
            if (!lastExchangeSystem.equals(MessageFormat.DUMMY_SENDER_SOFTWARE_FOR_BULK_DELETE)) {
                return "Must delete data first";
            }

            //if the last exchange WAS a mass delete, then make sure it's finished first
            List<ExchangeTransformAudit> transformAudits = exchangeDal.getAllExchangeTransformAudits(service.getId(), systemId, lastExchange.getId());
            for (ExchangeTransformAudit audit: transformAudits) {
                if (audit.getEnded() != null
                        && audit.getErrorXml() == null) {
                    deleteProcessedOk = true;
                }
            }
        }

        //we create a "delete" exchange for each system, but only send one of them through the queues
        //so check this OUTSIDE the loop on system IDs
        if (receivedExchanges
            && !deleteProcessedOk) {
            return "Delete not finished yet";
        }

        //if we make it here, we're good
        return null;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "ServiceEndpoint.DeleteServiceData")
    @Path("/data")
    @RequiresAdmin
    public Response deleteServiceData(@Context SecurityContext sc,
                                      @QueryParam("serviceId") String serviceIdStr) throws Exception {
        super.setLogbackMarkers(sc);
        userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Service Data",
                "Service Id", serviceIdStr);

        UUID serviceUuid = UUID.fromString(serviceIdStr);

        QueueHelper.queueUpFullServiceForDelete(serviceUuid);

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "ServiceEndpoint.GetServiceList")
    public Response get(@Context SecurityContext sc,
                        @QueryParam("uuid") String uuid,
                        @QueryParam("odsCode") String odsCode,
                        @QueryParam("searchText") String searchText) throws Exception {
        super.setLogbackMarkers(sc);
        userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Service(s)",
                "Service Id", uuid,
                "Search Data", searchText);

        Response response = null;

        if (Strings.isNullOrEmpty(uuid)
                && Strings.isNullOrEmpty(odsCode)
                && Strings.isNullOrEmpty(searchText)) {
            LOG.trace("Get Service list");
            response = getServiceList();

        } else if (!Strings.isNullOrEmpty(uuid)) {
            LOG.trace("Get Service single for UUID " + uuid);
            response = getSingleServiceForUuid(uuid);

        } else if (!Strings.isNullOrEmpty(odsCode)) {
            LOG.trace("Get Service single for ODS " + odsCode);
            response = getSingleServiceForOds(odsCode);

        } else {
            LOG.trace("Search services [" + searchText + "]");
            response = getServicesMatchingText(searchText);
        }

        LOG.trace("Returning service list");
        return response;
    }

    private Response getServiceList() throws Exception {
        List<Service> services = serviceRepository.getAll();
        List<JsonService> ret = createAndPopulateJsonServices(services);


        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getSingleServiceForUuid(String uuid) throws Exception {
        UUID serviceUuid = UUID.fromString(uuid);
        Service service = serviceRepository.getById(serviceUuid);

        List<Service> services = new ArrayList<>();
        services.add(service);

        List<JsonService> jsonServices = createAndPopulateJsonServices(services);
        JsonService ret = jsonServices.get(0);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getSingleServiceForOds(String odsCode) throws Exception {

        Service service = serviceRepository.getByLocalIdentifier(odsCode);

        List<Service> services = new ArrayList<>();
        services.add(service);

        List<JsonService> jsonServices = createAndPopulateJsonServices(services);
        JsonService ret = jsonServices.get(0);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getServicesMatchingText(String searchData) throws Exception {
        List<Service> services = serviceRepository.search(searchData);
        List<JsonService> ret = createAndPopulateJsonServices(services);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    /**
     * converts internal Service objects into ones suitable for DDS-UI, which includes additional info about errors etc.
     */
    public static List<JsonService> createAndPopulateJsonServices(List<Service> services) throws Exception {

        //get the extra information we want
        Map<UUID, Set<UUID>> hmServicesInError = findServicesInError(services);
        Map<UUID, Map<UUID, LastDataReceived>> hmLastDataReceived = findLastDataReceived(services);
        Map<UUID, Map<UUID, LastDataProcessed>> hmLastDataProcessed = findLastDataProcessed(services);

        List<JsonService> ret = new ArrayList<>();

        for (Service service : services) {

            //work out where we are with processing data
            List<JsonServiceSystemStatus> jsonSystemStatuses = createAndPopulateJsonSystemStatuses(service, hmServicesInError, hmLastDataReceived, hmLastDataProcessed);

            JsonService jsonService = new JsonService(service, jsonSystemStatuses);
            ret.add(jsonService);
        }

        return ret;
    }

    private static List<JsonServiceSystemStatus> createAndPopulateJsonSystemStatuses(Service service,
                                                                              Map<UUID, Set<UUID>> hmServicesInError,
                                                                              Map<UUID, Map<UUID, LastDataReceived>> hmLastDataReceived,
                                                                              Map<UUID, Map<UUID, LastDataProcessed>> hmLastDataProcessed) throws Exception {

        UUID serviceId = service.getId();
        Set<UUID> hsInError = hmServicesInError.get(serviceId);
        Map<UUID, LastDataReceived> hmReceived = hmLastDataReceived.get(serviceId);
        Map<UUID, LastDataProcessed> hmProcessed = hmLastDataProcessed.get(serviceId);

        Set<UUID> systemIds = new HashSet<>();
        if (hsInError != null) {
            systemIds.addAll(hsInError);
        }
        if (hmReceived != null) {
            systemIds.addAll(hmReceived.keySet());
        }

        //create a map of the publisher mode for each system
        Map<UUID, String> hmPublisherInterfaceModes = new HashMap<>();
        for (ServiceInterfaceEndpoint interfaceEndpoint: service.getEndpointsList()) {
            UUID systemId = interfaceEndpoint.getSystemUuid();
            String mode = interfaceEndpoint.getEndpoint();

            //if this interface is a publisher-type one, then ensure it's in the list of system IDs and add the mode to the map
            if (mode.equals(ServiceInterfaceEndpoint.STATUS_BULK_PROCESSING)
                    || mode.equals(ServiceInterfaceEndpoint.STATUS_AUTO_FAIL)
                    || mode.equals(ServiceInterfaceEndpoint.STATUS_DRAFT)
                    || mode.equals(ServiceInterfaceEndpoint.STATUS_NORMAL)) {

                hmPublisherInterfaceModes.put(systemId, mode);
                systemIds.add(systemId);
            }
        }


        if (systemIds.isEmpty()) {
            return null;
        }


        List<JsonServiceSystemStatus> ret = new ArrayList<>();

        for (UUID systemId: systemIds) {

            JsonServiceSystemStatus status = new JsonServiceSystemStatus();

            String desc = findSoftwareDescForSystem(systemId);
            status.setSystemName(desc);

            boolean inError = hsInError != null && hsInError.contains(systemId);
            status.setProcessingInError(inError);

            if (hmReceived != null) {
                LastDataReceived lastReceived = hmReceived.get(systemId);

                if (lastReceived != null) {
                    Date dtLastData = lastReceived.getDataDate();
                    UUID exchangeIdLastReceived = lastReceived.getExchangeId();
                    Date dtLastDataReceived = lastReceived.getReceivedDate();

                    status.setLastDataDate(dtLastData);
                    status.setLastDataReceived(dtLastDataReceived);

                    if (hmProcessed != null) {
                        LastDataProcessed processedObj = hmProcessed.get(systemId);
                        if (processedObj != null) {

                            Date lastDataProcessedDate = processedObj.getProcessedDate();
                            status.setLastDateSuccessfullyProcessed(lastDataProcessedDate);

                            Date lastDataDateProcessed = processedObj.getDataDate();
                            status.setLastDataDateSuccessfullyProcessed(lastDataDateProcessed);

                            UUID lastExchangeProcessed = processedObj.getExchangeId();
                            if (lastExchangeProcessed.equals(exchangeIdLastReceived)) {

                                status.setProcessingUpToDate(true);
                            }
                        }
                    }
                }
            }

            String publisherMode = hmPublisherInterfaceModes.get(systemId);
            status.setPublisherMode(publisherMode);

            ret.add(status);
        }

        return ret;
    }

    private static Map<UUID, Map<UUID, LastDataProcessed>> findLastDataProcessed(List<Service> services) throws Exception {

        if (services.isEmpty()) {
            return new HashMap<>();
        }

        //if we've less than X services, hit the DB one at a time, rather than loading the full error list
        List<LastDataProcessed> list = null;
        if (services.size() < 5) {

            list = new ArrayList<>();

            for (Service service : services) {
                UUID serviceId = service.getId();
                list.addAll(exchangeAuditRepository.getLastDataProcessed(serviceId));
            }

        } else {
            //if we're lots of services, it's easier to load all the error states
            list = exchangeAuditRepository.getLastDataProcessed();
        }

        Map<UUID, Map<UUID, LastDataProcessed>> ret = new HashMap<>();

        for (LastDataProcessed obj : list) {
            UUID serviceId = obj.getServiceId();

            Map<UUID, LastDataProcessed> l = ret.get(serviceId);
            if (l == null) {
                l = new HashMap<>();
                ret.put(serviceId, l);
            }

            UUID systemId = obj.getSystemId();
            l.put(systemId, obj);
        }

        return ret;
    }

    private static Map<UUID, Map<UUID, LastDataReceived>> findLastDataReceived(List<Service> services) throws Exception {

        if (services.isEmpty()) {
            return new HashMap<>();
        }

        //if we've less than X services, hit the DB one at a time, rather than loading the full error list
        List<LastDataReceived> list = null;
        if (services.size() < 5) {

            list = new ArrayList<>();

            for (Service service : services) {
                UUID serviceId = service.getId();
                list.addAll(exchangeAuditRepository.getLastDataReceived(serviceId));
            }

        } else {
            //if we're lots of services, it's easier to load all the error states
            list = exchangeAuditRepository.getLastDataReceived();
        }

        Map<UUID, Map<UUID, LastDataReceived>> ret = new HashMap<>();

        for (LastDataReceived obj : list) {
            UUID serviceId = obj.getServiceId();

            Map<UUID, LastDataReceived> l = ret.get(serviceId);
            if (l == null) {
                l = new HashMap<>();
                ret.put(serviceId, l);
            }

            UUID systemId = obj.getSystemId();
            l.put(systemId, obj);
        }

        return ret;
    }


    private static String findSoftwareDescForSystem(UUID systemId) throws Exception {

        LibraryItem libraryItem = LibraryRepositoryHelper.getLibraryItemUsingCache(systemId);
        if (libraryItem == null) {
            return "MISSING SYSTEM";
        } else {
            return libraryItem.getName();
        }
    }

    private static Map<UUID, Set<UUID>> findServicesInError(List<Service> services) throws Exception {

        Map<UUID, Set<UUID>> ret = new HashMap<>();

        if (services.isEmpty()) {
            return ret;
        }

        //if we've less than X services, hit the DB one at a time, rather than loading the full error list
        List<ExchangeTransformErrorState> errorStates = new ArrayList<>();
        if (services.size() < 5) {

            for (Service service : services) {
                UUID serviceId = service.getId();
                List<UUID> systemIds = findSystemIdsFromService(service);
                for (UUID systemId : systemIds) {
                    ExchangeTransformErrorState errorState = exchangeAuditRepository.getErrorState(serviceId, systemId);
                    if (errorState != null) {
                        errorStates.add(errorState);
                    }
                }
            }

        } else {
            //if we're lots of services, it's easier to load all the error states
            errorStates = exchangeAuditRepository.getAllErrorStates();
        }

        for (ExchangeTransformErrorState errorState : errorStates) {
            UUID serviceId = errorState.getServiceId();
            UUID systemId = errorState.getSystemId();

            Set<UUID> systemIdsInErr = ret.get(serviceId);
            if (systemIdsInErr == null) {
                systemIdsInErr = new HashSet<>();
                ret.put(serviceId, systemIdsInErr);
            }
            systemIdsInErr.add(systemId);
        }

        return ret;
    }

    private static List<UUID> findSystemIdsFromService(Service service) throws Exception {

        List<UUID> ret = new ArrayList<>();

        List<ServiceInterfaceEndpoint> endpoints = null;
        try {
            endpoints = service.getEndpointsList();
            for (ServiceInterfaceEndpoint endpoint : endpoints) {
                UUID endpointSystemId = endpoint.getSystemUuid();
                ret.add(endpointSystemId);
            }
        } catch (Exception e) {
            throw new Exception("Failed to process endpoints from service " + service.getId());
        }

        return ret;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "ServiceEndpoint.systemsForService")
    @Path("/systemsForService")
    public Response getSystemsForService(@Context SecurityContext sc, @QueryParam("serviceId") String serviceIdStr) throws Exception {
        super.setLogbackMarkers(sc);
        userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Service Systems",
                "ServiceId", serviceIdStr);

        UUID serviceId = UUID.fromString(serviceIdStr);

        ServiceDalI serviceDal = DalProvider.factoryServiceDal();
        Service service = serviceDal.getById(serviceId);

        LibraryDalI libraryRepository = DalProvider.factoryLibraryDal();

        List<System> ret = new ArrayList<>();

        List<ServiceInterfaceEndpoint> endpoints = service.getEndpointsList();
        for (ServiceInterfaceEndpoint endpoint : endpoints) {

            UUID endpointSystemId = endpoint.getSystemUuid();

            ActiveItem activeItem = libraryRepository.getActiveItemByItemId(endpointSystemId);
            Item item = libraryRepository.getItemByKey(endpointSystemId, activeItem.getAuditId());
            LibraryItem libraryItem = QueryDocumentSerializer.readLibraryItemFromXml(item.getXmlContent());
            System system = libraryItem.getSystem();
            ret.add(system);
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "ServiceEndpoint.openOdsRecord")
    @Path("/openOdsRecord")
    public Response getOpenOdsRecord(@Context SecurityContext sc, @QueryParam("odsCode") String odsCode) throws Exception {
        super.setLogbackMarkers(sc);

        OdsOrganisation org = OdsWebService.lookupOrganisationViaRest(odsCode);

        clearLogbackMarkers();

        if (org == null) {
            return Response
                    .noContent()
                    .build();
        } else {
            return Response
                    .ok()
                    .entity(org)
                    .build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "ServiceEndpoint.protocolsForService")
    @Path("/protocolsForService")
    public Response getProtocolsForService(@Context SecurityContext sc, @QueryParam("serviceId") String serviceIdStr) throws Exception {
        super.setLogbackMarkers(sc);
        userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Service Protocols",
                "ServiceId", serviceIdStr);

        UUID serviceId = UUID.fromString(serviceIdStr);

        //the protocols can be huge, so only return the relevant bits for our service
        List<LibraryItem> ret = new ArrayList<>();

        List<LibraryItem> libraryItems = LibraryRepositoryHelper.getProtocolsByServiceId(serviceIdStr, null);
        for (LibraryItem rawLibraryItem: libraryItems) {

            //copy the item, so amendments don't mess up what's in the cache
            QueryDocument doc = new QueryDocument();
            doc.getLibraryItem().add(rawLibraryItem);
            String xml = QueryDocumentSerializer.writeToXml(doc);

            LibraryItem libraryItem = (LibraryItem)XmlSerializer.deserializeFromString(LibraryItem.class, xml, (String)null);
            /*doc = QueryDocumentSerializer.readQueryDocumentFromXml(xml);
            LibraryItem libraryItem = (LibraryItem) XmlSerializer.deserializeFromString(LibraryItem.class, xml, (String)null);*/

            Protocol protocol = libraryItem.getProtocol();
            List<ServiceContract> serviceContracts = protocol.getServiceContract();
            for (int i=serviceContracts.size()-1; i>=0; i--) {
                ServiceContract serviceContract = serviceContracts.get(i);
                String serviceContractServiceId = serviceContract.getService().getUuid();
                if (!serviceContractServiceId.equals(serviceIdStr)) {
                    serviceContracts.remove(i);
                }
            }

            ret.add(libraryItem);
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "ServiceEndpoint.organisationTypeList")
    @Path("/organisationTypeList")
    public Response getOrganisationTypeList(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        List<JsonOrganisationType> ret = new ArrayList<>();

        for (OrganisationType t: OrganisationType.values()) {
            ret.add(new JsonOrganisationType(t));
        }

        //sort by alpha
        ret.sort(((o1, o2) -> o1.getDescription().compareToIgnoreCase(o2.getDescription())));

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "ServiceEndpoint.tagNames")
    @Path("/tagNames")
    public Response getTagNames(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        List<String> ret = cachedTagNames.get();
        if (ret == null) {
            LOG.debug("Tag name cache is empty, so will refresh");
            refreshServiceCaches(false);
            ret = cachedTagNames.get();
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }



    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "ServiceEndpoint.publisherConfigNames")
    @Path("/publisherConfigNames")
    public Response getPublisherConfigNames(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        //check cache
        List<String> ret = cachedPublisherNames.get();
        if (ret == null) {
            LOG.debug("Publisher config cache is empty, so will refresh");
            refreshServiceCaches(false);
            ret = cachedPublisherNames.get();
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "ServiceEndpoint.ccgCodes")
    @Path("/ccgCodes")
    public Response getCcgCodes(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        //check cache
        List<String> ret = cachedCcgCodes.get();
        if (ret == null) {
            LOG.debug("CCG code cache is empty, so will refresh");
            refreshServiceCaches(false);
            ret = cachedCcgCodes.get();
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    /**
     * builds/refreshes the caches of CCG codes, tags and publisher config names
     * synchronized to prevent multiple calls from a browser kicking this off repeatedly
     */
    private void refreshServiceCaches(boolean force) throws Exception {

        LOG.trace("refreshServiceCaches, force = " + force);

        //quick check before getting the lock
        if (!force && areCachesValid()) {
            LOG.trace("Not forced and caches OK");
            return;
        }

        try {
            cacheLock.lock();
            LOG.trace("Got lock");

            //second check now we've got the lock
            if (!force && areCachesValid()) {
                LOG.trace("Got lock and not forced and caches OK");
                return;
            }

            Set<String> hsPublisherConfigNames = new HashSet<>();
            Set<String> hsCcgCodes = new HashSet<>();
            Set<String> hsTags = new HashSet<>();

            List<Service> services = serviceRepository.getAll();
            for (Service service : services) {

                String publisherConfigName = service.getPublisherConfigName();
                if (!Strings.isNullOrEmpty(publisherConfigName)) {
                    hsPublisherConfigNames.add(publisherConfigName);
                }

                String ccgCode = service.getCcgCode();
                if (!Strings.isNullOrEmpty(ccgCode)) {
                    hsCcgCodes.add(ccgCode);
                }

                if (service.getTags() != null) {
                    Set<String> set = service.getTags().keySet();
                    hsTags.addAll(set);
                }
            }

            //set in caches
            List<String> l = new ArrayList<>(hsPublisherConfigNames);
            l.sort(((o1, o2) -> o1.toLowerCase().compareToIgnoreCase(o2.toLowerCase())));
            cachedPublisherNames.set(l);

            l = new ArrayList<>(hsCcgCodes);
            l.sort(((o1, o2) -> o1.toLowerCase().compareToIgnoreCase(o2.toLowerCase())));
            cachedCcgCodes.set(l);


            l = new ArrayList<>(hsTags);
            //sort by length so shorter ones are first, which has the end result of moving
            //the more interesting tags to the start
            //l.sort(((o1, o2) -> o1.toLowerCase().compareToIgnoreCase(o2.toLowerCase())));
            l.sort((o1, o2) -> new Integer(o1.length()).compareTo(new Integer(o2.length())));

            //always put NOTES at the end
            if (l.contains("Notes")) {
                l.remove("Notes");
                l.add("Notes");
            }

            cachedTagNames.set(l);

            LOG.trace("Caches rebuilt");

        } finally {
            cacheLock.unlock();
        }
    }

    private static boolean areCachesValid() {

        /*LOG.trace("Are caches valid:");
        LOG.trace("Publisher cache empty = " + (cachedPublisherNames.get() == null));
        LOG.trace("CCG cache empty = " + (cachedCcgCodes.get() == null));
        LOG.trace("Tag cache empty = " + (cachedTagNames.get() == null));*/

        if (cachedPublisherNames.get() == null
                || cachedCcgCodes.get() == null
                || cachedTagNames.get() == null) {
            return false;

        } else {
            return true;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "ServiceEndpoint.dsmDetails")
    @Path("/dsmDetails")
    public Response getDsmDetails(@Context SecurityContext sc, @QueryParam("odsCode") String odsCode) throws Exception {
        super.setLogbackMarkers(sc);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode obj = new ObjectNode(mapper.getNodeFactory());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {

            Boolean b = OrganisationCache.doesOrganisationHaveDPA(odsCode);
            obj.put("hasDPA", b);

            List<DataSharingAgreementEntity> publisherDsasList = DataSharingAgreementCache.getAllDSAsForPublisherOrg(odsCode);
            obj.put("countPublisherDSAs", publisherDsasList.size());

            ArrayNode arr = obj.putArray("publisherDSAs");
            for (DataSharingAgreementEntity dsa: publisherDsasList) {

                ObjectNode dsaNode = arr.addObject();
                dsaNode.put("uuid", dsa.getUuid());
                dsaNode.put("name", dsa.getName());
                dsaNode.put("description", dsa.getDescription());
                dsaNode.put("derivation", dsa.getDerivation());
                dsaNode.put("dsaStatusId", dsa.getDsaStatusId());
                dsaNode.put("consentModelId", dsa.getConsentModelId());
                dsaNode.put("startDate", (dsa.getStartDate() != null ? dateFormat.format(dsa.getStartDate()): null));
                dsaNode.put("endDate", (dsa.getEndDate() != null ? dateFormat.format(dsa.getEndDate()): null));
            }

            List<ProjectEntity> distributionProjects = ProjectCache.getValidDistributionProjectsForPublisher(odsCode);
            obj.put("countDistributionProjects", distributionProjects.size());

            arr = obj.putArray("distributionProjects");
            for (ProjectEntity dsa: distributionProjects) {

                ObjectNode dsaNode = arr.addObject();
                dsaNode.put("uuid", dsa.getUuid());
                dsaNode.put("name", dsa.getName());
                dsaNode.put("configName", dsa.getConfigName());
                dsaNode.put("leadUser", dsa.getLeadUser());
                dsaNode.put("technicalLeadUser", dsa.getTechnicalLeadUser());
                dsaNode.put("consentModelId", dsa.getConsentModelId());
                dsaNode.put("deidentificationLevel", dsa.getDeidentificationLevel());
                dsaNode.put("projectTypeId", dsa.getProjectTypeId());
                dsaNode.put("securityInfrastructureId", dsa.getSecurityInfrastructureId());
                dsaNode.put("ipAddress", dsa.getIpAddress());
                dsaNode.put("summary", dsa.getSummary());
                dsaNode.put("businessCase", dsa.getBusinessCase());
                dsaNode.put("objectives", dsa.getObjectives());
                dsaNode.put("securityArchitectureId", dsa.getSecurityArchitectureId());
                dsaNode.put("storageProtocolId", dsa.getStorageProtocolId());
                dsaNode.put("businessCaseStatus", dsa.getBusinessCaseStatus());
                dsaNode.put("flowScheduleId", dsa.getFlowScheduleId());
                dsaNode.put("projectStatusId", dsa.getProjectStatusId());
                dsaNode.put("startDate", (dsa.getStartDate() != null ? dateFormat.format(dsa.getStartDate()): null));
                dsaNode.put("endDate", (dsa.getEndDate() != null ? dateFormat.format(dsa.getEndDate()): null));
            }

            List<ProjectEntity> subscribersDsasList = ProjectCache.getAllProjectsForSubscriberOrg(odsCode);
            obj.put("countSubscriberDSAs", subscribersDsasList.size());

            arr = obj.putArray("subscriberDSAs");
            for (ProjectEntity dsa: subscribersDsasList) {

                ObjectNode dsaNode = arr.addObject();
                dsaNode.put("uuid", dsa.getUuid());
                dsaNode.put("name", dsa.getName());
                dsaNode.put("configName", dsa.getConfigName());
                dsaNode.put("leadUser", dsa.getLeadUser());
                dsaNode.put("technicalLeadUser", dsa.getTechnicalLeadUser());
                dsaNode.put("consentModelId", dsa.getConsentModelId());
                dsaNode.put("deidentificationLevel", dsa.getDeidentificationLevel());
                dsaNode.put("projectTypeId", dsa.getProjectTypeId());
                dsaNode.put("securityInfrastructureId", dsa.getSecurityInfrastructureId());
                dsaNode.put("ipAddress", dsa.getIpAddress());
                dsaNode.put("summary", dsa.getSummary());
                dsaNode.put("businessCase", dsa.getBusinessCase());
                dsaNode.put("objectives", dsa.getObjectives());
                dsaNode.put("securityArchitectureId", dsa.getSecurityArchitectureId());
                dsaNode.put("storageProtocolId", dsa.getStorageProtocolId());
                dsaNode.put("businessCaseStatus", dsa.getBusinessCaseStatus());
                dsaNode.put("flowScheduleId", dsa.getFlowScheduleId());
                dsaNode.put("projectStatusId", dsa.getProjectStatusId());
                dsaNode.put("startDate", (dsa.getStartDate() != null ? dateFormat.format(dsa.getStartDate()): null));
                dsaNode.put("endDate", (dsa.getEndDate() != null ? dateFormat.format(dsa.getEndDate()): null));


                ArrayNode odsCodeArr = obj.putArray("publisherOdsCodes");
                String uuid = dsa.getUuid();
                List<String> publisherOdsCodes = ProjectCache.getAllPublishersForProjectWithSubscriberCheck(uuid, odsCode);
                for (String publisherOdsCode: publisherOdsCodes) {
                    odsCodeArr.add(publisherOdsCode);
                }
            }



        } catch (Throwable t) {
            String msg = t.getMessage();
            obj.put("error", msg);
        }

        String retJson = mapper.writeValueAsString(obj);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(retJson)
                .build();
    }

}
