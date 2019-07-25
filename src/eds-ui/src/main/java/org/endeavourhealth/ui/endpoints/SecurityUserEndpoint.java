package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.security.OrgRoles;
import org.endeavourhealth.common.security.RoleUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.common.security.keycloak.client.KeycloakAdminClient;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.ui.json.security.JsonUser;
import org.endeavourhealth.ui.utility.SecurityUserHelper;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.HashMap;
import java.util.List;

@Api(value = "Security - Users", authorizations = {
        @Authorization(value="oauth", scopes = {})
})
@Path("/security/users")
public final class SecurityUserEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityUserEndpoint.class);

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Security);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.SecurityUserEndpoint.SearchForUsers")
    @RequiresAdmin
    @ApiOperation(value = "Search for users")
    public Response list(@Context SecurityContext sc,
                         @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The currently selected organisation") @HeaderParam(value = OrgRoles.HEADER_ORGANISATION_ID) String headerOrgId,
                         @ApiParam(value="Search terms") @QueryParam("search") String search,
                         @ApiParam(defaultValue = "0", value="Paging offset") @QueryParam("offset") int offset,
                         @ApiParam(defaultValue = "10", value="Paging limit") @QueryParam("limit") int limit
                         ) throws Exception {

        // TODO: audit action

        super.setLogbackMarkers(sc);
        List<UserRepresentation> users = KeycloakAdminClient.instance().realms().users().getUsers(search, offset, limit);
        clearLogbackMarkers();

        return Response
                .ok()
                .entity(users)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.SecurityUserEndpoint.GetUserDetails")
    @Path("/{userId}")
    @RequiresAdmin
    @ApiOperation(value = "Get a user's details")
    public Response get(@Context SecurityContext sc,
                        @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The currently selected organisation") @HeaderParam(value = OrgRoles.HEADER_ORGANISATION_ID) String headerOrgId,
                        @ApiParam(value="User id") @PathParam(value = "userId") String userId) throws Exception {

        // TODO: audit action

        if(StringUtils.isBlank(userId)) {
            throw new org.endeavourhealth.ui.framework.exceptions.NotFoundException("Please specify a user id");
        }

        super.setLogbackMarkers(sc);

        UserRepresentation user = KeycloakAdminClient.instance().realms().users().getUser(userId);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(user)
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.SecurityUserEndpoint.CreateUser")
    @RolesAllowed({ "eds_admin" })
    @ApiOperation(value = "Create a user")
    public Response create(@Context SecurityContext sc,
                           @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The currently selected organisation") @HeaderParam(value = OrgRoles.HEADER_ORGANISATION_ID) String headerOrgId,
                           JsonUser user) throws Exception {
        // TODO: audit action

        super.setLogbackMarkers(sc);

        // clean up data before creating
        user.setUserId(null);

        UserRepresentation userIn = SecurityUserHelper.toUserRepresentation(user);
        userIn.setEnabled(true);
        userIn.setRequiredActions(Lists.newArrayList("VERIFY_EMAIL", "UPDATE_PASSWORD"));
        userIn.setAttributes(new HashMap<>());

        // create user (without roles)
        UserRepresentation userOut = KeycloakAdminClient.instance().realms().users().postUser(userIn);

        // get roles
        RoleRepresentation roleEDSUser = KeycloakAdminClient.instance().realms().roles().getRole(RoleUtils.ROLE_EDS_USER);

        // update user
        KeycloakAdminClient.instance().realms().users().addRealmRole(userOut.getId(), Lists.newArrayList(roleEDSUser));

        // get updated user
        userOut = KeycloakAdminClient.instance().realms().users().getUser(userOut.getId());

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(userOut)
                .build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.SecurityUserEndpoint.UpdateUser")
    @Path("/{userId}")
    @RequiresAdmin
    @ApiOperation(value = "Update a user")
    public Response update(@Context SecurityContext sc,
                           @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The currently selected organisation") @HeaderParam(value = OrgRoles.HEADER_ORGANISATION_ID) String headerOrgId,
                           @ApiParam(value="User id") @PathParam(value = "userId") String userId,
                           JsonUser user) throws Exception {
        // TODO: audit action

        if(StringUtils.isBlank(userId)) {
            throw new org.endeavourhealth.ui.framework.exceptions.NotFoundException("Please specify a user id");
        }
        if(!user.getUserId().toString().equalsIgnoreCase(userId)) {
            throw new org.endeavourhealth.ui.framework.exceptions.NotFoundException("The user id in the url path must match the body user id");
        }

        super.setLogbackMarkers(sc);

        UserRepresentation userOut = KeycloakAdminClient.instance().realms().users().putUser(SecurityUserHelper.toUserRepresentation(user));

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(userOut)
                .build();
    }

    @DELETE
    @Timed(absolute = true, name="EDS-UI.SecurityUserEndpoint.DeleteUser")
    @Path("/{userId}")
    @RequiresAdmin
    @ApiOperation(value = "Delete a user")
    public Response delete(@Context SecurityContext sc,
                           @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The currently selected organisation") @HeaderParam(value = OrgRoles.HEADER_ORGANISATION_ID) String headerOrgId,
                           @ApiParam(value="User id") @PathParam(value = "userId") String userId) throws Exception {
        // TODO: audit action

        if(StringUtils.isBlank(userId)) {
            throw new org.endeavourhealth.ui.framework.exceptions.NotFoundException("Please specify a user id");
        }

        KeycloakAdminClient.instance().realms().users().deleteUser(userId);

        super.setLogbackMarkers(sc);

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.SecurityUserEndpoint.JoinGroup")
    @Path("/{userId}/groups/{groupId}")
    @RequiresAdmin
    @ApiOperation(value = "Add a user to a group or role-profile group")
    public Response joinGroup(@Context SecurityContext sc,
                              @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The currently selected organisation") @HeaderParam(value = OrgRoles.HEADER_ORGANISATION_ID) String headerOrgId,
                              @ApiParam(value="User id") @PathParam(value = "userId") String userId,
                              @ApiParam(value="Group id") @PathParam(value = "groupId") String groupId) throws Exception {
        // TODO: audit action

        super.setLogbackMarkers(sc);
        KeycloakAdminClient.instance().realms().users().joinGroup(userId, groupId);
        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }

    @DELETE
    @Timed(absolute = true, name="EDS-UI.SecurityUserEndpoint.LeaveGroup")
    @Path("/{userId}/groups/{groupId}")
    @RequiresAdmin
    @ApiOperation(value = "Remove a user from a group or role-profile group")
    public Response leaveGroup(@Context SecurityContext sc,
                               @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The currently selected organisation") @HeaderParam(value = OrgRoles.HEADER_ORGANISATION_ID) String headerOrgId,
                               @ApiParam(value="User id") @PathParam(value = "userId") String userId,
                               @ApiParam(value="Group id") @PathParam(value = "groupId") String groupId) throws Exception {
        // TODO: audit action

        super.setLogbackMarkers(sc);
        KeycloakAdminClient.instance().realms().users().leaveGroup(userId, groupId);
        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }
}