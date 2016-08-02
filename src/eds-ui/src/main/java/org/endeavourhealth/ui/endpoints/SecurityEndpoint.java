package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.admin.OrganisationRepository;
import org.endeavourhealth.core.data.admin.UserRepository;
import org.endeavourhealth.core.data.admin.models.EndUser;
import org.endeavourhealth.core.data.admin.models.EndUserPwd;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.core.data.admin.models.OrganisationEndUserLink;
import org.endeavourhealth.ui.email.EmailProvider;
import org.endeavourhealth.ui.framework.security.PasswordHash;
import org.endeavourhealth.ui.framework.security.SecurityConfig;
import org.endeavourhealth.ui.framework.security.TokenHelper;
import org.endeavourhealth.ui.framework.security.Unsecured;
import org.endeavourhealth.ui.json.*;
import org.endeavourhealth.ui.database.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.Instant;
import java.util.*;

@Path("/security")
public final class SecurityEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityEndpoint.class);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/login")
    @Unsecured
    public Response login(@Context SecurityContext sc, JsonEndUser personParameters) throws Exception {
        super.setLogbackMarkers(sc);

        String email = personParameters.getUsername();
        String password = personParameters.getPassword();

        LOG.trace("Login for {}", email);

        if (email == null
                || email.length() == 0
                || password == null
                || password.length() == 0) {
            throw new BadRequestException("Missing username or password in request");
        }

        UserRepository userRepository = new UserRepository();

        EndUser user = userRepository.getEndUserByEmail(email);

        if (user == null) {
            throw new NotAuthorizedException("No user found for email");
        }

        //retrieve the most recent password for the person
        UUID uuid = user.getId();
        EndUserPwd pwd = userRepository.getEndUserPwdById(uuid); // TODO: Check for password expired

        if (pwd == null) {
            throw new NotAuthorizedException("No active password for email");
        }

        //validate the password
        String hash = pwd.getPwdHash();
        if (!PasswordHash.validatePassword(password, hash)) {

            int failedAttempts = pwd.getFailedAttempts();
            failedAttempts ++;
            pwd.setFailedAttempts(failedAttempts);
            if (failedAttempts >= SecurityConfig.MAX_FAILED_PASSWORD_ATTEMPTS) {
                pwd.setDtExpired(new Date());
            }

            userRepository.updateEndUserPwd(pwd);

            throw new NotAuthorizedException("Invalid password");
        }

        Boolean mustChangePassword = null;
        if (pwd.getIsOneTimeUse()) {
            pwd.setDtExpired(new Date());
            mustChangePassword = Boolean.TRUE;
        }

        pwd.setFailedAttempts(0);

        userRepository.updateEndUserPwd(pwd);

        JsonOrganisationList ret = ret = new JsonOrganisationList();
        Organisation orgToAutoSelect = null;
        Boolean isAdminForAutoSelect = null;

        //now see what organisations the person can access
        //if the person is a superUser, then we want to now prompt them to log on to ANY organisation
        OrganisationRepository organisationRepository = new OrganisationRepository();

        if (user.getIsSuperUser()) {

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
        ret.setUser(new JsonEndUser(user, null, mustChangePassword));

        NewCookie cookie = TokenHelper.createTokenAsCookie(user, orgToAutoSelect, isAdminForAutoSelect);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .cookie(cookie)
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/selectOrganisation")
    public Response selectOrganisation(@Context SecurityContext sc, JsonOrganisation orgParameters) throws Exception {
        super.setLogbackMarkers(sc);

        UUID uuid = getEndUserUuidFromToken(sc);
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

        //issue a new cookie, with the newly selected organisation
        NewCookie cookie = TokenHelper.createTokenAsCookie(endUser, org, isAdmin);

        //return the full org details and the user's role at this place
        JsonOrganisation ret = new JsonOrganisation(org, isAdmin);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .cookie(cookie)
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/logoff")
    @Unsecured
    public Response logoff(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        LOG.trace("Logoff");

        //replace the cookie on the client with an empty one
        NewCookie cookie = TokenHelper.createTokenAsCookie(null, null, false);

        clearLogbackMarkers();

        return Response
                .ok()
                .cookie(cookie)
                .build();
    }



}