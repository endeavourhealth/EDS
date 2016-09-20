package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.core.security.SecurityUtils;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.coreui.framework.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.net.URI;
import java.net.URLEncoder;

@Path("/user")
public final class UserEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(UserEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.User);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/details")
    public Response userDetails(@Context SecurityContext sc) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Level", "Details");
        return Response
                .ok()
                .entity(SecurityUtils.getCurrentUser(sc))
                .build();
    }

    @GET
    @Path("/account")
    public Response userAccount(@Context SecurityContext sc) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Level", "Account");

        String url = String.format(ConfigService.instance().getAuthConfig().getAuthServerUrl() + "/realms/%s/account",
                SecurityUtils.getKeycloakSecurityContext(sc).getRealm());

        return Response
                .seeOther(new URI(url))
                .build();
    }

    @GET
    @Path("/logout")
    public Response logout(@Context SecurityContext sc) throws Exception {

        LOG.info("Logout: {}", SecurityUtils.getCurrentUser(sc));

        String redirectUrl = URLEncoder.encode(ConfigService.instance().getAppConfig().getAppUrl() + "/api/user/details", "UTF-8");

        String url = String.format(ConfigService.instance().getAuthConfig().getAuthServerUrl() + "/realms/%s/protocol/openid-connect/logout?redirect_uri=%s",
                SecurityUtils.getKeycloakSecurityContext(sc).getRealm(), redirectUrl);

        return Response
                .seeOther(new URI(url))
                .build();
    }
}