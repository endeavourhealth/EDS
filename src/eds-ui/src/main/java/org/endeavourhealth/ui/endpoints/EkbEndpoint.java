package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.ui.database.EkbManager;
import org.endeavourhealth.ui.database.models.ConceptEntity;
import org.endeavourhealth.ui.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.stream.Collectors;

@Path("/ekb")
@Metrics(registry = "EdsRegistry")
public final class EkbEndpoint extends AbstractEndpoint {
	private static final Logger LOG = LoggerFactory.getLogger(EkbEndpoint.class);
	private UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Ekb);


	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(absolute = true, name="EDS-UI.EkbEndpoint.Search")
	@Path("/search/sct")
	public Response search(@Context SecurityContext sc, @QueryParam("term") String term, @QueryParam("maxResultsSize") int maxResultsSize, @QueryParam("start") int start) throws Exception {
		super.setLogbackMarkers(sc);
		// userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load);

		List<ConceptEntity> concepts = EkbManager.search(term, maxResultsSize, start);
		List<JsonCodeSetValue> ret = concepts.stream().map(JsonCodeSetValue::new).collect(Collectors.toList());

		clearLogbackMarkers();

		return Response
				.ok()
				.entity(ret)
				.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(absolute = true, name="EDS-UI.EkbEndpoint.GetConcept")
	@Path("/concepts/{code}")
	public Response getConcept(@Context SecurityContext sc, @PathParam("code") String code) throws Exception {
		super.setLogbackMarkers(sc);
		// userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load);

		ConceptEntity concept = EkbManager.getConcept(code);
		JsonConcept ret = new JsonConcept(concept);

		clearLogbackMarkers();

		return Response
				.ok()
				.entity(ret)
				.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(absolute = true, name="EDS-UI.EkbEndpoint.GetChildren")
	@Path("/hierarchy/{code}/childHierarchy")
	public Response getChildren(@Context SecurityContext sc, @PathParam("code") String code) throws Exception {
		super.setLogbackMarkers(sc);
		// userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load);

		List<ConceptEntity> concepts = EkbManager.getChildren(code);
		List<JsonCodeSetValue> ret = concepts.stream().map(JsonCodeSetValue::new).collect(Collectors.toList());

		clearLogbackMarkers();

		return Response
				.ok()
				.entity(ret)
				.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed(absolute = true, name="EDS-UI.EkbEndpoint.GetParents")
	@Path("/hierarchy/{code}/parentHierarchy")
	public Response getParents(@Context SecurityContext sc, @PathParam("code") String code) throws Exception {
		super.setLogbackMarkers(sc);
		// userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load);

		List<ConceptEntity> concepts = EkbManager.getParents(code);
		List<JsonCodeSetValue> ret = concepts.stream().map(JsonCodeSetValue::new).collect(Collectors.toList());

		clearLogbackMarkers();

		return Response
				.ok()
				.entity(ret)
				.build();
	}}
