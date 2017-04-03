package org.endeavourhealth.coreui.endpoints;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.core.data.audit.models.UserEvent;
import org.endeavourhealth.common.security.KeycloakConfigUtils;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.keycloak.client.KeycloakClient;
import org.endeavourhealth.coreui.json.JsonEndUser;
import org.endeavourhealth.coreui.json.JsonUserEvent;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Api(value = "Audit", authorizations = {
		@Authorization(value="oauth", scopes = {})
})
@Path("/audit")
public final class AuditEndpoint extends AbstractEndpoint {
	private static final Logger LOG = LoggerFactory.getLogger(AuditEndpoint.class);
	private static final UserAuditRepository userAuditRepository = new UserAuditRepository(AuditModule.EdsUiModule.Audit);

	private String keycloakRealm;
	private String authServerBaseUrl;

	private boolean initKeycloakAdmin = false;

	private void initKeycloakAdminClient() {

		// get the Endeavour realm name
		KeycloakDeployment keycloakDeployment = KeycloakConfigUtils.getDeployment();
		keycloakRealm = keycloakDeployment.getRealm();
		authServerBaseUrl = KeycloakConfigUtils.initialize();

		try {
			LOG.trace("Keycloak token = '{}'", KeycloakClient.instance().getToken().getToken());
		} catch (IOException e) {
			LOG.trace("Keycloak token = 'null'", e);
		}

		initKeycloakAdmin = true;
	}

	private List<UserRepresentation> keycloakGetUsers(String search, int offset, int limit) {
		if(!initKeycloakAdmin) {
			initKeycloakAdminClient();
		}

		List<UserRepresentation> users = null;
		ObjectMapper mapper = new ObjectMapper();
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			// The URL is this: http://localhost:9080/auth/admin/realms/endeavour/users?first=0&max=20 (NOTE: paging in the query string)
			HttpGet httpGet = new HttpGet(authServerBaseUrl + "/admin/realms/" + keycloakRealm + "/users");
			httpGet.addHeader(KeycloakClient.instance().getAuthorizationHeader());
			HttpResponse response = httpClient.execute(httpGet);
			users = mapper.readValue(response.getEntity().getContent(), new TypeReference<List<UserRepresentation>>() { });
		} catch (IOException e) {
			LOG.error("Keycloak get users failed", e);

		}
		return users;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/")
	public Response getAudit(
			@Context SecurityContext sc,
			@QueryParam("module") String module,
			@QueryParam("userId") UUID userId,
			@QueryParam("month") Long monthLong,
			@QueryParam("organisationId") UUID organisationId) throws Exception {
		super.setLogbackMarkers(sc);

		Date month = new Date(monthLong);

		userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Audit",
				"User Id", userId,
				"Organisation Id", organisationId,
				"Module", module);

		LOG.trace("getAudit");

		List<UserEvent> events = userAuditRepository.load(module, userId, month, organisationId);

		List<JsonUserEvent> entity = events.stream()
				.map(e -> new JsonUserEvent(e))
				.collect(Collectors.toList());

		AbstractEndpoint.clearLogbackMarkers();
		return Response
				.ok()
				.entity(entity)
				.build();
	}


	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/users")
	public Response getUsers(@Context SecurityContext sc) throws Exception {
		super.setLogbackMarkers(sc);
		userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Users");

		LOG.trace("getUsers");

		List<JsonEndUser> userList = new ArrayList<>();

		List<UserRepresentation> users = keycloakGetUsers("*", 0, 10);
		for(UserRepresentation user : users) {
			userList.add(new JsonEndUser(user));
		}

		AbstractEndpoint.clearLogbackMarkers();
		return Response
				.ok()
				.entity(userList)
				.build();
	}


	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/modules")
	public Response getAuditModules(@Context SecurityContext sc) throws Exception {
		super.setLogbackMarkers(sc);
		userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Modules");

		LOG.trace("getAuditModules");

		AbstractEndpoint.clearLogbackMarkers();
		return Response
				.ok()
				.entity(userAuditRepository.getModuleList())
				.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/submodules")
	public Response getAuditSubModules(@Context SecurityContext sc, @QueryParam("module") String module) throws Exception {
		super.setLogbackMarkers(sc);
		userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"SubModules",
				"Module", module);

		LOG.trace("getAuditSubmodules");

		AbstractEndpoint.clearLogbackMarkers();
		return Response
				.ok()
				.entity(userAuditRepository.getSubModuleList(module))
				.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/actions")
	public Response getAuditActions(@Context SecurityContext sc) throws Exception {
		super.setLogbackMarkers(sc);
		userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Actions");

		LOG.trace("getAuditActions");

		AbstractEndpoint.clearLogbackMarkers();
		return Response
				.ok()
				.entity(userAuditRepository.getActionList())
				.build();
	}
}
