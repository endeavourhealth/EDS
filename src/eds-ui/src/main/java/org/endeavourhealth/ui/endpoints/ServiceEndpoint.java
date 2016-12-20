package org.endeavourhealth.ui.endpoints;

import com.fasterxml.jackson.core.type.TypeReference;
import org.endeavourhealth.core.cache.ObjectMapperPool;
import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.admin.OrganisationRepository;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.ActiveItem;
import org.endeavourhealth.core.data.admin.models.Item;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.fhirStorage.FhirDeletionService;
import org.endeavourhealth.core.json.JsonServiceInterfaceEndpoint;
import org.endeavourhealth.core.security.SecurityUtils;
import org.endeavourhealth.core.security.annotations.RequiresAdmin;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Path("/service")
public final class ServiceEndpoint extends AbstractEndpoint {
	private static final Logger LOG = LoggerFactory.getLogger(ServiceEndpoint.class);

	private static final ServiceRepository repository = new ServiceRepository();
	private static final OrganisationRepository organisationRepository = new OrganisationRepository();
	private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Service);
	private static final Map<UUID, FhirDeletionService> dataBeingDeleted = new ConcurrentHashMap<>();

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/")
	@RequiresAdmin
	public Response post(@Context SecurityContext sc, JsonService service) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
				"Service",
				"Service", service);

		Service dbService = new Service();
		dbService.setId(service.getUuid());
		dbService.setName(service.getName());
		dbService.setLocalIdentifier(service.getLocalIdentifier());
		dbService.setOrganisations(service.getOrganisations());

		String endpointsJson = ObjectMapperPool.getInstance().writeValueAsString(service.getEndpoints());
		dbService.setEndpoints(endpointsJson);

		UUID serviceId = repository.save(dbService);

		if (service.getUuid() == null)
			service.setUuid(serviceId);

		clearLogbackMarkers();

		return Response
				.ok()
				.entity(service)
				.build();
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/")
	@RequiresAdmin
	public Response deleteService(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
				"Service",
				"Service Id", uuid);

		UUID serviceUuid = UUID.fromString(uuid);
		Service service = repository.getById(serviceUuid);

		//validate that there's no data in the EHR repo before allowing a delete
		ResourceRepository resourceRepository = new ResourceRepository();
		List<JsonServiceInterfaceEndpoint> endpoints = ObjectMapperPool.getInstance().readValue(service.getEndpoints(), new TypeReference<List<JsonServiceInterfaceEndpoint>>() {});
		for (JsonServiceInterfaceEndpoint endpoint: endpoints) {
			UUID systemId = endpoint.getSystemUuid();

			if (resourceRepository.dataExists(serviceUuid, systemId)) {
				throw new BadRequestException("Cannot delete service without deleting data first");
			}
		}

		repository.delete(service);

		clearLogbackMarkers();
		return Response
				.ok()
				.build();
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/data")
	@RequiresAdmin
	public Response deleteServiceData(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
				"Service Data",
				"Service Id", uuid);

		UUID serviceUuid = UUID.fromString(uuid);
		if (dataBeingDeleted.get(serviceUuid) != null) {
			throw new BadRequestException("Data deletion already in progress");
		}

		final Service dbService = repository.getById(serviceUuid);

		//the delete will take some time, so do the delete in a separate thread. This does mean
		//that there's no way to check the progress of the delete, but that can be added later.
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
	@Path("/organisations")
	public Response getServiceOrganisations(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Service Organisations",
				"ServiceId", uuid);

		UUID serviceUuid = UUID.fromString(uuid);
		Service service = repository.getById(serviceUuid);

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
	@Path("/")
	public Response get(@Context SecurityContext sc, @QueryParam("uuid") String uuid, @QueryParam("searchData") String searchData) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Service(s)",
				"Service Id", uuid,
				"Search Data", searchData);

		if (uuid == null && searchData == null) {
			LOG.trace("Get Service list");
			return getList();
		} else if (uuid != null) {
			LOG.trace("Get Service single - " + uuid);
			return get(uuid);
		} else {
			LOG.trace("Search services - " + searchData);
			return search(searchData);
		}
	}

	private Response getList() throws Exception {
		Iterable<Service> services = repository.getAll();

		List<JsonService> ret = new ArrayList<>();

		for (Service service: services) {
			ret.add(new JsonService(service, getAdditionalInfo(service)));
		}

		clearLogbackMarkers();
		return Response
				.ok()
				.entity(ret)
				.build();
	}

	private Response get(String uuid) throws Exception {
		UUID serviceUuid = UUID.fromString(uuid);
		Service service = repository.getById(serviceUuid);

		JsonService ret = new JsonService(service, getAdditionalInfo(service));

		clearLogbackMarkers();
		return Response
				.ok()
				.entity(ret)
				.build();
	}

	private Response search(String searchData) throws Exception {
		Iterable<Service> services = repository.search(searchData);

		List<JsonService> ret = new ArrayList<>();

		for (Service service: services) {
			ret.add(new JsonService(service, getAdditionalInfo(service)));
		}

		clearLogbackMarkers();
		return Response
				.ok()
				.entity(ret)
				.build();
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
	@Path("/systemsForService")
	public Response getSystemsForService(@Context SecurityContext sc, @QueryParam("serviceId") String serviceIdStr) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Service Systems",
				"ServiceId", serviceIdStr);

		UUID serviceId = UUID.fromString(serviceIdStr);
		org.endeavourhealth.core.data.admin.models.Service service = new ServiceRepository().getById(serviceId);

		LibraryRepository libraryRepository = new LibraryRepository();

		List<System> ret = new ArrayList<>();

		List<JsonServiceInterfaceEndpoint> endpoints = ObjectMapperPool.getInstance().readValue(service.getEndpoints(), new TypeReference<List<JsonServiceInterfaceEndpoint>>() {});
		for (JsonServiceInterfaceEndpoint endpoint: endpoints) {

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
