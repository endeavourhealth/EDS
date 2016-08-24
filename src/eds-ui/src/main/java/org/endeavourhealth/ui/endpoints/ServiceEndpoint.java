package org.endeavourhealth.ui.endpoints;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.admin.OrganisationRepository;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.ActiveItem;
import org.endeavourhealth.core.data.admin.models.Item;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.json.JsonServiceInterfaceEndpoint;
import org.endeavourhealth.core.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.core.xml.QueryDocument.System;
import org.endeavourhealth.core.xml.QueryDocumentSerializer;
import org.endeavourhealth.ui.json.JsonOrganisation;
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
import java.util.UUID;

@Path("/service")
public final class ServiceEndpoint extends AbstractEndpoint {
	private static final Logger LOG = LoggerFactory.getLogger(ServiceEndpoint.class);
	private final ServiceRepository repository = new ServiceRepository();
	private final OrganisationRepository organisationRepository = new OrganisationRepository();

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/")
	@RequiresAdmin
	public Response post(@Context SecurityContext sc, JsonService service) throws Exception {
		super.setLogbackMarkers(sc);

		Service dbService = new Service();
		dbService.setId(service.getUuid());
		dbService.setName(service.getName());
		dbService.setLocalIdentifier(service.getLocalIdentifier());
		dbService.setOrganisations(service.getOrganisations());

		String endpointsJson = new ObjectMapper().writeValueAsString(service.getEndpoints());
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

		UUID serviceUuid = UUID.fromString(uuid);
		Service dbService = repository.getById(serviceUuid);

		repository.delete(dbService);

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
			ret.add(new JsonService(service));
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

		JsonService ret = new JsonService(service);

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
			ret.add(new JsonService(service));
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
	@Path("/systemsForService")
	public Response getSystemsForService(@Context SecurityContext sc, @QueryParam("serviceId") String serviceIdStr) throws Exception {
		super.setLogbackMarkers(sc);

		UUID serviceId = UUID.fromString(serviceIdStr);
		org.endeavourhealth.core.data.admin.models.Service service = new ServiceRepository().getById(serviceId);

		LibraryRepository libraryRepository = new LibraryRepository();

		List<System> ret = new ArrayList<>();

		List<JsonServiceInterfaceEndpoint> endpoints = new ObjectMapper().readValue(service.getEndpoints(), new TypeReference<List<JsonServiceInterfaceEndpoint>>() {});
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
