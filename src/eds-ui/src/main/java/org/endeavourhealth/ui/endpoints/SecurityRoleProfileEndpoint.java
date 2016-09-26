package org.endeavourhealth.ui.endpoints;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.core.security.OrgRoles;
import org.endeavourhealth.core.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.security.keycloak.client.KeycloakAdminClient;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.ui.json.security.JsonRole;
import org.endeavourhealth.ui.json.security.JsonRoleProfile;
import org.endeavourhealth.ui.utility.SecurityRoleHelper;
import org.keycloak.representations.idm.RoleRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.stream.Collectors;

@Api(value = "Security - Role Profiles", authorizations = {
        @Authorization(value="oauth", scopes = {})
})
@Path("/security/roleProfiles")
public final class SecurityRoleProfileEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityRoleProfileEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Security);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("availableRoles")
    @RequiresAdmin
    @ApiOperation(value = "List roles that can be added to role profiles")
    public Response listAvailableRoles(@Context SecurityContext sc,
                                       @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The currently selected organisation") @HeaderParam(value = OrgRoles.HEADER_ORGANISATION_ID) String headerOrgId) throws Exception {

        // TODO: audit action

        super.setLogbackMarkers(sc);

        List<RoleRepresentation> rolesSource = KeycloakAdminClient.instance().realms().roles().getRealmRoles();

        List<JsonRole> roles = rolesSource.stream()
                .filter(r -> r.getName().startsWith(SecurityRoleHelper.ROLE_PREFIX_PREFIX))
                .map(r -> SecurityRoleHelper.toJsonRole(r))
                .sorted((o1, o2) -> o1.getName().compareTo(o2.getName()))
                .collect(Collectors.toList());

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(roles)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("")
    @RequiresAdmin
    @ApiOperation(value = "List role profiles")
    public Response list(@Context SecurityContext sc,
                         @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The currently selected organisation") @HeaderParam(value = OrgRoles.HEADER_ORGANISATION_ID) String headerOrgId) throws Exception {

        // TODO: audit action

        super.setLogbackMarkers(sc);

        List<RoleRepresentation> rolesSource = KeycloakAdminClient.instance().realms().roles().getRealmRoles();

        // TODO: add custom REST resource, performance will be awful!
        List<JsonRoleProfile> roles = rolesSource.stream()
                .filter(r -> {
                    return r.getName().startsWith(SecurityRoleHelper.ROLE_PROFILE_PREFIX_PREFIX);
                })
                .map(r -> {
                        List<RoleRepresentation> composites = KeycloakAdminClient.instance().realms().roles().composites().getComposites(r.getName());
                        return SecurityRoleHelper.toJsonRoleProfile(r, composites);
                    }
                )
                .collect(Collectors.toList());

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(roles)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{role-profile-name}")
    @RequiresAdmin
    @ApiOperation(value = "Get a role profile")
    public Response get(@Context SecurityContext sc,
                        @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The currently selected organisation") @HeaderParam(value = OrgRoles.HEADER_ORGANISATION_ID) String headerOrgId,
                        @ApiParam(value="Role profile id") @PathParam(value = "role-profile-name") String orgRoleId) throws Exception {

        // TODO: audit action

        if(StringUtils.isBlank(orgRoleId)) {
            throw new org.endeavourhealth.ui.framework.exceptions.NotFoundException("Please specify an role profile id");
        }

        super.setLogbackMarkers(sc);

        RoleRepresentation role = KeycloakAdminClient.instance().realms().roles().getRole(orgRoleId);
        List<RoleRepresentation> composites = KeycloakAdminClient.instance().realms().roles().composites().getComposites(role.getName());
        JsonRoleProfile roleOut = SecurityRoleHelper.toJsonRoleProfile(role, composites);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(roleOut)
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("")
    @RequiresAdmin
    @ApiOperation(value = "Create a role profile")
    public Response create(@Context SecurityContext sc,
                           @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The currently selected organisation") @HeaderParam(value = OrgRoles.HEADER_ORGANISATION_ID) String headerOrgId,
                           JsonRoleProfile roleIn) throws Exception {
        // TODO: audit action

        super.setLogbackMarkers(sc);

        // clean up data before creating
        roleIn.setRoleProfileId(null);

        RoleRepresentation roleOut = KeycloakAdminClient.instance().realms().roles().postRealmRole(SecurityRoleHelper.toRoleRepresentation(roleIn));
        KeycloakAdminClient.instance().realms().roles().composites().postComposite(roleOut.getName(), SecurityRoleHelper.toCompositeRoleRepresentation(roleIn));

        List<RoleRepresentation> composites = KeycloakAdminClient.instance().realms().roles().composites().getComposites(roleOut.getName());
        JsonRoleProfile roleOut2 = SecurityRoleHelper.toJsonRoleProfile(roleOut, composites);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(roleOut2)
                .build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{role-profile-name}")
    @RequiresAdmin
    @ApiOperation(value = "Update a role profile")
    public Response update(@Context SecurityContext sc,
                           @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The currently selected organisation") @HeaderParam(value = OrgRoles.HEADER_ORGANISATION_ID) String headerOrgId,
                           @ApiParam(value="Role profile name (not the UUID!)") @PathParam(value = "role-profile-name") String roleProfileId,
                           JsonRoleProfile roleProfile) throws Exception {
        // TODO: audit action

        if(roleProfile.getRoleProfileId() == null) {
            throw new org.endeavourhealth.ui.framework.exceptions.NotFoundException("Please specify a role profile id in body");
        }

        super.setLogbackMarkers(sc);

        RoleRepresentation roleOut = KeycloakAdminClient.instance().realms().roles().putRealmRole(SecurityRoleHelper.toRoleRepresentation(roleProfile));

        // TODO: refactor, so more efficient
        List<RoleRepresentation> composites = KeycloakAdminClient.instance().realms().roles().composites().getComposites(roleOut.getName());
        KeycloakAdminClient.instance().realms().roles().composites().deleteComposite(roleOut.getName(), composites);
        KeycloakAdminClient.instance().realms().roles().composites().postComposite(roleOut.getName(), SecurityRoleHelper.toCompositeRoleRepresentation(roleProfile));

        composites = KeycloakAdminClient.instance().realms().roles().composites().getComposites(roleOut.getName());
        JsonRoleProfile roleOut2 = SecurityRoleHelper.toJsonRoleProfile(roleOut, composites);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(roleOut2)
                .build();
    }

    @DELETE
    @Path("/{role-profile-name}")
    @RequiresAdmin
    @ApiOperation(value = "Delete a role profile")
    public Response delete(@Context SecurityContext sc,
                           @ApiParam(defaultValue = OrgRoles.ROOT_ORGANISATION_ID, value="The currently selected organisation") @HeaderParam(value = OrgRoles.HEADER_ORGANISATION_ID) String headerOrgId,
                           @ApiParam(value="Role profile name (not the UUID!)") @PathParam(value = "role-profile-name") String roleProfileId) throws Exception {
        // TODO: audit action

        KeycloakAdminClient.instance().realms().roles().deleteRealmRole(roleProfileId);

        super.setLogbackMarkers(sc);

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }
}