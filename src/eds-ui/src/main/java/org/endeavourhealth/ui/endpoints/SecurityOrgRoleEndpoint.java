package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.common.security.OrgRoles;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.common.security.keycloak.client.KeycloakAdminClient;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.ui.json.security.JsonKeycloakUser;
import org.endeavourhealth.ui.json.security.JsonOrgRole;
import org.endeavourhealth.ui.json.security.JsonRole;
import org.endeavourhealth.ui.utility.SecurityGroupHelper;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Api(value = "Security - Organisation Role Groups", authorizations = {
        @Authorization(value="oauth", scopes = {})
})
@Path("/security/orgRoleGroups/{organisationId}")
@Metrics(registry = "EdsRegistry")
public final class SecurityOrgRoleEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityOrgRoleEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Security);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.SecurityOrgRoleEndpoint.GetAllRoleProfileGroups")
    @Path("")
    @RequiresAdmin
    @ApiOperation(value = "Get all role profile groups for the currently selected organisation")
    public Response list(@Context SecurityContext sc,
                         @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The organisation id to view a list of roles") @PathParam(value = "organisationId") String organisationId,
                         @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The currently selected organisation") @HeaderParam(value = OrgRoles.HEADER_ORGANISATION_ID) String headerOrgId ) throws Exception {

        // TODO: audit action

        if(StringUtils.isBlank(organisationId)) {
            throw new org.endeavourhealth.ui.framework.exceptions.NotFoundException("Please specify an organisation id");
        }

        super.setLogbackMarkers(sc);

        List<GroupRepresentation> groups = KeycloakAdminClient.instance().realms().groups().getGroups();

        // TODO: the performance of this will be awful! Need to add a custom REST resource to Keycloak!
        List<GroupRepresentation> groups2 = new ArrayList<>();
        for(GroupRepresentation g : groups) {
            groups2.add(KeycloakAdminClient.instance().realms().groups().getGroup(g.getId()));
        }

        List<JsonOrgRole> roles = groups2.stream()
                .filter(g -> {
                    String groupOrgId = SecurityGroupHelper.getOrganisationId(g);
                    return (groupOrgId != null && groupOrgId.equalsIgnoreCase(organisationId));
                })
                .map(g -> SecurityGroupHelper.toJsonOrgRole(g))
                .collect(Collectors.toList());

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(roles)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.SecurityOrgRoleEndpoint.GetRoleProfileGroup")
    @Path("/{orgRoleId}")
    @RequiresAdmin
    @ApiOperation(value = "Get a role profile group for the currently selected organisation")
    public Response get(@Context SecurityContext sc,
                        @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The organisation id to view a list of roles") @PathParam(value = "organisationId") String organisationId,
                        @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The currently selected organisation") @HeaderParam(value = OrgRoles.HEADER_ORGANISATION_ID) String headerOrgId,
                        @ApiParam(value="Role id") @PathParam(value = "orgRoleId") String orgRoleId) throws Exception {

        // TODO: audit action

        if(StringUtils.isBlank(organisationId)) {
            throw new org.endeavourhealth.ui.framework.exceptions.NotFoundException("Please specify an organisation id");
        }
        if(StringUtils.isBlank(orgRoleId)) {
            throw new org.endeavourhealth.ui.framework.exceptions.NotFoundException("Please specify an organisation role id");
        }

        super.setLogbackMarkers(sc);

        // TODO: improve performance through custom REST resource
        GroupRepresentation group = KeycloakAdminClient.instance().realms().groups().getGroup(orgRoleId);
        List<RoleRepresentation> roles = KeycloakAdminClient.instance().realms().groups().getRealmRoleMappingIds(orgRoleId);
        List<String> roleIds = roles.stream().map(r -> r.getId()).collect(Collectors.toList());
        group.setRealmRoles(roleIds);
        JsonOrgRole role = SecurityGroupHelper.toJsonOrgRole(group);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(role)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.SecurityOrgRoleEndpoint.GetEffectiveRoleProfileGroup")
    @Path("/{orgRoleId}/effective")
    @RequiresAdmin
    @ApiOperation(value = "Get effective roles for a role profile group for the currently selected organisation")
    public Response getEffective(@Context SecurityContext sc,
                        @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The organisation id to view a list of roles") @PathParam(value = "organisationId") String organisationId,
                        @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The currently selected organisation") @HeaderParam(value = OrgRoles.HEADER_ORGANISATION_ID) String headerOrgId,
                        @ApiParam(value="Role id") @PathParam(value = "orgRoleId") String orgRoleId) throws Exception {

        // TODO: audit action

        if(StringUtils.isBlank(organisationId)) {
            throw new org.endeavourhealth.ui.framework.exceptions.NotFoundException("Please specify an organisation id");
        }
        if(StringUtils.isBlank(orgRoleId)) {
            throw new org.endeavourhealth.ui.framework.exceptions.NotFoundException("Please specify an organisation role id");
        }
        validateOrgId(organisationId, orgRoleId);

        super.setLogbackMarkers(sc);

        List<RoleRepresentation> roleRepresentations = KeycloakAdminClient.instance().realms().groups().getEffectiveRealmRoleMappingIds(orgRoleId);
        List<JsonRole> roles = roleRepresentations.stream().map(r -> new JsonRole(UUID.fromString(r.getId()), r.getName(), r.getDescription(), r.isComposite())).collect(Collectors.toList());

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(roles)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.SecurityOrgRoleEndpoint.GetRoleProfileGroupMembers")
    @Path("/{orgRoleId}/members")
    @RequiresAdmin
    @ApiOperation(value = "Get members of a role profile group for the currently selected organisation")
    public Response getMembers(@Context SecurityContext sc,
                        @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The organisation id to view a list of roles") @PathParam(value = "organisationId") String organisationId,
                        @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The currently selected organisation") @HeaderParam(value = OrgRoles.HEADER_ORGANISATION_ID) String headerOrgId,
                        @ApiParam(defaultValue = "0", value="Paging offset") @QueryParam("offset") int offset,
                        @ApiParam(defaultValue = "10", value="Paging limit") @QueryParam("limit") int limit,
                        @ApiParam(value="Role id") @PathParam(value = "orgRoleId") String orgRoleId) throws Exception {


        // TODO: audit action

        if(StringUtils.isBlank(organisationId)) {
            throw new org.endeavourhealth.ui.framework.exceptions.NotFoundException("Please specify an organisation id");
        }
        if(StringUtils.isBlank(orgRoleId)) {
            throw new org.endeavourhealth.ui.framework.exceptions.NotFoundException("Please specify an organisation role id");
        }
        validateOrgId(organisationId, orgRoleId);


        super.setLogbackMarkers(sc);

        List<UserRepresentation> users = KeycloakAdminClient.instance().realms().groups().getGroupMembers(orgRoleId, offset, limit);
        List<JsonKeycloakUser> usersOut = SecurityGroupHelper.toJsonKeycloakUserList(users);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(usersOut)
                .build();
    }

    private void validateOrgId(@ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value = "The organisation id to view a list of roles") @PathParam(value = "organisationId") String organisationId, @ApiParam(value = "Role id") @PathParam(value = "orgRoleId") String orgRoleId) throws org.endeavourhealth.ui.framework.exceptions.NotFoundException {
        // only list the users if the organisationId in the path matches the group's organisation id
        String groupOrgId = SecurityGroupHelper.getOrganisationId(KeycloakAdminClient.instance().realms().groups().getGroup(orgRoleId));
        if(!groupOrgId.equalsIgnoreCase(organisationId)) {
            throw new org.endeavourhealth.ui.framework.exceptions.NotFoundException("Organisation role id not associated with organisation specified.");
        }
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.SecurityOrgRoleEndpoint.CreateRoleProfileGroup")
    @Path("")
    @RequiresAdmin
    @ApiOperation(value = "Create a role profile group for the currently selected organisation")
    public Response create(@Context SecurityContext sc,
                           @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The organisation id to view a list of roles") @PathParam(value = "organisationId") String organisationId,
                           @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The currently selected organisation") @HeaderParam(value = OrgRoles.HEADER_ORGANISATION_ID) String headerOrgId,
                           JsonOrgRole orgRole) throws Exception {
        // TODO: audit action

        super.setLogbackMarkers(sc);

        // clean up data before creating
        orgRole.setOrgRoleId(null);
        orgRole.setOrganisationId(UUID.fromString(organisationId));

        List<RoleRepresentation> realmRoles = KeycloakAdminClient.instance().realms().roles().getRealmRoles();
        GroupRepresentation groupRepresentation = SecurityGroupHelper.toGroupRepresentation(orgRole);
        GroupRepresentation groupOut = KeycloakAdminClient.instance().realms().groups().postGroup(groupRepresentation);
        List<RoleRepresentation> roles = groupRepresentation.getRealmRoles().stream()
                .map(r -> realmRoles.stream().filter(f -> f.getId().equalsIgnoreCase(r)).findFirst().get())
                .collect(Collectors.toList());
        KeycloakAdminClient.instance().realms().groups().addRealmRoleMapping(groupOut.getId(), roles);
        groupOut.setRealmRoles(groupRepresentation.getRealmRoles());
        JsonOrgRole roleOut = SecurityGroupHelper.toJsonOrgRole(groupOut);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(roleOut)
                .build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.SecurityOrgRoleEndpoint.UpdateRoleProfileGroup")
    @Path("/{orgRoleId}")
    @RequiresAdmin
    @ApiOperation(value = "Update a role profile group for the currently selected organisation")
    public Response update(@Context SecurityContext sc,
                           @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The organisation id to view a list of roles") @PathParam(value = "organisationId") String organisationId,
                           @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The currently selected organisation") @HeaderParam(value = OrgRoles.HEADER_ORGANISATION_ID) String headerOrgId,
                           @ApiParam(value="Role id") @PathParam(value = "orgRoleId") String orgRoleId,
                           JsonOrgRole orgRole) throws Exception {
        // TODO: audit action

        if(StringUtils.isBlank(organisationId)) {
            throw new org.endeavourhealth.ui.framework.exceptions.NotFoundException("Please specify an organisation id");
        }
        if(orgRole.getOrgRoleId() == null) {
            throw new org.endeavourhealth.ui.framework.exceptions.NotFoundException("Please specify an organisation id in body");
        }
        if(!orgRole.getOrganisationId().toString().equalsIgnoreCase(organisationId)) {
            throw new org.endeavourhealth.ui.framework.exceptions.NotFoundException("The organisation id in the url path must match the body organisation id");
        }

        super.setLogbackMarkers(sc);

        List<RoleRepresentation> realmRoles = KeycloakAdminClient.instance().realms().roles().getRealmRoles();

        List<RoleRepresentation> origRoles = KeycloakAdminClient.instance().realms().groups().getRealmRoleMappingIds(orgRoleId);
        List<String> origRoleIds = origRoles.stream().map(r -> r.getId()).collect(Collectors.toList());
        List<RoleRepresentation> rolesOrig = origRoleIds.stream()
                .map(r -> realmRoles.stream().filter(f -> f.getId().equalsIgnoreCase(r)).findFirst().get())
                .collect(Collectors.toList());

        GroupRepresentation groupRepresentation = SecurityGroupHelper.toGroupRepresentation(orgRole);
        GroupRepresentation groupOut = KeycloakAdminClient.instance().realms().groups().putGroup(groupRepresentation);

        KeycloakAdminClient.instance().realms().groups().deleteRealmRoleMapping(groupOut.getId(), rolesOrig);

        List<RoleRepresentation> roles = groupRepresentation.getRealmRoles().stream()
                .map(r -> realmRoles.stream().filter(f -> f.getId().equalsIgnoreCase(r)).findFirst().get())
                .collect(Collectors.toList());
        KeycloakAdminClient.instance().realms().groups().addRealmRoleMapping(groupOut.getId(), roles);
        groupOut.setRealmRoles(groupRepresentation.getRealmRoles());

        JsonOrgRole roleOut = SecurityGroupHelper.toJsonOrgRole(groupOut);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(roleOut)
                .build();
    }

    @DELETE
    @Timed(absolute = true, name="EDS-UI.SecurityOrgRoleEndpoint.DeleteRoleProfileGroup")
    @Path("/{orgRoleId}")
    @RequiresAdmin
    @ApiOperation(value = "Delete a role profile group for the currently selected organisation")
    public Response delete(@Context SecurityContext sc,
                           @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The organisation id to view a list of roles") @PathParam(value = "organisationId") String organisationId,
                           @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The currently selected organisation") @HeaderParam(value = OrgRoles.HEADER_ORGANISATION_ID) String headerOrgId,
                           @ApiParam(value="Role id") @PathParam(value = "orgRoleId") String orgRoleId) throws Exception {
        // TODO: audit action

        if(StringUtils.isBlank(organisationId)) {
            throw new org.endeavourhealth.ui.framework.exceptions.NotFoundException("Please specify an organisation id");
        }

        KeycloakAdminClient.instance().realms().groups().deleteGroup(orgRoleId);

        super.setLogbackMarkers(sc);

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }
}