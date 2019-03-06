package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import io.astefanutti.metrics.aspectj.Metrics;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.fhir.schema.OrganisationType;
import org.endeavourhealth.common.ods.OdsOrganisation;
import org.endeavourhealth.common.ods.OdsWebService;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryDalI;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.admin.OrganisationDalI;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.ActiveItem;
import org.endeavourhealth.core.database.dal.admin.models.Item;
import org.endeavourhealth.core.database.dal.admin.models.Organisation;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.*;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.fhirStorage.FhirDeletionService;
import org.endeavourhealth.core.fhirStorage.JsonServiceInterfaceEndpoint;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.core.xml.QueryDocument.System;
import org.endeavourhealth.core.xml.QueryDocumentSerializer;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.coreui.json.JsonOrganisation;
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
import java.util.concurrent.ConcurrentHashMap;

@Path("/service")
@Metrics(registry = "EdsRegistry")
public final class ServiceEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceEndpoint.class);

    private static final ServiceDalI serviceRepository = DalProvider.factoryServiceDal();
    private static final OrganisationDalI organisationRepository = DalProvider.factoryOrganisationDal();
    private static final UserAuditDalI userAuditRepository = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Service);
    private static final ExchangeDalI exchangeAuditRepository = DalProvider.factoryExchangeDal();

    private static final Map<UUID, FhirDeletionService> dataBeingDeleted = new ConcurrentHashMap<>();

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "EDS-UI.ServiceEndpoint.Post")
    @Path("/")
    @RequiresAdmin
    public Response post(@Context SecurityContext sc, JsonService service) throws Exception {
        super.setLogbackMarkers(sc);
        userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Service",
                "Service", service);

        //if no postcode etc. are set, see if we can find them via Open ODS
        populateFromOds(service);

        Service dbService = new Service();
        dbService.setId(service.getUuid());
        dbService.setName(service.getName());
        dbService.setLocalId(service.getLocalIdentifier());
        dbService.setOrganisations(service.getOrganisations());
        dbService.setPublisherConfigName(service.getPublisherConfigName());
        dbService.setNotes(service.getNotes());
        dbService.setPostcode(service.getPostcode());
        dbService.setCcgCode(service.getCcgCode());
        if (!Strings.isNullOrEmpty(service.getOrganisationTypeCode())) {
            dbService.setOrganisationType(OrganisationType.fromCode(service.getOrganisationTypeCode()));
        }

        String endpointsJson = ObjectMapperPool.getInstance().writeValueAsString(service.getEndpoints());
        dbService.setEndpoints(endpointsJson);

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

    private void populateFromOds(JsonService service) throws Exception {

        if (Strings.isNullOrEmpty(service.getLocalIdentifier())) {
            return;
        }

        if (!Strings.isNullOrEmpty(service.getPostcode())
                || !Strings.isNullOrEmpty(service.getCcgCode())
                || !Strings.isNullOrEmpty(service.getOrganisationTypeCode())) {
            return;
        }

        OdsOrganisation org = OdsWebService.lookupOrganisationViaRest(service.getLocalIdentifier());
        if (org == null) {
            LOG.error("NO ODS record found for [" + service.getLocalIdentifier() + "]");
            return;
        }
        String postcode = org.getPostcode();
        service.setPostcode(postcode);

        OrganisationType type = org.getOrganisationType();
        service.setOrganisationTypeCode(type.getCode());

        Map<String, String> parents = org.getParents();

        String parentCode = null;
        if (parents.size() == 1) {
            parentCode = parents.keySet().iterator().next();

        } else {
            //some orgs have multiple parents, and the simplest way to choose just one seems to be
            //to ignore the old SHA hierarchy
            for (String code: parents.keySet()) {
                String name = parents.get(code);
                if (name.toUpperCase().contains("STRATEGIC HEALTH AUTHORITY")) {
                    continue;
                }

                parentCode = code;
            }
        }

        service.setCcgCode(parentCode);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "EDS-UI.ServiceEndpoint.DeleteService")
    @Path("/")
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
    @Timed(absolute = true, name = "EDS-UI.ServiceEndpoint.DeleteServiceData")
    @Path("/data")
    @RequiresAdmin
    public Response deleteServiceData(@Context SecurityContext sc,
                                      @QueryParam("serviceId") String serviceIdStr) throws Exception {
        super.setLogbackMarkers(sc);
        userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Service Data",
                "Service Id", serviceIdStr);

        UUID serviceUuid = UUID.fromString(serviceIdStr);

        if (dataBeingDeleted.get(serviceUuid) != null) {
            throw new BadRequestException("Data deletion already in progress for this service");
        }

        final Service dbService = serviceRepository.getById(serviceUuid);

        //the delete will take some time, so do the delete in a separate thread
        Runnable task = () -> {
            LOG.info("Deleting all data for service " + dbService.getName() + " " + dbService.getId());

            FhirDeletionService deletor = new FhirDeletionService(dbService);
            dataBeingDeleted.put(dbService.getId(), deletor);

            try {
                deletor.deleteData();
                LOG.info("Completed deleting all data for service " + dbService.getName() + " " + dbService.getId());
            } catch (Exception ex) {
                LOG.error("Error deleting service " + dbService.getName() + " " + dbService.getId(), ex);
            } finally {
                dataBeingDeleted.remove(dbService.getId());
            }
        };

        Thread thread = new Thread(task);
        thread.start();

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "EDS-UI.ServiceEndpoint.GetServiceOrganisations")
    @Path("/organisations")
    public Response getServiceOrganisations(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Service Organisations",
                "ServiceId", uuid);

        UUID serviceUuid = UUID.fromString(uuid);
        Service service = serviceRepository.getById(serviceUuid);

        List<JsonOrganisation> ret = new ArrayList<>();
        for (UUID organisationId : service.getOrganisations().keySet()) {
            Organisation organisation = organisationRepository.getById(organisationId);
            ret.add(new JsonOrganisation(organisation, false));
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
    @Timed(absolute = true, name = "EDS-UI.ServiceEndpoint.GetServiceList")
    @Path("/")
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
    private List<JsonService> createAndPopulateJsonServices(List<Service> services) throws Exception {

        //get the extra information we want
        Map<UUID, Set<UUID>> hmServicesInError = findServicesInError(services);
        Map<UUID, Map<UUID, LastDataReceived>> hmLastDataReceived = findLastDataReceived(services);
        Map<UUID, Map<UUID, LastDataProcessed>> hmLastDataProcessed = findLastDataProcessed(services);

        List<JsonService> ret = new ArrayList<>();

        for (Service service : services) {
            UUID serviceId = service.getId();
            String additionalInfo = getAdditionalInfo(service);
            JsonService jsonService = new JsonService(service, additionalInfo);

            //work out where we are with processing data
            List<JsonServiceSystemStatus> jsonSystemStatuses = createAndPopulateJsonSystemStatuses(serviceId, hmServicesInError, hmLastDataReceived, hmLastDataProcessed);
            jsonService.setSystemStatuses(jsonSystemStatuses);

            ret.add(jsonService);
        }

        return ret;
    }

    private List<JsonServiceSystemStatus> createAndPopulateJsonSystemStatuses(UUID serviceId,
                                                                              Map<UUID, Set<UUID>> hmServicesInError,
                                                                              Map<UUID, Map<UUID, LastDataReceived>> hmLastDataReceived,
                                                                              Map<UUID, Map<UUID, LastDataProcessed>> hmLastDataProcessed) throws Exception {

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

            ret.add(status);
        }

        return ret;
    }

    private Map<UUID, Map<UUID, LastDataProcessed>> findLastDataProcessed(List<Service> services) throws Exception {

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

    private Map<UUID, Map<UUID, LastDataReceived>> findLastDataReceived(List<Service> services) throws Exception {

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

    /*private Map<UUID, String> findLastDataDescs(List<Service> services) throws Exception {

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

        long durMin = 1000 * 60;
        long durHour = durMin * 60;
        long durDay = durHour * 25;
        long durWeek = durDay * 7;
        long durYear = (long)((double)durDay * 365.25d);

        //first, hash the objects by service, so we know how many there are for each service
        Map<UUID, List<LastDataReceived>> hmByService = new HashMap<>();

        for (LastDataReceived obj : list) {
            UUID serviceId = obj.getServiceId();

            List<LastDataReceived> l = hmByService.get(serviceId);
            if (l == null) {
                l = new ArrayList<>();
                hmByService.put(serviceId, l);
            }
            l.add(obj);
        }

        //now go through the map and create a suitable string for each service
        Map<UUID, String> ret = new HashMap<>();

        for (UUID serviceId: hmByService.keySet()) {

            List<LastDataReceived> l = hmByService.get(serviceId);

            List<String> lines = new ArrayList<>();

            for (LastDataReceived obj: l) {

                Date d = obj.getDataDate();

                //format the time span between the date and now. I've honestly spent ages trying to write
                //this using the Java 8 time classes, which are supposed to be better, but gave up and
                //just went with this quick and dirty approach
                List<String> periodToks = new ArrayList<>();

                long diffMs = java.lang.System.currentTimeMillis() - d.getTime();

                long years = diffMs / durYear;
                if (years > 0 && periodToks.size() < 2) {
                    periodToks.add("" + years + "y");
                    diffMs -= years * durYear;
                }
                long weeks = diffMs / durWeek;
                if (weeks > 0 && periodToks.size() < 2) {
                    periodToks.add("" + weeks + "w");
                    diffMs -= weeks * durWeek;
                }
                long days = diffMs / durDay;
                if (days > 0 && periodToks.size() < 2) {
                    periodToks.add("" + days + "d");
                    diffMs -= days * durDay;
                }
                long hours = diffMs / durHour;
                if (hours > 0 && periodToks.size() < 2) {
                    periodToks.add("" + hours + "h");
                    diffMs -= hours * durHour;
                }
                long mins = diffMs / durMin;
                if (mins > 0 && periodToks.size() < 2) {
                    periodToks.add("" + mins + "m");
                    diffMs -= mins * durMin;
                }

                String periodDesc = String.join(" ", periodToks);

                //if there are multiple systems, include the system name in the string for each period
                if (l.size() > 1) {

                    UUID systemId = obj.getSystemId();
                    String softwareDesc = findSoftwareDescForSystem(systemId);
                    lines.add(softwareDesc + ": " + periodDesc);

                } else {
                    lines.add(periodDesc);
                }
            }

            String s = String.join(", ", lines);
            ret.put(serviceId, s);
        }

        return ret;
    }*/

    private static String findSoftwareDescForSystem(UUID systemId) throws Exception {

        LibraryItem libraryItem = LibraryRepositoryHelper.getLibraryItemUsingCache(systemId);
        if (libraryItem == null) {
            return "MISSING SYSTEM";
        } else {
            return libraryItem.getName();
        }
    }

    private Map<UUID, Set<UUID>> findServicesInError(List<Service> services) throws Exception {

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

        List<JsonServiceInterfaceEndpoint> endpoints = null;
        try {
            endpoints = ObjectMapperPool.getInstance().readValue(service.getEndpoints(), new TypeReference<List<JsonServiceInterfaceEndpoint>>() {
            });
            for (JsonServiceInterfaceEndpoint endpoint : endpoints) {
                UUID endpointSystemId = endpoint.getSystemUuid();
                ret.add(endpointSystemId);
            }
        } catch (Exception e) {
            throw new Exception("Failed to process endpoints from service " + service.getId());
        }

        return ret;
    }

    /**
     * returns additional info string for the service. Currently this is just
     * the progress on data being deleted
     */
    private String getAdditionalInfo(Service service) {

        FhirDeletionService deletionService = dataBeingDeleted.get(service.getId());
        if (deletionService != null) {
            return "Data being deleted: " + deletionService.getProgress();
        }

        return null;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "EDS-UI.ServiceEndpoint.GetSystemsForService")
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

        List<JsonServiceInterfaceEndpoint> endpoints = ObjectMapperPool.getInstance().readValue(service.getEndpoints(), new TypeReference<List<JsonServiceInterfaceEndpoint>>() {
        });
        for (JsonServiceInterfaceEndpoint endpoint : endpoints) {

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

}
