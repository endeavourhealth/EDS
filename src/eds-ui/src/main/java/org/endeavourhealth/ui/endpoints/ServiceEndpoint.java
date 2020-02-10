package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
import org.endeavourhealth.common.fhir.schema.OrganisationType;
import org.endeavourhealth.common.ods.OdsOrganisation;
import org.endeavourhealth.common.ods.OdsWebService;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.common.utility.XmlSerializer;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryDalI;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.ActiveItem;
import org.endeavourhealth.core.database.dal.admin.models.Item;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.*;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.endeavourhealth.core.xml.QueryDocument.System;
import org.endeavourhealth.core.xml.QueryDocumentSerializer;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
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
import java.util.*;

@Path("/service")
public final class ServiceEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceEndpoint.class);

    private static final ServiceDalI serviceRepository = DalProvider.factoryServiceDal();
    private static final UserAuditDalI userAuditRepository = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Service);
    private static final ExchangeDalI exchangeAuditRepository = DalProvider.factoryExchangeDal();

    private static List<String> cachedTagNames = null;

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

        Service dbService = new Service();
        dbService.setId(service.getUuid());
        dbService.setName(service.getName());
        dbService.setLocalId(service.getLocalIdentifier());
        dbService.setPublisherConfigName(service.getPublisherConfigName());
        dbService.setPostcode(service.getPostcode());
        dbService.setCcgCode(service.getCcgCode());
        if (!Strings.isNullOrEmpty(service.getOrganisationTypeCode())) {
            dbService.setOrganisationType(OrganisationType.fromCode(service.getOrganisationTypeCode()));
        }
        dbService.setEndpointsList(service.getEndpoints());
        dbService.setAlias(service.getAlias());
        if (service.getTags() != null) {
            dbService.setTags(new HashMap<>(service.getTags()));

            //make sure any new ones are added to the cached list
            if (cachedTagNames != null) {
                Set<String> hs = new HashSet<>(cachedTagNames);
                for (String tagName: service.getTags().keySet()) {
                    hs.add(tagName);
                }
                List<String> l = new ArrayList<>(hs);
                l.sort(((o1, o2) -> o1.toLowerCase().compareToIgnoreCase(o2.toLowerCase())));
                cachedTagNames = l;
            }
        }

        UUID serviceId = serviceRepository.save(dbService);

        if (service.getUuid() == null) {
            service.setUuid(serviceId);
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
            Service existingService = dal.getByLocalIdentifier(localId);
            if (existingService != null
                    && (serviceToSave.getUuid() == null
                        || !serviceToSave.getUuid().equals(existingService.getId()))) {
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

        //ensure don't change publisher state to normal or bulk when there's stuff in the queue already
        if (error == null) {
            UUID existingServiceId = serviceToSave.getUuid();
            if (existingServiceId != null) {
                Service existingService = dal.getById(existingServiceId);
                if (existingService != null) {

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

                                ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
                                List<Exchange> mostRecentExchanges = exchangeDal.getExchangesByService(existingServiceId, systemUuid, 1);
                                if (!mostRecentExchanges.isEmpty()) {
                                    Exchange mostRecentExchange = mostRecentExchanges.get(0);
                                    ExchangeTransformAudit latestTransform = exchangeDal.getLatestExchangeTransformAudit(existingServiceId, systemUuid, mostRecentExchange.getId());

                                    boolean inQueue = false;

                                    //if the exchange has never been transformed or the transform hasn't ended, we
                                    //can infer that it's in the queue
                                    if (latestTransform == null
                                            || latestTransform.getEnded() == null) {
                                        LOG.debug("Exchange " + mostRecentExchange.getId() + " has never been transformed or hasn't finished yet");
                                        inQueue = true;

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
                                                inQueue = true;
                                            }
                                        }
                                    }

                                    if (inQueue) {
                                        error = "Cannot change publisher mode while inbound messages are queued";
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
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

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
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

        //validate that there's no data in the EHR repo before allowing a delete
        if (!Strings.isNullOrEmpty(service.getPublisherConfigName())) {
            ResourceDalI resourceRepository = DalProvider.factoryResourceDal();
            if (resourceRepository.dataExists(serviceUuid)) {
                throw new BadRequestException("Cannot delete service without deleting data first");
            }
        }

        serviceRepository.delete(service);

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
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
    public Response get(@Context SecurityContext sc, @QueryParam("uuid") String uuid, @QueryParam("searchData") String searchData) throws Exception {
        super.setLogbackMarkers(sc);
        userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Service(s)",
                "Service Id", uuid,
                "Search Data", searchData);

        if (uuid == null && searchData == null) {
            LOG.trace("Get Service list");
            return getServiceList();

        } else if (uuid != null) {
            LOG.trace("Get Service single - " + uuid);
            return getSingleService(uuid);

        } else {
            LOG.trace("Search services - " + searchData);
            return getServicesMatchingText(searchData);
        }
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

    private Response getSingleService(String uuid) throws Exception {
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
    @Timed(absolute = true, name = "ServiceEndpoint.GetSystemsForService")
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
    @Timed(absolute = true, name = "ServiceEndpoint.GetServiceOrganisations")
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
    @Timed(absolute = true, name = "ServiceEndpoint.GetProtocolsForService")
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
    @Timed(absolute = true, name = "ServiceEndpoint.OrganisationTypeList")
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
    @Timed(absolute = true, name = "ServiceEndpoint.OrganisationTypeList")
    @Path("/tagNames")
    public Response getTagNames(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        if (cachedTagNames == null) {

            Set<String> all = new HashSet<>();

            List<Service> services = serviceRepository.getAll();
            for (Service service: services) {
                if (service.getTags() != null) {
                    Set<String> set = service.getTags().keySet();
                    all.addAll(set);
                }
            }

            List<String> l = new ArrayList<>(all);
            l.sort(((o1, o2) -> o1.toLowerCase().compareToIgnoreCase(o2.toLowerCase())));
            cachedTagNames = l;
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(cachedTagNames)
                .build();
    }
}
