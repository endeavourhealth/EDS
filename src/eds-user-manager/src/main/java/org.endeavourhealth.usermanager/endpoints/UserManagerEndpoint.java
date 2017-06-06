package org.endeavourhealth.usermanager.endpoints;

import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.common.security.keycloak.client.KeycloakAdminClient;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.core.mySQLDatabase.models.GroupRoleMappingEntity;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.coreui.json.JsonClient;
import org.endeavourhealth.coreui.json.JsonEndUser;
import org.endeavourhealth.coreui.json.JsonEndUserRole;
import org.endeavourhealth.coreui.json.JsonGroup;
import org.keycloak.representations.idm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.endeavourhealth.common.security.SecurityUtils.getCurrentUserId;
import static org.endeavourhealth.common.security.SecurityUtils.hasRole;

@Path("/usermanager")
public final class UserManagerEndpoint extends AbstractEndpoint {
	private static final Logger LOG = LoggerFactory.getLogger(UserManagerEndpoint.class);
	private static final UserAuditRepository userAuditRepository = new UserAuditRepository(AuditModule.EdsUserManagerModule.UserManager);

//	private String keycloakRealm;
//	private String authServerBaseUrl;
//
//	private boolean initKeycloakAdmin = false;
//
//	private void initKeycloakAdminClient() {
//
//		// get the Endeavour realm name
//		KeycloakDeployment keycloakDeployment = KeycloakConfigUtils.getDeployment();
//		keycloakRealm = keycloakDeployment.getRealm();
//		authServerBaseUrl = KeycloakConfigUtils.initialize();
//
//		try {
//			LOG.trace("Keycloak token = '{}'", KeycloakClient.instance().getToken().getToken());
//		} catch (IOException e) {
//			LOG.trace("Keycloak token = 'null'", e);
//		}
//
//		initKeycloakAdmin = true;
//	}

	private List<RoleRepresentation> removeSystemRoles (List<RoleRepresentation> rolesIn) {
		//Remove $system roles and the eds_user role (default for all users to enable API calls for logged in user)

		List<RoleRepresentation> rolesOut = new ArrayList<>();

		for (RoleRepresentation roleRep : rolesIn) {
			if (roleRep.getDescription().startsWith("$") || roleRep.getName().equalsIgnoreCase("eds_user")) {
				continue;
			}
			rolesOut.add(roleRep);
		}
		return rolesOut;
	}

	private List<ClientRepresentation> removeSystemClients (List<ClientRepresentation> clientsIn) {
		//Remove $system clients

		List<ClientRepresentation> clientsOut = new ArrayList<>();

		for (ClientRepresentation clientRep : clientsIn) {
			String clientName = clientRep.getName() == null ? clientRep.getClientId() : clientRep.getName();

			if (clientName.startsWith("$")) {
				continue;
			}
			clientsOut.add(clientRep);
		}
		return clientsOut;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/users/user")
	public Response getUser(@Context SecurityContext sc, @QueryParam("userId") String userId) throws Exception {
		super.setLogbackMarkers(sc);

		userAuditRepository.save(getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"User", "User Id", userId);

		LOG.trace("getUser");

		UUID currentUserUuid = getCurrentUserId(sc);
		boolean superUser = hasRole(sc, "eds_superuser");
		//can only view other users info with eds_superuser role in v1.1
		if (!currentUserUuid.toString().equalsIgnoreCase(userId) && !superUser)
		{
			throw new NotAllowedException("Get User not allowed with UserId mismatch");
		}

		//First up, get the user account representation
		KeycloakAdminClient keycloakClient = new KeycloakAdminClient();
		UserRepresentation userRep = keycloakClient.realms().users().getUser(userId);

		//Get all user roles, removing the system roles
		List<RoleRepresentation> realmRoles = removeSystemRoles (keycloakClient.realms().users().getUserRealmRoles(userId));
		List<JsonEndUserRole> userRoles = JsonGetUserRoles(realmRoles);

		//Add user as Json and set roles
		JsonEndUser user = new JsonEndUser(userRep);
		user.setUserRoles(userRoles);

		AbstractEndpoint.clearLogbackMarkers();
		return Response
				.ok()
				.entity(user)
				.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/users/user/groups")
	public Response getUserGroups(@Context SecurityContext sc, @QueryParam("userId") String userId) throws Exception {
		super.setLogbackMarkers(sc);

		userAuditRepository.save(getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"User Groups", "User Id", userId);

		LOG.trace("getUserGroups");

		//First up, get the user account representation
		KeycloakAdminClient keycloakClient = new KeycloakAdminClient();

		//Get user groups
		List<GroupRepresentation> userGroups = keycloakClient.realms().users().getUserGroups(userId);
		List<JsonGroup> groupList = JsonGetGroups (userGroups, false);

		AbstractEndpoint.clearLogbackMarkers();
		return Response
				.ok()
				.entity(groupList)
				.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/users")
	public Response getUsers(@Context SecurityContext sc, @QueryParam("searchData") String searchData) throws Exception {
		super.setLogbackMarkers(sc);

		userAuditRepository.save(getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Users", "Search Data", searchData);

		LOG.trace("getUsers");

		List<JsonEndUser> userList = new ArrayList<>();
		List<UserRepresentation> users;

		KeycloakAdminClient keycloakClient = new KeycloakAdminClient();

		if (searchData == null) {
			users = keycloakClient.realms().users().getUsers("", 0, 100);
		} else {
			users = keycloakClient.realms().users().getUsers(searchData, 0, 100);
		}

		//Add as Json
		for (UserRepresentation user : users) {
			userList.add(new JsonEndUser(user));
		}

		AbstractEndpoint.clearLogbackMarkers();
		return Response
				.ok()
				.entity(userList)
				.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/roles/realm")
	public Response getRealmRoles(@Context SecurityContext sc) throws Exception {
		super.setLogbackMarkers(sc);

		userAuditRepository.save(getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Realm roles");

		LOG.trace("getRealmRoles");

		//Create the keycloak admin client
		KeycloakAdminClient keycloakClient = new KeycloakAdminClient();

		//Get all realm roles, removing the system roles
		List<RoleRepresentation> realmRoles = removeSystemRoles(keycloakClient.realms().roles().getRealmRoles());
		List<JsonEndUserRole> roleList = JsonGetUserRoles(realmRoles);

		AbstractEndpoint.clearLogbackMarkers();
		return Response
				.ok()
				.entity(roleList)
				.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/users/roles/realm/available")
	public Response getAvailableRealmRoles(@Context SecurityContext sc, @QueryParam("userId") String userId) throws Exception {
		super.setLogbackMarkers(sc);

		userAuditRepository.save(getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"User roles available", "User Id", userId);

		LOG.trace("getUserAvailableRealmRoles for userId: "+userId);

		//Create the keycloak admin client
		KeycloakAdminClient keycloakClient = new KeycloakAdminClient();

		//Get all available roles for the user, removing the system roles and creating Json. If call with null userId, get all roles
		List<RoleRepresentation> availableRealmRoles;
		List<JsonEndUserRole> roleList;
		if (userId == null){
			availableRealmRoles = removeSystemRoles(keycloakClient.realms().roles().getRealmRoles());
			roleList = JsonGetUserRoles(availableRealmRoles);
		}
		else {
			availableRealmRoles = removeSystemRoles(keycloakClient.realms().users().getUserRealmRolesAvailable(userId));
			roleList = JsonGetUserRoles(availableRealmRoles);
		}

		AbstractEndpoint.clearLogbackMarkers();
		return Response
				.ok()
				.entity(roleList)
				.build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/groups/save")
	public Response saveGroup(@Context SecurityContext sc) throws Exception {
		super.setLogbackMarkers(sc);

		userAuditRepository.save(getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Groups");

		LOG.trace("saveGroup");

		//Create the keycloak admin client
		KeycloakAdminClient keycloakClient = new KeycloakAdminClient();

		//Test whether group Id can be set.   //TODO: remove test code
		String groupId = UUID.randomUUID().toString();
		GroupRepresentation group = new GroupRepresentation();
		group.setName(groupId);
		group.singleAttribute("organisation-id", groupId);
		//group.setId(groupId);
		group = keycloakClient.realms().groups().postGroup(group);

		AbstractEndpoint.clearLogbackMarkers();
		return Response
				.ok()
				.entity(group)
				.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/groups")
	public Response getGroups(@Context SecurityContext sc) throws Exception {
		super.setLogbackMarkers(sc);

		userAuditRepository.save(getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Groups");

		LOG.trace("getGroups");

		//Create the keycloak admin client
		KeycloakAdminClient keycloakClient = new KeycloakAdminClient();

		//Get all groups
		List<GroupRepresentation> realmGroups = keycloakClient.realms().groups().getGroups();
		List<JsonGroup> groupList = JsonGetGroups (realmGroups, true);

		AbstractEndpoint.clearLogbackMarkers();
		return Response
				.ok()
				.entity(groupList)
				.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/groups/topfolders")
	public Response getGroupsTopParents(@Context SecurityContext sc) throws Exception {
		super.setLogbackMarkers(sc);

		userAuditRepository.save(getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Groups");

		LOG.trace("getGroupsTopFolders");

		//Create the keycloak admin client
		KeycloakAdminClient keycloakClient = new KeycloakAdminClient();

		//Get all groups
		List<GroupRepresentation> realmGroups = keycloakClient.realms().groups().getGroups();
		List<JsonGroup> groupList = JsonGetGroups (realmGroups, true);

		AbstractEndpoint.clearLogbackMarkers();
		return Response
				.ok()
				.entity(groupList)
				.build();
	}

	private List<JsonGroup> JsonGetGroups(List<GroupRepresentation> groups, Boolean parentsOnly ) {
		//Create the keycloak admin client
		KeycloakAdminClient keycloakClient = new KeycloakAdminClient();

		List<JsonGroup> groupList = new ArrayList<>();

		//Loop through the parent groups
		for (GroupRepresentation groupRep : groups) {
			if (!parentsOnly || (parentsOnly && groupRep.getSubGroups().size()>0)) {
				//Get each parent group full representation including attributes
				GroupRepresentation groupRepTop = keycloakClient.realms().groups().getGroup(groupRep.getId());
				JsonGroup group = new JsonGroup(groupRepTop);

				//Add sub groups to each parent inc all info
				List<GroupRepresentation> subGroups = groupRepTop.getSubGroups();
				for (GroupRepresentation subGroupRep : subGroups) {
					GroupRepresentation subGroupRepTop = keycloakClient.realms().groups().getGroup(subGroupRep.getId());
					JsonGroup subGroup = new JsonGroup(subGroupRepTop);

					//Add sub groups level 2 to each parent inc all info
					List<GroupRepresentation> subGroups2 = subGroupRep.getSubGroups();
					for (GroupRepresentation subGroup2Rep : subGroups2) {
						GroupRepresentation subGroup2RepTop = keycloakClient.realms().groups().getGroup(subGroup2Rep.getId());
						JsonGroup subGroup2 = new JsonGroup(subGroup2RepTop);
						subGroup.setSubGroup(subGroup2);
					}

					group.setSubGroup(subGroup);
				}

				//Add to the group list
				groupList.add(group);
			}
		}

		return groupList;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/users/clients/realm")
	public Response getRealmClients(@Context SecurityContext sc) throws Exception {
		super.setLogbackMarkers(sc);

		userAuditRepository.save(getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"Clients");

		LOG.trace("getRealmClients");

		//Create the keycloak admin client
		KeycloakAdminClient keycloakClient = new KeycloakAdminClient();

		//Get all available roles (Realm or for the user), removing the system clients
		List<ClientRepresentation> realmClients = removeSystemClients(keycloakClient.realms().clients().getRealmClients());
		List<JsonClient> clientList = new ArrayList<>();

		//For each client, add in the roles then add as Json
		for (ClientRepresentation clientRep : realmClients) {
			//get client roles
			List<RoleRepresentation> clientRoles = removeSystemRoles(keycloakClient.realms().clients().getClientRoles(clientRep.getId()));

			clientList.add(new JsonClient(clientRep, clientRoles));
		}

		AbstractEndpoint.clearLogbackMarkers();
		return Response
				.ok()
				.entity(clientList)
				.build();
	}

	private GroupRepresentation getRoleGroupMappingDirectDB(String roleId) throws Exception {

		GroupRoleMappingEntity groupRoleMappingEntity = GroupRoleMappingEntity.getGroupRoleMappingByRoleId(roleId);

		if (groupRoleMappingEntity != null) {

			KeycloakAdminClient keycloakClient = new KeycloakAdminClient();
			String groupId = groupRoleMappingEntity.getGroupId();
			GroupRepresentation group = keycloakClient.realms().groups().getGroup(groupId);
			return group;
		}

		return null;
	}

	private GroupRepresentation getRoleGroupMapping(String roleId){
		KeycloakAdminClient keycloakClient = new KeycloakAdminClient();

		//Get realm group hirearchy
		List<GroupRepresentation> groups = keycloakClient.realms().groups().getGroups();
		for (GroupRepresentation groupRep : groups)
		{
			//get group role mapping for each top level group
			List<RoleRepresentation> groupRoles = keycloakClient.realms().groups().getEffectiveRealmRoleMappingIds (groupRep.getId());

			//find if role is mapped to group
			for (RoleRepresentation groupRole : groupRoles)
			{
				if (groupRole.getId().equalsIgnoreCase(roleId))
					return groupRep;
			}

			//Now look down sub groups
			List<GroupRepresentation> subGroups = groupRep.getSubGroups();
			for (GroupRepresentation subGroupRep : subGroups)
			{
				//get group role mapping for each sub group
				List<RoleRepresentation> subGroupRoles = keycloakClient.realms().groups().getEffectiveRealmRoleMappingIds (subGroupRep.getId());

				//find if role is mapped to sub group
				for (RoleRepresentation subGrouprole : subGroupRoles)
				{
					if (subGrouprole.getId().equalsIgnoreCase(roleId))
						return subGroupRep;
				}
			}
		}

		//group not found
		return null;
	}

	private List<JsonEndUserRole> JsonGetUserRoles(List<RoleRepresentation> realmRoles) throws Exception {
		//Create the keycloak admin client
		KeycloakAdminClient keycloakClient = new KeycloakAdminClient();

		List<JsonEndUserRole> roleList = new ArrayList<>();
		for (RoleRepresentation roleRep : realmRoles)
		{
			JsonEndUserRole endUserRole = new JsonEndUserRole(roleRep);

			//Find linked group (org)
			//GroupRepresentation group = getRoleGroupMapping(roleRep.getId());   SLOW!!!!!
			GroupRepresentation group = getRoleGroupMappingDirectDB (roleRep.getId());
			if (group != null) {
				JsonGroup roleGroup = new JsonGroup(group);
				endUserRole.setGroup(roleGroup);
			}

			//Get Realm role composites, i.e. client roles - check is composite first
			if (roleRep.isComposite()) {
				List<RoleRepresentation> roleComposites = keycloakClient.realms().roles().composites().getComposites(roleRep.getName());

				for (RoleRepresentation userClientRole : roleComposites) {
					JsonEndUserRole endUserClientRole = new JsonEndUserRole(userClientRole);
					endUserRole.setClientRole(endUserClientRole);
				}
			}
			//Add to the role list including composites
			roleList.add(endUserRole);
		}

		return roleList;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/users/roles/realm/assigned")
	public Response getUserAssignedRoles(@Context SecurityContext sc, @QueryParam("userId") String userId) throws Exception {
		super.setLogbackMarkers(sc);

		userAuditRepository.save(getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
				"User roles assigned", "User Id", userId);

		LOG.trace("getUserAssignedRoles for userId: "+userId);

		//Create the keycloak admin client
		KeycloakAdminClient keycloakClient = new KeycloakAdminClient();

		//Get all user roles (Realm or for the user), removing the system roles
		List<RoleRepresentation> realmRoles = removeSystemRoles (keycloakClient.realms().users().getUserRealmRoles(userId));
		List<JsonEndUserRole> userRoles = JsonGetUserRoles (realmRoles);

		AbstractEndpoint.clearLogbackMarkers();
		return Response
				.ok()
				.entity(userRoles)
				.build();
	}

 	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/users/save")
	@RequiresAdmin
	public Response saveUser(@Context SecurityContext sc, JsonEndUser user, @QueryParam("editMode") String editMode) throws Exception {
		super.setLogbackMarkers(sc);

		boolean editModeb = editMode.equalsIgnoreCase("1") ? true:false;

		//If editing and the user IDs don't match, then throw an error if user does not have correct role
		//This prevents security vunerability when an authenticated user could execute API outside of app without role
		if (editModeb){
			//get current authenticated user id and check for user editing role (currently eds_superuser for v1.1)  //TODO: v2 roles
			UUID currentUserUuid = getCurrentUserId(sc);
			boolean superUser = hasRole(sc, "eds_superuser");

			//can only update other users with eds_superuser role
			if (!currentUserUuid.toString().equalsIgnoreCase(user.getUuid().toString()) && !superUser)
			{
				throw new NotAllowedException("Save User not allowed with UserId mismatch");
			}
		}

		//Set the basic user profile info
		UserRepresentation userRep = new UserRepresentation();
		userRep.setEnabled(true);
		userRep.setUsername(user.getUsername());
		userRep.setLastName(user.getSurname());
		userRep.setFirstName(user.getForename());
		userRep.setEmail(user.getEmail());

		//Set the user attributes such as mobile and photo and v1 organisation-id
		userRep.singleAttribute("Mobile", user.getMobile());
		userRep.singleAttribute("Photo", user.getPhoto());
		//Preserve v1 organisation-id as the keycloak API has a bug which deletes all attributes
		String defaultOrgId = user.getDefaultOrgId();
		if (defaultOrgId != null && defaultOrgId.trim()!="") {
			userRep.singleAttribute("organisation-id", defaultOrgId);
		}

		//Create the keycloak admin client and file the user
		KeycloakAdminClient keycloakClient = new KeycloakAdminClient();

		String userId;
		if (!editModeb) {
			userRep = keycloakClient.realms().users().postUser(userRep);
			//This is the newly created userId
			userId = userRep.getId();
			user.setUuid(UUID.fromString(userId));  //new uuid to return to client
		} else {
			//This is the existing userId, so we set for update
			userId = user.getUuid().toString();
			userRep.setId(userId);
			userRep = keycloakClient.realms().users().putUser(userRep);
		}

		//Now, file the new temporary password if it is not blank (edit mode password may be blank)
		if (!editModeb || user.getPassword().trim()!=""){
			CredentialRepresentation credential = new CredentialRepresentation();
			credential.setType(CredentialRepresentation.PASSWORD);
			credential.setValue(user.getPassword());
			credential.setTemporary(true);
			keycloakClient.realms().users().setUserPassword (userId, credential);
		}

		//Once initial user saved, add in the role assignments and join/leave associated groups
		List<RoleRepresentation> roles = new ArrayList<>();
		for (JsonEndUserRole jsonEndUserRole : user.getUserRoles()) {
			RoleRepresentation role = new RoleRepresentation(jsonEndUserRole.getName(), jsonEndUserRole.getDescription(), false);
			role.setId(jsonEndUserRole.getUuid().toString());
			roles.add(role);
		}

		//If editing a user, delete their existing roles and group memberships
		if (editModeb) {
			//Delete the edited user from their existing groups
			List<GroupRepresentation> userGroups = keycloakClient.realms().users().getUserGroups(userId);
			for (GroupRepresentation groupRep : userGroups) {

				keycloakClient.realms().users().leaveGroup(userId, groupRep.getId());
			}

			//Delete the existing roles from the edited user
			List<RoleRepresentation> existingRoles = removeSystemRoles(keycloakClient.realms().users().getUserRealmRoles(userId));
			keycloakClient.realms().users().removeRealmRole(userId, existingRoles);
		}

		//Save the new roles to the user
		keycloakClient.realms().users().addRealmRole(userId, roles);

		//Finally, add the user to each role associated groups (i.e. make them a member by virtue they have a role there)
		for (JsonEndUserRole jsonEndUserRole : user.getUserRoles()) {
			keycloakClient.realms().users().joinGroup(userId, jsonEndUserRole.getGroup().getUuid().toString());
		}

		//Blank out password for audit object
		user.setPassword("*********");
		userAuditRepository.save(getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
				"User", "User", user);

		clearLogbackMarkers();

		return Response
				.ok()
				.entity(user)
				.build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/users/roles/save")
	@RequiresAdmin
	public Response saveRole(@Context SecurityContext sc, JsonEndUserRole userRole, @QueryParam("editMode") String editMode) throws Exception {
		super.setLogbackMarkers(sc);

		userAuditRepository.save(getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
				"Roles", "Role", userRole);

		boolean editModeb = editMode.equalsIgnoreCase("1") ? true:false;

		//Create the top level realm role
		RoleRepresentation roleRep = new RoleRepresentation();
		roleRep.setName(userRole.getName().trim());
		roleRep.setDescription(userRole.getDescription());
		roleRep.setComposite(true);

		//Create the keycloak admin client and file the realm role
		KeycloakAdminClient keycloakClient = new KeycloakAdminClient();

		String roleId;
		if (!editModeb) {
			roleRep = keycloakClient.realms().roles().postRealmRole(roleRep);
			roleId = roleRep.getId();
			userRole.setUuid(UUID.fromString(roleId));
		} else {
			roleId = userRole.getUuid().toString();
			roleRep.setId(roleId);
			roleRep = keycloakClient.realms().roles().putRealmRole(roleRep);
		}

		//This is the new Id - file against organisation
		//String roleId = roleRep.getId();
		//TODO: File RoleId against OrganisationId: userRole.getOrganisation(); ??? SEE BELOW

		//Get the new linked group (org)
		String newGroupId = userRole.getGroup().getUuid().toString();

		// Add role to list for API functions
		List<RoleRepresentation> groupRoles = new ArrayList<>();
		groupRoles.add(roleRep);

		//If adding a new role, just add the group
		if (!editModeb) {
			keycloakClient.realms().groups().addRealmRoleMapping(newGroupId, groupRoles);
		}
		else {
			//Get existing role group if they exist, and then removed
			GroupRepresentation oldGroup = getRoleGroupMappingDirectDB (roleRep.getId());
			if (oldGroup != null) {
				String oldGroupId = oldGroup.getId();
				keycloakClient.realms().groups().deleteRealmRoleMapping(oldGroupId, groupRoles);
			}
			// Add in the new linked group (org)
			keycloakClient.realms().groups().addRealmRoleMapping(newGroupId, groupRoles);
		}

		//Get all the linked role composites
		List<RoleRepresentation> clientRoles = new ArrayList<>();
		for (JsonEndUserRole jsonEndUserRole: userRole.getClientRoles()) {
			RoleRepresentation clientRole = new RoleRepresentation(jsonEndUserRole.getName(), jsonEndUserRole.getDescription(),false);
			clientRole.setId(jsonEndUserRole.getUuid().toString());
			clientRoles.add(clientRole);
		}

		//This is the newly created role name, should be same as what was passed in!
		String roleName = roleRep.getName();

		//If adding a new role, just add the role composites, else, delete existing role composites and then add the new role composites (edit)
		if(!editModeb){
			//Save the composite roles to the role (add)
			keycloakClient.realms().roles().composites().postComposite(roleName, clientRoles);
		} else {
			//Delete the existing roles from the edited user
			List<RoleRepresentation> existingRoleComposites = keycloakClient.realms().roles().composites().getComposites(roleName);
			keycloakClient.realms().roles().composites().deleteComposite(roleName, existingRoleComposites);
			//Save the new role composites to the role
			keycloakClient.realms().roles().composites().postComposite(roleName, clientRoles);
		}

		clearLogbackMarkers();

		return Response
				.ok()
				.entity(userRole)
				.build();
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/users/delete")
	@RequiresAdmin
	public Response deleteUser(@Context SecurityContext sc, @QueryParam("userId") String userId) throws Exception {
		super.setLogbackMarkers(sc);

		userAuditRepository.save(getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
				"User", "User Id", userId);

		//Create the keycloak admin client and delete the user
		KeycloakAdminClient keycloakClient = new KeycloakAdminClient();
		boolean success = keycloakClient.realms().users().deleteUser(userId);

		return Response
				.ok()
				.entity(success)
				.build();
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/users/role/delete")
	@RequiresAdmin
	public Response deleteRealmRole(@Context SecurityContext sc, @QueryParam("roleName") String roleName) throws Exception {
		super.setLogbackMarkers(sc);

		userAuditRepository.save(getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
				"Role", "Role", roleName);

		//Create the keycloak admin client and delete the role
		KeycloakAdminClient keycloakClient = new KeycloakAdminClient();

		UriBuilder uriBuilder = UriBuilder.fromPath(roleName);
		URI uri = uriBuilder.build();
		keycloakClient.realms().roles().deleteRealmRole(uri.getPath());

		return Response
				.ok()
				.entity(true)
				.build();
	}
}