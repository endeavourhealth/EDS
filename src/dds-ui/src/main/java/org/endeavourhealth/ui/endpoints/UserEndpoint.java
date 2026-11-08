package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
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

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.User);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="UserEndpoint.GetUserDetails")
    @Path("/details")
    public Response userDetails(@Context SecurityContext sc) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "User Details");
        return Response
                .ok()
                .entity(SecurityUtils.getCurrentUser(sc))
                .build();
    }

    @GET
    @Path("/account")
    @Timed(absolute = true, name="UserEndpoint.GetUserAccount")
    public Response userAccount(@Context SecurityContext sc) throws Exception {
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "User Account");

        String url = String.format(ConfigService.instance().getAuthConfig().getAuthServerUrl() + "/realms/%s/account",
                SecurityUtils.getKeycloakSecurityContext(sc).getRealm());

        return Response
                .seeOther(new URI(url))
                .build();
    }

    @GET
    @Path("/logout")
    @Timed(absolute = true, name="UserEndpoint.Logout")
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