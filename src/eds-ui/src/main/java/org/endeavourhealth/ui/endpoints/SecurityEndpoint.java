package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.admin.OrganisationRepository;
import org.endeavourhealth.core.data.admin.UserRepository;
import org.endeavourhealth.core.data.admin.models.EndUser;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.core.data.admin.models.OrganisationEndUserLink;
import org.endeavourhealth.core.security.RoleUtils;
import org.endeavourhealth.ui.database.DatabaseManager;
import org.endeavourhealth.ui.database.DbAbstractTable;
import org.endeavourhealth.ui.database.administration.DbEndUser;
import org.endeavourhealth.ui.database.administration.DbEndUserEmailInvite;
import org.endeavourhealth.ui.database.administration.DbOrganisationEndUserLink;
import org.endeavourhealth.ui.framework.config.ConfigService;
import org.endeavourhealth.ui.json.*;
import org.endeavourhealth.core.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Path("/security")
public final class SecurityEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityEndpoint.class);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/login")
    public Response login(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        UUID uuid = SecurityUtils.getCurrentUserId(sc);

        JsonOrganisationList ret = ret = new JsonOrganisationList();
        Organisation orgToAutoSelect = null;
        Boolean isAdminForAutoSelect = null;

        //now see what organisations the person can access
        //if the person is a superUser, then we want to now prompt them to log on to ANY organisation
        OrganisationRepository organisationRepository = new OrganisationRepository();

        if (RoleUtils.isSuperUser(sc)) {

            Iterable<Organisation> orgs = organisationRepository.getAll();

            for (Organisation o : orgs) {
                //super-users are assumed to be admins at every organisation
                ret.add(o, new Boolean(true));

                //if there's only one organisation, automatically select it
                orgToAutoSelect = o;
                isAdminForAutoSelect = new Boolean(true);
            }
        }
        //if the person ISN'T a superUser, then we look at the person/org link, so see where they can log on to
        else {
            Iterable<OrganisationEndUserLink> orgLinks = organisationRepository.getByUserId(uuid);
            if (!orgLinks.iterator().hasNext()) {
                throw new NotAuthorizedException("No organisations to log on to");
            }

            for (OrganisationEndUserLink orgLink : orgLinks) {
                UUID orgUuid = orgLink.getOrganisationId();
                Organisation o = organisationRepository.getById(orgUuid);
                Boolean isAdmin = new Boolean(orgLink.getIsAdmin());
                ret.add(o, isAdmin);

                //if there's only one organisation, automatically select it
                orgToAutoSelect = o;
                isAdminForAutoSelect = new Boolean(isAdmin);
            }
        }

        //set the user details in the return object as well
        EndUser user = SecurityUtils.getCurrentUser(sc);
        ret.setUser(new JsonEndUser(user, null, false));

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/selectOrganisation")
    public Response selectOrganisation(@Context SecurityContext sc, JsonOrganisation orgParameters) throws Exception {
        super.setLogbackMarkers(sc);

        UUID uuid = SecurityUtils.getCurrentUserId(sc);
        UserRepository userRepository = new UserRepository();

        EndUser endUser = userRepository.getById(uuid);

        UUID endUserUuid = endUser.getId();

        //the only parameter is the org UUID
        UUID orgUuid = orgParameters.getUuid();

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

        String redirectUrl = URLEncoder.encode(ConfigService.instance().getAppConfig().getAppUrl() + "/api/user/details", "UTF-8");

        String url = String.format(ConfigService.instance().getAuthConfig().getAuthServerUrl() + "realms/%s/protocol/openid-connect/logout?redirect_uri=%s",
                SecurityUtils.getKeycloakSecurityContext(sc).getRealm(), redirectUrl);

        clearLogbackMarkers();

        return Response
                .seeOther(new URI(url))
                .build();
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/setPasswordFromInviteToken")
    public Response setPasswordFromInviteToken(@Context SecurityContext sc, JsonEmailInviteParameters parameters) throws Exception {
        super.setLogbackMarkers(sc);

        String token = parameters.getToken();
        String password = parameters.getPassword();

        LOG.trace("SettingPasswordFromInviteEmail");

        //find the invite for the token
        DbEndUserEmailInvite invite = DbEndUserEmailInvite.retrieveForToken(token);
        if (invite == null) {
            throw new BadRequestException("No invite found for token");
        }


        //now we've correctly set up the new password for the user, we can delete the invite
        invite.setDtCompleted(Instant.now());
        invite.writeToDb();

        UUID userUuid = SecurityUtils.getCurrentUserId(sc);


        //retrieve the link entity for the org and person
        UUID orgUuid = getOrganisationUuidFromToken(sc);
        DbOrganisationEndUserLink link = DbOrganisationEndUserLink.retrieveForOrganisationEndUserNotExpired(orgUuid, userUuid);
        boolean isAdmin = link.isAdmin();

        //create cookie
        DbEndUser user = DbEndUser.retrieveForUuid(userUuid);
        Organisation org = getOrganisationFromSession(sc);

        //NewCookie cookie = TokenHelper.createTokenAsCookie(user, org, isAdmin);
        NewCookie cookie = null; // TODO: to be converted to Cassandra

        clearLogbackMarkers();

        return Response
                .ok()
                .cookie(cookie)
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/sendPasswordForgottenEmail")
    public Response sendPasswordForgottenEmail(@Context SecurityContext sc, JsonUserEmail parameters) throws Exception {
        super.setLogbackMarkers(sc);

        String email = parameters.getEmail();

        LOG.trace("sendPasswordForgottenEmail {}", email);

        UserRepository userRepository = new UserRepository();

        EndUser user = userRepository.getEndUserByEmail(email);
        if (user != null) {

            UUID userUuid = user.getId();

            //the email needs to be linked to an organisation, so just choose one the user can access
            List<DbOrganisationEndUserLink> orgLinks = DbOrganisationEndUserLink.retrieveForEndUserNotExpired(userUuid);
            if (!orgLinks.isEmpty()) {
                DbOrganisationEndUserLink firstLink = orgLinks.get(0);
                UUID orgUuid = firstLink.getOrganisationUuid();
                OrganisationRepository organisationRepository = new OrganisationRepository();

                Organisation org = organisationRepository.getById(orgUuid);

                List<DbAbstractTable> toSave = new ArrayList<>();

                //expire any existing email invite records, so old tokens won't work
                List<DbEndUserEmailInvite> invites = DbEndUserEmailInvite.retrieveForEndUserNotCompleted(userUuid);
                for (int i = 0; i < invites.size(); i++) {
                    DbEndUserEmailInvite invite = invites.get(i);
                    invite.setDtCompleted(Instant.now());
                    toSave.add(invite);
                }

                //use a base64 encoded version of a random UUID
                String tokenUuid = UUID.randomUUID().toString();
                String token = Base64.getEncoder().encodeToString(tokenUuid.getBytes());

                //now generate a new invite and send it
                DbEndUserEmailInvite invite = new DbEndUserEmailInvite();
                invite.setEndUserUuid(userUuid);
                invite.setUniqueToken(token);
                toSave.add(invite);

                //send the email. If the send fails, it'll be logged on the server, but don't return any failure
                //indication to the client, so we don't give away whether the email existed or not
                // TO CONVERT: if (EmailProvider.getInstance().sendInviteEmail(user, org, token)) {
                    //only save AFTER we've successfully send the invite email
                    DatabaseManager.db().writeEntities(toSave);
                //}
            }
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }

}