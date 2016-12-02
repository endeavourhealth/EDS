package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.admin.OrganisationRepository;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.core.security.SecurityUtils;
import org.endeavourhealth.core.security.annotations.RequiresAdmin;
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

@Path("/organisation")
public final class OrganisationEndpoint extends AbstractEndpoint {
	private static final Logger LOG = LoggerFactory.getLogger(OrganisationEndpoint.class);

	private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);
	private static final OrganisationRepository repository = new OrganisationRepository();
	private static final ServiceRepository serviceRepository = new ServiceRepository();

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/")
	@RequiresAdmin
	public Response post(@Context SecurityContext sc, JsonOrganisation organisation) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
				"Organisation", organisation);

		// Save the new
		Organisation dbOrganisation = new Organisation();
		dbOrganisation.setId(organisation.getUuid());
		dbOrganisation.setName(organisation.getName());
		dbOrganisation.setNationalId(organisation.getNationalId());
		dbOrganisation.setServices(organisation.getServices());
		UUID organisationUuid = repository.save(dbOrganisation);

		if (organisation.getUuid() == null)
			organisation.setUuid(organisationUuid);

		clearLogbackMarkers();

		return Response
				.ok()
				.entity(organisation)
				.build();
	}


	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/")
	@RequiresAdmin
	public Response deleteOrganisation(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
				"Organisation Id", uuid);

		UUID organisationUuid = UUID.fromString(uuid);
		Organisation dbOrganisation = repository.getById(organisationUuid);

		repository.delete(dbOrganisation);

		clearLogbackMarkers();
		return Response
				.ok()
				.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/services")
	public Response getOrganisationServices(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Organisation Services", uuid);

		super.setLogbackMarkers(sc);
		UUID organisationUuid = UUID.fromString(uuid);
		Organisation organisation = repository.getById(organisationUuid);

		List<JsonService> ret = new ArrayList<>();
		for (UUID serviceId : organisation.getServices().keySet()) {
			Service service = serviceRepository.getById(serviceId);
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
	@Path("/")
	public Response get(@Context SecurityContext sc, @QueryParam("uuid") String uuid, @QueryParam("searchData") String searchData) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Organisation Id", uuid,
				"SearchData", searchData);

		if (uuid == null && searchData == null) {
			LOG.trace("getOrganisation - list");
			return getOrganisationList();
		} else if (uuid != null){
			LOG.trace("getOrganisation - single - " + uuid);
			return getOrganisation(uuid);
		} else {
			LOG.trace("Search Organisations - " + searchData);
			return search(searchData);
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

	private Response search(String searchData) {
		Iterable<Organisation> organisations = repository.search(searchData);

		List<JsonOrganisation> ret = new ArrayList<>();

		for (Organisation organisation : organisations) {
			ret.add(new JsonOrganisation(organisation, false));
		}

		clearLogbackMarkers();
		return Response
				.ok()
				.entity(ret)
				.build();
	}
}
