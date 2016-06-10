package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.Service;
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
}
