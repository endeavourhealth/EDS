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
import org.endeavourhealth.core.database.dal.admin.OrganisationDalI;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.ActiveItem;
import org.endeavourhealth.core.database.dal.admin.models.Item;
import org.endeavourhealth.core.database.dal.admin.models.Organisation;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeTransformErrorState;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.fhirStorage.FhirDeletionService;
import org.endeavourhealth.core.fhirStorage.JsonServiceInterfaceEndpoint;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.core.xml.QueryDocument.System;
import org.endeavourhealth.core.xml.QueryDocumentSerializer;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.coreui.json.JsonOrganisation;
import org.endeavourhealth.ui.json.JsonService;
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

        if (service.getUuid() == null)
            service.setUuid(serviceId);


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
            return;
        }
        String postcode = org.getPostcode();
        service.setPostcode(postcode);

        OrganisationType type = org.getOrganisationType();
        service.setOrganisationTypeCode(type.getCode());

        Map<String, String> parents = org.getParents();
        //some orgs have multiple parents, and I can't think of a good way to select the best one, so don't apply this
        if (parents.size() == 1) {
            String ccgCode = parents.keySet().iterator().next();
            service.setCcgCode(ccgCode);
        }
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
        ResourceDalI resourceRepository = DalProvider.factoryResourceDal();
        List<JsonServiceInterfaceEndpoint> endpoints = ObjectMapperPool.getInstance().readValue(service.getEndpoints(), new TypeReference<List<JsonServiceInterfaceEndpoint>>() {
        });
        for (JsonServiceInterfaceEndpoint endpoint : endpoints) {
            UUID systemId = endpoint.getSystemUuid();

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
                                      @QueryParam("serviceId") String serviceIdStr,
                                      @QueryParam("systemId") String systemIdStr) throws Exception {
        super.setLogbackMarkers(sc);
        userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Service Data",
                "Service Id", serviceIdStr,
                "System Id", systemIdStr);

        UUID serviceUuid = UUID.fromString(serviceIdStr);
        UUID systemUuid = UUID.fromString(systemIdStr);

        if (dataBeingDeleted.get(serviceUuid) != null) {
            throw new BadRequestException("Data deletion already in progress for this service");
        }

        final Service dbService = serviceRepository.getById(serviceUuid);

        //the delete will take some time, so do the delete in a separate thread
        Runnable task = () -> {
            LOG.info("Deleting all data for service " + dbService.getName() + " " + dbService.getId());
            FhirDeletionService deletor = new FhirDeletionService(dbService, systemUuid);
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

        //we want to indicate if any service has inbound errors
        Set<UUID> servicesInError = calculateServicesInError(services);

        List<JsonService> ret = new ArrayList<>();

        for (Service service : services) {
            UUID serviceId = service.getId();
            boolean isInError = servicesInError.contains(serviceId);
            String additionalInfo = getAdditionalInfo(service);
            ret.add(new JsonService(service, additionalInfo, isInError));
        }

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
        Set<UUID> servicesInError = calculateServicesInError(services);

        UUID serviceId = service.getId();
        boolean isInError = servicesInError.contains(serviceId);
        String additionalInfo = getAdditionalInfo(service);
        JsonService ret = new JsonService(service, additionalInfo, isInError);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getServicesMatchingText(String searchData) throws Exception {
        List<Service> services = serviceRepository.search(searchData);

        Set<UUID> servicesInError = calculateServicesInError(services);

        List<JsonService> ret = new ArrayList<>();

        for (Service service : services) {
            UUID serviceId = service.getId();
            boolean isInError = servicesInError.contains(serviceId);
            String additionalInfo = getAdditionalInfo(service);
            ret.add(new JsonService(service, additionalInfo, isInError));
        }

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Set<UUID> calculateServicesInError(List<Service> services) throws Exception {
        Set<UUID> ret = new HashSet<>();

        if (services.isEmpty()) {
            return ret;
        }

        //if we've less than X services, hit the DB one at a time, rather than loading the full error list
        if (services.size() < 5) {

            for (Service service : services) {
                UUID serviceId = service.getId();
                List<UUID> systemIds = findSystemIdsFromService(service);
                for (UUID systemId : systemIds) {
                    ExchangeTransformErrorState errorState = exchangeAuditRepository.getErrorState(serviceId, systemId);
                    if (errorState != null) {
                        ret.add(serviceId);
                        //no need to check the other systems for this service, so break out
                        break;
                    }
                }
            }

        } else {
            //if we're lots of services, it's easier to load all the error states
            List<ExchangeTransformErrorState> errorStates = exchangeAuditRepository.getAllErrorStates();
            for (ExchangeTransformErrorState errorState : errorStates) {
                UUID serviceId = errorState.getServiceId();
                ret.add(serviceId);
            }
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
