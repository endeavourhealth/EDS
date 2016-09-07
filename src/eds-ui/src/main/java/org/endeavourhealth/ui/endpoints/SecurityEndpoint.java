package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.admin.OrganisationRepository;
import org.endeavourhealth.core.data.admin.UserRepository;
import org.endeavourhealth.core.data.admin.models.EndUser;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.core.data.admin.models.OrganisationEndUserLink;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.ui.framework.config.ConfigService;
import org.endeavourhealth.ui.json.*;
import org.endeavourhealth.core.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URLEncoder;
import java.util.UUID;

@Path("/security")
public final class SecurityEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Security);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/selectOrganisation")
    public Response selectOrganisation(@Context SecurityContext sc, JsonOrganisation orgParameters) throws Exception {
        super.setLogbackMarkers(sc);

        UUID uuid = SecurityUtils.getCurrentUserId(sc);
        UUID orgUuid = orgParameters.getUuid();
        userAudit.save(uuid, getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Organisation Select", orgUuid);

        UserRepository userRepository = new UserRepository();

        EndUser endUser = userRepository.getById(uuid);

        UUID endUserUuid = endUser.getId();

        LOG.trace("Selecting organisationUUID {}", orgUuid);

        //validate the organisation exists
        OrganisationRepository organisationRepository = new OrganisationRepository();

        Organisation org = organisationRepository.getById(orgUuid);
        if (org == null) {
            throw new BadRequestException("Invalid organisation " + orgUuid);
        }

        //validate the person can log on there
        boolean isAdmin = false;

        if (endUser.getIsSuperUser()) {
            //super users are always admin
            isAdmin = true;
        } else {
            OrganisationEndUserLink link = null;
            Iterable<OrganisationEndUserLink> links = organisationRepository.getByUserId(endUserUuid);
            for (OrganisationEndUserLink l : links) {
                if (l.getOrganisationId().equals(orgUuid)) {
                    link = l;
                    break;
                }
            }

            if (link == null) {
                throw new BadRequestException("Invalid organisation " + orgUuid + " or user doesn't have access");
            }

            isAdmin = link.getIsAdmin();
        }

        //return the full org details and the user's role at this place
        JsonOrganisation ret = new JsonOrganisation(org, isAdmin);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/logoff")
    public Response logoff(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        LOG.trace("Logoff");
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Logoff);

        String redirectUrl = URLEncoder.encode(ConfigService.instance().getAppConfig().getAppUrl() + "/api/user/details", "UTF-8");

        String url = String.format(ConfigService.instance().getAuthConfig().getAuthServerUrl() + "/realms/%s/protocol/openid-connect/logout?redirect_uri=%s",
                SecurityUtils.getKeycloakSecurityContext(sc).getRealm(), redirectUrl);


        clearLogbackMarkers();

        return Response
                .seeOther(new URI(url))
                .build();
    }
}