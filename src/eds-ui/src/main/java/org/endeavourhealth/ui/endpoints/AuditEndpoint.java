package org.endeavourhealth.ui.endpoints;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.core.data.audit.models.UserEvent;
import org.endeavourhealth.core.data.config.ConfigurationRepository;
import org.endeavourhealth.core.security.KeycloakConfigUtils;
import org.endeavourhealth.core.security.SecurityUtils;
import org.endeavourhealth.core.security.keycloak.client.KeycloakClient;
import org.endeavourhealth.ui.json.JsonUserEvent;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/audit")
public final class AuditEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(AuditEndpoint.class);
    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Audit);

    private KeycloakDeployment keycloakDeployment;
    private String keycloakRealm;
    private Keycloak client;

    private boolean initKeycloakAdmin = false;

    private void initKeycloakAdminClient() {

        // get the Endeavour realm name
        keycloakDeployment = KeycloakConfigUtils.getConfig(ConfigurationRepository.KEYCLOAK_CONFIG);
        keycloakRealm = keycloakDeployment.getRealm();

        // get config details for the realm admin client

        // TODO: put this in config files !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        String adminClientUsername = "eds-ui";
        String adminClientPassword = "bd285adbc36842d7a27088e93c36c13e29ed69fa63a6";

        // build the admin client
        KeycloakClient.init(keycloakDeployment.getAuthServerBaseUrl(),
                "master",
                adminClientUsername,
                adminClientPassword,
                "admin-cli");

        try {
            LOG.trace("Keycloak token = '{}'", KeycloakClient.instance().getToken().getToken());
        } catch (IOException e) {
            LOG.trace("Keycloak token = 'null'");
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
            HttpGet httpGet = new HttpGet(keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + keycloakDeployment.getRealm() + "/users");
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
        @QueryParam("userId") UUID userId,
        @QueryParam("organisationId") UUID organisationId,
        @QueryParam("module") String module,
        @QueryParam("subModule") String subModule,
        @QueryParam("action") String action) throws Exception {
        super.setLogbackMarkers(sc);

        // TODO: remove this example code!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        List<UserRepresentation> users = keycloakGetUsers("*", 0, 10);
        for(UserRepresentation user : users) {
            LOG.info("{}: {} {} ({})", user.getId(), user.getFirstName(), user.getLastName(), user.getUsername());
        }
        // TODO: remove this example code!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "User Id", userId,
            "Organisation Id", organisationId,
            "Module", module,
            "Sub Module", subModule,
            "Action", action);

        LOG.trace("getAudit");

        Iterable<UserEvent> audit = userAudit.load(userId, organisationId, module, subModule, action);

        List<JsonUserEvent> jsonAudit = new ArrayList<>();
        for (UserEvent event : audit) {
            jsonAudit.add(new JsonUserEvent(event));
        }

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(jsonAudit)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/modules")
    public Response getAuditModules(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Data", "Modules");

        LOG.trace("getAuditModules");

        clearLogbackMarkers();
        return Response
            .ok()
            .entity(userAudit.getModuleList())
            .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/submodules")
    public Response getAuditSubModules(@Context SecurityContext sc, @QueryParam("module") String module) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Data", "SubModules",
            "Module", module);

        LOG.trace("getAuditSubodules");

        clearLogbackMarkers();
        return Response
            .ok()
            .entity(userAudit.getSubModuleList(module))
            .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/actions")
    public Response getAuditActions(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Data", "Actions");

        LOG.trace("getAuditActions");

        clearLogbackMarkers();
        return Response
            .ok()
            .entity(userAudit.getActionList())
            .build();
    }
}
