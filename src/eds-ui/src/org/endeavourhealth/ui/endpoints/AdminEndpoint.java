package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.admin.UserRepository;
import org.endeavourhealth.core.data.admin.models.EndUser;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.ui.email.EmailProvider;
import org.endeavourhealth.ui.framework.security.PasswordHash;
import org.endeavourhealth.ui.json.JsonEndUser;
import org.endeavourhealth.ui.json.JsonEndUserList;
import org.endeavourhealth.ui.json.JsonOrganisation;
import org.endeavourhealth.core.data.admin.models.DefinitionItemType;
import org.endeavourhealth.ui.database.DatabaseManager;
import org.endeavourhealth.ui.database.DbAbstractTable;
import org.endeavourhealth.ui.database.administration.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Endpoint for the functions related to managing person and organisation entities
 */
@Path("/admin")
public final class AdminEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(AdminEndpoint.class);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/saveUser")
    public Response saveUser(@Context SecurityContext sc, JsonEndUser userParameters) throws Exception {
        super.setLogbackMarkers(sc);

        //userParameters
        UUID uuid = userParameters.getUuid();
        String email = userParameters.getUsername();
        String title = userParameters.getTitle();
        String forename = userParameters.getForename();
        String surname = userParameters.getSurname();
        Integer permissions = userParameters.getPermissions();
        Boolean isAdmin = userParameters.getAdmin();
        Boolean isSuperUser = userParameters.getSuperUser();
        String password = userParameters.getPassword();

        //until the web client is changed, we need to use the permissions value
        if (isAdmin == null) {
            if (permissions != null) {
                isAdmin = new Boolean(permissions.intValue() == 2);
            }
        }

        if (isSuperUser == null) {
            isSuperUser = new Boolean(false);
        }

        LOG.trace("SavingUser UserUUID {}, Email {} Title {} Forename {} Surname {} IsAdmin {} IsSuperUser {}", uuid, email, title, forename, surname, isAdmin, isSuperUser);

        //if doing anything to a super user, verify the current user is a super-user
        if (isSuperUser.booleanValue()) {
            EndUser user = getEndUserFromSession(sc);
            if (!user.getIsSuperUser()) {
                throw new NotAuthorizedException("Non-super user cannot create or modify super users");
            }
        }

        //validate that the user is amending themselves or is an admin
        EndUser userLoggedOn = getEndUserFromSession(sc);
        if (uuid == null
                || !userLoggedOn.getId().equals(uuid)) {
            if (!isAdminFromSession(sc)) {
                throw new NotAuthorizedException("Must be an admin to create new users or amend others");
            }
        }

        Organisation org = getOrganisationFromSession(sc);
        UUID orgUuid = getOrganisationUuidFromToken(sc);

        List<DbAbstractTable> toSave = new ArrayList<>();

        DbEndUser user = null;
        DbOrganisationEndUserLink link = null;
        Boolean createdNewPerson = null;

        //if the uuid is null, we're creating a new person
        if (uuid == null) {
            //see if we have a person for this email address, that the admin user couldn't see, which we can just use
            user = DbEndUser.retrieveForEmail(email);

            if (isAdmin == null) {
                isAdmin = Boolean.FALSE;
            }

            if (user == null) {
                //if the user doesn't already exist, create it and save to the DB
                createdNewPerson = Boolean.TRUE;

                if (email == null || email.length() == 0) {
                    throw new BadRequestException("Cannot set blank username");
                }
                if (title == null) {
                    //allow a blank title
                    title = "";
                }
                if (forename == null || forename.length() == 0) {
                    throw new BadRequestException("Cannot set blank forename");
                }
                if (surname == null || surname.length() == 0) {
                    throw new BadRequestException("Cannot set blank surname");
                }

                user = new DbEndUser();
                user.assignPrimaryUUid();
                user.setEmail(email);
                user.setTitle(title);
                user.setForename(forename);
                user.setSurname(surname);
                user.setSuperUser(isSuperUser);
                uuid = user.getEndUserUuid();
            }
            //if we're trying to create a new user, but they already exist at another org,
            //then we can just use that same user record and link it to the new organisation
            else {
                createdNewPerson = Boolean.FALSE;

                //validate the name matches what's already on the DB
                if (!user.getForename().equalsIgnoreCase(forename)
                        || !user.getSurname().equalsIgnoreCase(surname)) {
                    throw new BadRequestException("User already exists but with different name");
                }

                //validate the person isn't already a user at our org
                uuid = user.getEndUserUuid();
                link = DbOrganisationEndUserLink.retrieveForOrganisationEndUserNotExpired(orgUuid, uuid);
                if (link != null) {
                    throw new BadRequestException("User already is registered here");
                }
            }

            //create the user/org link for non-superusers only, as superusers don't require them
            if (!isSuperUser.booleanValue()) {
                link = new DbOrganisationEndUserLink();
                link.setOrganisationUuid(orgUuid);
                link.setEndUserUuid(uuid);
                link.setAdmin(isAdmin);
            }
        }
        //if we have a uuid, we're updating an existing person
        else {
            user = DbEndUser.retrieveForUuid(uuid);

            //if we're changing the email, validate that the email isn't already on the DB
            String existingEmail = user.getEmail();
            if (!existingEmail.equals(email)) {
                DbEndUser duplicateEmail = DbEndUser.retrieveForEmail(email);
                if (duplicateEmail != null) {
                    throw new BadRequestException("New email address already in use");
                }
            }

            //we can turn a super-user into a NON-super user, but don't allow going the other way
            if (!user.isSuperUser()
                    && isSuperUser.booleanValue()) {
                throw new BadRequestException("Cannot promote a user to super-user status");
            }

            if (email != null && email.length() > 0) {
                user.setEmail(email);
            }
            if (title != null) {
                user.setTitle(title);
            }
            if (forename != null && forename.length() > 0) {
                user.setForename(forename);
            }
            if (surname != null && surname.length() > 0) {
                user.setSurname(surname);
            }

            user.setSuperUser(isSuperUser);

            //retrieve the link entity, as we may want to change the permissions on there
            link = DbOrganisationEndUserLink.retrieveForOrganisationEndUserNotExpired(orgUuid, uuid);

            //the link will be null if we're a super-user, so that's fine
            if (link != null && isAdmin != null) {
                link.setAdmin(isAdmin);
            }
        }

        toSave.add(user);

        //if we created or changed a link, save it
        if (link != null) {
            toSave.add(link);
        }

        //if a password was supplied, then set or change the password
        // CONVERT: if (password != null) {
        // CONVERT: changePassword(user, userLoggedOn, password, toSave);
        // CONVERT: }

        //if we just updated a person, then we don't want to generate any invite email
        if (createdNewPerson == null) {
            //do nothing
        }
        //if we created a new person, generate the invite email
        else if (createdNewPerson.booleanValue()) {
            // CONVERT: createAndSendInvite(user, org, toSave);
        }
        //if we didn't create a new person, then we don't need them to verify and create
        //a password, but we still want to tell the person that they were given new access
        else {
            // CONVERT: if (!EmailProvider.getInstance().sendNewAccessGrantedEmail(user, org)) {
            // CONVERT: throw new InternalServerErrorException("Failed to send new organisation email");
            // CONVERT: }
        }

        DatabaseManager.db().writeEntities(toSave);

        //return the UUID of the person back to the client
        JsonEndUser ret = new JsonEndUser();
        ret.setUuid(uuid);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private static void changePassword(DbEndUser user, DbEndUser loggedOnUser, String newPwd, List<DbAbstractTable> toSave) throws Exception {

        //validate the user is changing their own password or is an admin
        Boolean oneTimeUse = Boolean.FALSE;
        if (!loggedOnUser.equals(user)) {
            oneTimeUse = Boolean.TRUE; //if an admin resets a password, then it is one-time use
        }

        String hash = PasswordHash.createHash(newPwd);

        //retrieve the most recent password for the person
        UUID uuid = user.getEndUserUuid();
        DbEndUserPwd oldPwd = DbEndUserPwd.retrieveForEndUserNotExpired(uuid);

        //create the new password entity
        DbEndUserPwd p = new DbEndUserPwd();
        p.setEndUserUuid(uuid);
        p.setPwdHash(hash);
        p.setOneTimeUse(oneTimeUse);
        p.setFailedAttempts(new Integer(0));
        toSave.add(p);

        //expire the old password, if there was one
        if (oldPwd != null) {
            oldPwd.setDtExpired(Instant.now());
            toSave.add(oldPwd);
        }
    }

    private static void createAndSendInvite(EndUser user, Organisation org, List<DbAbstractTable> toSave) throws Exception {
        UUID userUuid = user.getId();

        //use a base64 encoded version of a random UUID
        String tokenUuid = UUID.randomUUID().toString();
        String token = Base64.getEncoder().encodeToString(tokenUuid.getBytes());

        DbEndUserEmailInvite invite = new DbEndUserEmailInvite();
        invite.setEndUserUuid(userUuid);
        invite.setUniqueToken(token);
        toSave.add(invite);

        //send the invite email before saving to the DB
        // CONVERT: if (!EmailProvider.getInstance().sendInviteEmail(user, org, token)) {
        // CONVERT: throw new InternalServerErrorException("Failed to send invitation email");
        // CONVERT: }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/deleteUser")
    public Response deleteUser(@Context SecurityContext sc, JsonEndUser userParameters) throws Exception {
        super.setLogbackMarkers(sc);

        //first, verify the user is an admin
        boolean isAdmin = super.isAdminFromSession(sc);
        if (!isAdmin) {
            throw new org.endeavourhealth.ui.framework.exceptions.NotAuthorizedException();
        }

        //userParameters
        UUID userUuid = userParameters.getUuid();

        LOG.trace("DeletingUser UserUUID {}", userUuid);

        UUID currentUserUuid = getEndUserUuidFromToken(sc);
        if (userUuid.equals(currentUserUuid)) {
            throw new BadRequestException("Cannot delete your own account");
        }

        //rather than actually deleting the user record, we mark their link
        //at the current organisation as expired
        UUID orgUuid = getOrganisationUuidFromToken(sc);

        List<DbOrganisationEndUserLink> links = DbOrganisationEndUserLink.retrieveForEndUserNotExpired(userUuid);
        for (int i = 0; i < links.size(); i++) {
            DbOrganisationEndUserLink link = links.get(i);
            if (link.getOrganisationUuid().equals(orgUuid)) {
                link.setDtExpired(Instant.now());
                link.writeToDb();
            }
        }

        clearLogbackMarkers();

        //don't bother returning anything to the client
        return Response
                .ok()
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getUsers")
    public Response getUsers(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        UserRepository userRepository = new UserRepository();

        UUID orgUuid = getOrganisationUuidFromToken(sc);

        LOG.trace("GettingUsers");

        JsonEndUserList ret = new JsonEndUserList();

        //retrieve all users at this organisation
        List<DbOrganisationEndUserLink> links = DbOrganisationEndUserLink.retrieveForOrganisationNotExpired(orgUuid);
        for (int i = 0; i < links.size(); i++) {
            DbOrganisationEndUserLink link = links.get(i);
            UUID endUserUuid = link.getEndUserUuid();
            boolean isAdmin = link.isAdmin();
            EndUser endUser = userRepository.getById(endUserUuid);

            ret.add(endUser, isAdmin);
        }

        //if we're a super-user then we should also include all other super-users in the result
        EndUser user = getEndUserFromSession(sc);
        if (user.getIsSuperUser()) {
            //List<EndUser> superUsers = userRepository.retrieveSuperUsers();
            List<EndUser> superUsers = null; // TODO: convert to cassandra
            for (int i = 0; i < superUsers.size(); i++) {
                EndUser superUser = superUsers.get(i);

                //super-users are always treated as admins
                ret.add(superUser, true);
            }
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    /*private static void testSql() throws Exception {

        int times = 100;
        String uuid = "C8E02710-C380-454C-8776-45E4DD180825";

        LOG.trace("Starting SQL literal test");
        for (int i=0; i<times; i++) {
            String sql = "SELECT * FROM Administration.EndUser WHERE EndUserUuid = '" + uuid + "'";

            Connection conn = DatabaseManager.getConnection();
            Statement s = conn.createStatement();
            s.execute(sql);
            ResultSet rs = s.getResultSet();
            rs.close();
            conn.close();
        }
        LOG.trace("Completed literal test");


        LOG.trace("Starting SQL prepared statement test");
        for (int i=0; i<times; i++) {
            String sql = "SELECT * FROM Administration.EndUser WHERE EndUserUuid = ?";

            Connection conn = DatabaseManager.getConnection();
            PreparedStatement s = conn.prepareStatement(sql);
            s.setString(1, uuid);
            s.execute();
            ResultSet rs = s.getResultSet();
            rs.close();
            conn.close();
        }
        LOG.trace("Completed prepared statement test");
    }*/

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/resendInviteEmail")
    public Response resendInviteEmail(@Context SecurityContext sc, JsonEndUser userParameters) throws Exception {
        super.setLogbackMarkers(sc);

        //first, verify the user is an admin
        boolean isAdmin = super.isAdminFromSession(sc);
        if (!isAdmin) {
            throw new org.endeavourhealth.ui.framework.exceptions.NotAuthorizedException();
        }

        //userParameters
        UUID userUuid = userParameters.getUuid();
        UserRepository userRepository = new UserRepository();

        EndUser user = userRepository.getById(userUuid);

        LOG.trace("ResendingInviteEmail UserUUID {}", userUuid);

        //retrieve any existing invite for this person and mark it as completed,
        //so clicking the link in the old email will no longer work
        List<DbAbstractTable> toSave = new ArrayList<>();

        List<DbEndUserEmailInvite> invites = DbEndUserEmailInvite.retrieveForEndUserNotCompleted(userUuid);
        for (int i = 0; i < invites.size(); i++) {
            DbEndUserEmailInvite invite = invites.get(i);
            invite.setDtCompleted(Instant.now());
            toSave.add(invite);
        }

        //now generate a new invite and send it
        Organisation org = getOrganisationFromSession(sc);
        createAndSendInvite(user, org, toSave);

        DatabaseManager.db().writeEntities(toSave);

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }


}
