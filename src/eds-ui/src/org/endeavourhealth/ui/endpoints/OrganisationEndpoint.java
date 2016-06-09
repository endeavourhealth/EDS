package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.admin.OrganisationRepository;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.ui.json.JsonOrganisation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;

@Path("/organisation")
public final class OrganisationEndpoint extends AbstractEndpoint {
	private static final Logger LOG = LoggerFactory.getLogger(OrganisationEndpoint.class);
	private final OrganisationRepository repository = new OrganisationRepository();

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/")
	public Response getOrganisation(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
		super.setLogbackMarkers(sc);

		if (uuid == null) {
			LOG.trace("getOrganisation - list");
			return getOrganisationList();
		} else {
			LOG.trace("getOrganisation - single - " + uuid);
			return getOrganisation(uuid);
		}
	}

	private Response getOrganisationList() throws Exception {
		Iterable<Organisation> organisations = repository.getAll();

		List<JsonOrganisation> ret = new ArrayList<>();

		for (Organisation organisation: organisations) {
			ret.add(new JsonOrganisation(organisation, false));
		}

		clearLogbackMarkers();
		return Response
				.ok()
				.entity(ret)
				.build();
	}

	private Response getOrganisation(String uuid) throws Exception {
		UUID organisationUuid = UUID.fromString(uuid);
		Organisation organisation = repository.getById(organisationUuid);

		JsonOrganisation ret = new JsonOrganisation(organisation, false);

		clearLogbackMarkers();
		return Response
				.ok()
				.entity(ret)
				.build();
	}
}
