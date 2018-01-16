package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.OrganisationDalI;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Organisation;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
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
import java.util.UUID;

@Path("/organisation")
@Metrics(registry = "EdsRegistry")
public final class OrganisationEndpoint extends AbstractEndpoint {
	private static final Logger LOG = LoggerFactory.getLogger(OrganisationEndpoint.class);

	private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Organisation);
	private static final OrganisationDalI organisationRepository = DalProvider.factoryOrganisationDal();
	private static final ServiceDalI serviceRepository = DalProvider.factoryServiceDal();

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(absolute = true, name="EDS-UI.OrganisationEndpoint.Post")
	@Path("/")
	@RequiresAdmin
	public Response post(@Context SecurityContext sc, JsonOrganisation organisation) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
				"Organisation",
				"Organisation", organisation);

		// Save the new
		Organisation dbOrganisation = new Organisation();
		dbOrganisation.setId(organisation.getUuid());
		dbOrganisation.setName(organisation.getName());
		dbOrganisation.setNationalId(organisation.getNationalId());
		dbOrganisation.setServices(organisation.getServices());
		UUID organisationUuid = organisationRepository.save(dbOrganisation);

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
	@Timed(absolute = true, name="EDS-UI.OrganisationEndpoint.DeleteOrganisation")
	@Path("/")
	@RequiresAdmin
	public Response deleteOrganisation(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
				"Organisation",
				"Organisation Id", uuid);

		UUID organisationUuid = UUID.fromString(uuid);
		Organisation dbOrganisation = organisationRepository.getById(organisationUuid);

		organisationRepository.delete(dbOrganisation);

		clearLogbackMarkers();
		return Response
				.ok()
				.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(absolute = true, name="EDS-UI.OrganisationEndpoint.GetServices")
	@Path("/services")
	public Response getOrganisationServices(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Organisation Services",
				"OrganisationId", uuid);

		super.setLogbackMarkers(sc);
		UUID organisationUuid = UUID.fromString(uuid);
		Organisation organisation = organisationRepository.getById(organisationUuid);

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
	@Timed(absolute = true, name="EDS-UI.OrganisationEndpoint.Get")
	@Path("/")
	public Response get(@Context SecurityContext sc, @QueryParam("uuid") String uuid, @QueryParam("searchData") String searchData) throws Exception {
		super.setLogbackMarkers(sc);
		userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Organisation(s)",
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
		Iterable<Organisation> organisations = organisationRepository.getAll();

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
		Organisation organisation = organisationRepository.getById(organisationUuid);

		JsonOrganisation ret = new JsonOrganisation(organisation, false);

		clearLogbackMarkers();
		return Response
				.ok()
				.entity(ret)
				.build();
	}

	private Response search(String searchData) throws Exception {
		Iterable<Organisation> organisations = organisationRepository.search(searchData);

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
