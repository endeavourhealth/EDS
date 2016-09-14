package org.endeavourhealth.ui.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.core.security.OrgRoles;
import org.endeavourhealth.core.security.SecurityUtils;
import org.endeavourhealth.ui.framework.config.ConfigService;
import org.keycloak.KeycloakSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "Security", authorizations = {
        @Authorization(value="oauth", scopes = {})
})
@Path("/security")
public final class SecurityEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Security);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/info")
    @ApiOperation(value = "Returns current user information")
    public Response userInfo(@Context SecurityContext sc) throws Exception {

        super.setLogbackMarkers(sc);

        KeycloakSecurityContext keycloakSecurityContext = SecurityUtils.getKeycloakSecurityContext(sc);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(keycloakSecurityContext.getToken()))
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/info/organisationRoles")
    @ApiOperation(value = "Returns current user's roles in the current organisation")
    public Response userInfoOrganisationRoles(@Context SecurityContext sc,
                                              @Context ContainerRequestContext containerRequestContext,
                                              @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The currently selected organisation") @HeaderParam(value = OrgRoles.HEADER_ORGANISATION_ID) String headerOrgId) throws Exception {

        super.setLogbackMarkers(sc);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        String organisationId = SecurityUtils.getCurrentUserOrganisationId(containerRequestContext);
        Map<String,Object> response = new HashMap<>();
        response.put("organisationId", organisationId);
        List<String> orgRoles = SecurityUtils.getOrganisationRoles(sc, organisationId);
        response.put("orgRoles", orgRoles);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response))
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/info/organisations")
    @ApiOperation(value = "Returns current user's available organisations")
    public Response userInfoOrganisations(@Context SecurityContext sc,
                                          @Context ContainerRequestContext containerRequestContext) throws Exception {

        super.setLogbackMarkers(sc);

        // TODO: complete implementation, currently only based on information in Keycloak
        Map<String, List<String>> orgRoles = SecurityUtils.getOrganisationRoles(sc);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(orgRoles)
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/logoff")
    @ApiOperation(value = "Redirects the current user to the single sign-off URL")
    public Response logoff(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        LOG.trace("Logoff");

        String redirectUrl = URLEncoder.encode(ConfigService.instance().getAppConfig().getAppUrl() + "/api/user/details", "UTF-8");

        String url = String.format(ConfigService.instance().getAuthConfig().getAuthServerUrl() + "/realms/%s/protocol/openid-connect/logout?redirect_uri=%s",
                SecurityUtils.getKeycloakSecurityContext(sc).getRealm(), redirectUrl);

        clearLogbackMarkers();

        return Response
                .seeOther(new URI(url))
                .build();
    }
}