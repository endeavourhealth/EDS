package org.endeavourhealth.usermanager.endpoints;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.security.KeycloakConfigUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.common.security.keycloak.client.KeycloakAdminClient;
import org.endeavourhealth.common.security.keycloak.client.KeycloakClient;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.coreui.json.JsonClient;
import org.endeavourhealth.coreui.json.JsonEndUser;
import org.endeavourhealth.coreui.json.JsonEndUserRole;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.representations.idm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.net.URI;
import java.util.*;

@Path("/usermanager")
public final class UserManagerEndpoint extends AbstractEndpoint {
	private static final Logger LOG = LoggerFactory.getLogger(UserManagerEndpoint.class);
	private static final UserAuditRepository userAuditRepository = new UserAuditRepository(AuditModule.EdsUiModule.Audit);

	private String keycloakRealm;
	private String authServerBaseUrl;

	private boolean initKeycloakAdmin = false;

	private void initKeycloakAdminClient() {

		// get the Endeavour realm name
		KeycloakDeployment keycloakDeployment = KeycloakConfigUtils.getDeployment();
		keycloakRealm = keycloakDeployment.getRealm();
		authServerBaseUrl = KeycloakConfigUtils.initialize();

		try {
			LOG.trace("Keycloak token = '{}'", KeycloakClient.instance().getToken().getToken());
		} catch (IOException e) {
			LOG.trace("Keycloak token = 'null'", e);
		}

		initKeycloakAdmin = true;
	}

	private MappingsRepresentation keycloakGetUserRoleMappings (String userId) {
		if(!initKeycloakAdmin) {
			initKeycloakAdminClient();
		}

		MappingsRepresentation roleMapping = null;
		ObjectMapper mapper = new ObjectMapper();
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

			String url = authServerBaseUrl + "/admin/realms/" + keycloakRealm + "/users/"+ userId + "/role-mappings";

			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader(KeycloakClient.instance().getAuthorizationHeader());
			HttpResponse response = httpClient.execute(httpGet);
			roleMapping = mapper.readValue(response.getEntity().getContent(), new TypeReference<MappingsRepresentation>() { });
		} catch (IOException e) {
			LOG.error("Keycloak get user failed", e);
		}
		return roleMapping;
	}

	private List<RoleRepresentation> keycloakGetRoleComposites(String roleName){
		if(!initKeycloakAdmin) {
			initKeycloakAdminClient();
		}

		List<RoleRepresentation> roleComposites = null;
		ObjectMapper mapper = new ObjectMapper();
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

			String url = authServerBaseUrl + "/admin/realms/" + keycloakRealm + "/roles/" + roleName + "/composites";
			UriBuilder uriBuilder = UriBuilder.fromPath(url);
			URI uri = uriBuilder.build();

			HttpGet httpGet = new HttpGet(uri);
			httpGet.addHeader(KeycloakClient.instance().getAuthorizationHeader());
			HttpResponse response = httpClient.execute(httpGet);
			roleComposites = mapper.readValue(response.getEntity().getContent(), new TypeReference<List<RoleRepresentation>>() { });
		} catch (IOException e) {
			LOG.error("Keycloak get role composites failed", e);
		}
		return roleComposites;
	}

	private List<RoleRepresentation> keycloakGetAvailableRealmRoles(String userId){
		if(!initKeycloakAdmin) {
			initKeycloakAdminClient();
		}

		List<RoleRepresentation> userAvailableRoles = null;
		ObjectMapper mapper = new ObjectMapper();
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

			String url;
			if (userId != null) {
				url = authServerBaseUrl + "/admin/realms/" + keycloakRealm + "/users/" + userId + "/role-mappings/realm/available";
			}
			else {
				url = authServerBaseUrl + "/admin/realms/" + keycloakRealm + "/roles";
			}

			UriBuilder uriBuilder = UriBuilder.fromPath(url);
			URI uri = uriBuilder.build();

			HttpGet httpGet = new HttpGet(uri);
			httpGet.addHeader(KeycloakClient.instance().getAuthorizationHeader());
			HttpResponse response = httpClient.execute(httpGet);
			userAvailableRoles = mapper.readValue(response.getEntity().getContent(), new TypeReference<List<RoleRepresentation>>() { });
		} catch (IOException e) {
			LOG.error("Keycloak get user available roles failed", e);
		}
		return userAvailableRoles;
	}

	private List<ClientRepresentation> keycloakGetRealmClients() {
		if (!initKeycloakAdmin) {
			initKeycloakAdminClient();
		}

		List<ClientRepresentation> realmClients = null;
		ObjectMapper mapper = new ObjectMapper();
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

			String url = authServerBaseUrl + "/admin/realms/" + keycloakRealm + "/clients";
			UriBuilder uriBuilder = UriBuilder.fromPath(url);
			URI uri = uriBuilder.build();

			HttpGet httpGet = new HttpGet(uri);
			httpGet.addHeader(KeycloakClient.instance().getAuthorizationHeader());
			HttpResponse response = httpClient.execute(httpGet);
			realmClients = mapper.readValue(response.getEntity().getContent(), new TypeReference<List<ClientRepresentation>>() {
			});
		} catch (IOException e) {
			LOG.error("Keycloak get realm clients failed", e);
		}
		return realmClients;
	}

	private List<RoleRepresentation> keycloakGetClientRoles(String clientId) {
		if (!initKeycloakAdmin) {
			initKeycloakAdminClient();
		}

		List<RoleRepresentation> clientRoles = null;
		ObjectMapper mapper = new ObjectMapper();
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

			String url = authServerBaseUrl + "/admin/realms/" + keycloakRealm + "/clients/"+clientId+"/roles";
			UriBuilder uriBuilder = UriBuilder.fromPath(url);
			URI uri = uriBuilder.build();

			HttpGet httpGet = new HttpGet(uri);
			httpGet.addHeader(KeycloakClient.instance().getAuthorizationHeader());
			HttpResponse response = httpClient.execute(httpGet);
			clientRoles = mapper.readValue(response.getEntity().getContent(), new TypeReference<List<RoleRepresentation>>() {
			});
		} catch (IOException e) {
			LOG.error("Keycloak get realm clients failed", e);
		}
		return clientRoles;
	}

	private List<RoleRepresentation> removeSystemRoles (List<RoleRepresentation> rolesIn) {
		//Remove $system roles and the eds_user role (default for all users to enable API calls for logged in user)

		List<RoleRepresentation> rolesOut = new ArrayList<>();;

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

		List<ClientRepresentation> clientsOut = new ArrayList<>();;

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

		//TODO: Audit into MySQL?
		//userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
		//		"Users");

		LOG.trace("getUser");

		//First up, get the user account representation
		KeycloakAdminClient keycloakClient = new KeycloakAdminClient();
		UserRepresentation userRep = keycloakClient.realms().users().getUser(userId);

		//Get user roles and composites
		List<JsonEndUserRole> userRoles = JsonGetUserRoles(userId);

		//Add as Json
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
	@Path("/users")
	public Response getUsers(@Context SecurityContext sc, @QueryParam("searchData") String searchData) throws Exception {
		super.setLogbackMarkers(sc);

		//TODO: Audit into MySQL?
		//userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
		//		"Users");

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
	@Path("/users/roles/realm/available")
	public Response getAvailableRealmRoles(@Context SecurityContext sc, @QueryParam("userId") String userId) throws Exception {
		super.setLogbackMarkers(sc);

		//TODO: Audit into MySQL?
		//userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
		//		"Users");

		LOG.trace("getUserAvailableRealmRoles for userId: "+userId);

		//Get all available roles (Realm or for the user), removing the system role
		List<RoleRepresentation> availableRealmRoles = removeSystemRoles(keycloakGetAvailableRealmRoles(userId));

		List<JsonEndUserRole> roleList = new ArrayList<>();
		for (RoleRepresentation roleRep : availableRealmRoles) {
			JsonEndUserRole endUserRole = new JsonEndUserRole(roleRep, false);

			//Get Realm role composites, i.e. client roles - check is composite first
			if (roleRep.isComposite()) {
				List<RoleRepresentation> roleComposites = keycloakGetRoleComposites(roleRep.getName());

				for (RoleRepresentation userClientRole : roleComposites) {
					JsonEndUserRole endUserClientRole = new JsonEndUserRole(userClientRole, true);
					endUserRole.setClientRole(endUserClientRole);
				}
			}
			//Set the role list with composites
			roleList.add(endUserRole);
		}

		AbstractEndpoint.clearLogbackMarkers();
		return Response
				.ok()
				.entity(roleList)
				.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/users/clients/realm")
	public Response getRealmClients(@Context SecurityContext sc) throws Exception {
		super.setLogbackMarkers(sc);

		//TODO: Audit into MySQL?
		//userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
		//		"Users");

		LOG.trace("getRealmClients");

		//Get all available roles (Realm or for the user), removing the system clients
		List<ClientRepresentation> realmClients = removeSystemClients(keycloakGetRealmClients());
		List<JsonClient> clientList = new ArrayList<>();

		//For each client, add in the roles then add as Json
		for (ClientRepresentation clientRep : realmClients) {
			//get client roles
			List<RoleRepresentation> clientRoles = removeSystemRoles(keycloakGetClientRoles(clientRep.getId()));

			clientList.add(new JsonClient(clientRep, clientRoles));
		}

		AbstractEndpoint.clearLogbackMarkers();
		return Response
				.ok()
				.entity(clientList)
				.build();
	}

	private List<RoleRepresentation> getUserRealmRoles(String userId) {
		//Firstly, get the mapping representation for the user
		MappingsRepresentation userRoleMap = new MappingsRepresentation();
		userRoleMap = keycloakGetUserRoleMappings(userId);

		//Get all available roles (Realm or for the user), removing the system roles
		return removeSystemRoles(userRoleMap.getRealmMappings());
	}

	private List<JsonEndUserRole> JsonGetUserRoles(String userId) {
		//Get all available roles (Realm or for the user), removing the system roles
		List<RoleRepresentation> availableRealmRoles = getUserRealmRoles(userId);

		List<JsonEndUserRole> roleList = new ArrayList<>();
		for (RoleRepresentation roleRep : availableRealmRoles) {
			JsonEndUserRole endUserRole = new JsonEndUserRole(roleRep, false);

			//Get Realm role composites, i.e. client roles - check is composite first
			if (roleRep.isComposite()) {
				List<RoleRepresentation> roleComposites = keycloakGetRoleComposites(roleRep.getName());

				for (RoleRepresentation userClientRole : roleComposites) {
					JsonEndUserRole endUserClientRole = new JsonEndUserRole(userClientRole, true);
					endUserRole.setClientRole(endUserClientRole);
				}
			}
			//Set the role list with composites
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

		//TODO: Audit into MySQL?
		//userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
		//		"Users");

		LOG.trace("getUserAssignedRoles for userId: "+userId);

		//Get user roles and composites
		List<JsonEndUserRole> userRoles = JsonGetUserRoles(userId);

		AbstractEndpoint.clearLogbackMarkers();
		return Response
				.ok()
				.entity(userRoles)
				.build();
	}

	private void keycloakSetUserPassword(String userId, CredentialRepresentation credential) {
		if(!initKeycloakAdmin) {
			initKeycloakAdminClient();
		}

		ObjectMapper mapper = new ObjectMapper();
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

			String url = authServerBaseUrl + "/admin/realms/" + keycloakRealm + "/users/" + userId + "/reset-password";

			UriBuilder uriBuilder = UriBuilder.fromPath(url);
			URI uri = uriBuilder.build();

			HttpPut httpPut = new HttpPut(uri);
			httpPut.addHeader(KeycloakClient.instance().getAuthorizationHeader());
			String content = ObjectMapperPool.getInstance().writeValueAsString(credential);
			HttpEntity httpEntity = new StringEntity(content, ContentType.APPLICATION_JSON);
			httpPut.setEntity(httpEntity);

			HttpResponse response = httpClient.execute(httpPut);

		} catch (IOException e) {
			LOG.error("Keycloak save user password failed", e);
		}
	}

 	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/users/save")
	@RequiresAdmin
	public Response saveUser(@Context SecurityContext sc, JsonEndUser user, @QueryParam("editMode") String editMode) throws Exception {
		super.setLogbackMarkers(sc);

		//TODO: Audit into MySQL?
		//userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
		//		"Users");

		boolean editModeb = editMode.equalsIgnoreCase("1") ? true:false;

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
		if (user.getDefaultOrgId().trim()!="") {
			userRep.singleAttribute("organisation-id", user.getDefaultOrgId());
		}

		//setting TOTP to true means the user needs to reconfigure TOTP on login
//		if (user.getTOTP().equalsIgnoreCase("yes")) {
//			userRep.setRequiredActions(Lists.newArrayList("CONFIGURE_TOTP"));
//		}
//		else {
//			userRep.setTotp(false);   //remove TOTP during edit
//		}

		//Create the keycloak admin client and file the user
		KeycloakAdminClient keycloakClient = new KeycloakAdminClient();

		String userId = "";
		if (!editModeb) {
			userRep = keycloakClient.realms().users().postUser(userRep);
			//This is the newly created userId
			userId = userRep.getId();
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
			keycloakSetUserPassword (userId, credential);
		}

		//Once initial user saved, add in the role assignments
		List<RoleRepresentation> roles = new ArrayList<>();
		for (JsonEndUserRole jsonEndUserRole : user.getUserRoles()) {
			RoleRepresentation role = new RoleRepresentation(jsonEndUserRole.getName(), jsonEndUserRole.getDescription(), false);
			role.setId(jsonEndUserRole.getUuid().toString());
			roles.add(role);
		}

		//If adding, just add the roles, else, delete existing roles and then add the new roles (edit)
		if(!editModeb){
			//Save the roles to the user (add)
			keycloakClient.realms().users().addRealmRole(userId, roles);
		} else {
			//Delete the existing roles from the edited user
			List<RoleRepresentation> existingRoles = getUserRealmRoles(userId);
			keycloakClient.realms().users().removeRealmRole(userId, existingRoles);
			//Save the new roles to the user
			keycloakClient.realms().users().addRealmRole(userId, roles);
		}

		clearLogbackMarkers();

		return Response
				.ok()
				.entity(userRep)
				.build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/users/roles/save")
	@RequiresAdmin
	public Response saveRole(@Context SecurityContext sc, JsonEndUserRole userRole) throws Exception {
		super.setLogbackMarkers(sc);

		//TODO: Audit into MySQL?
		//userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
		//		"Users");

		//Create the top level realm role
		RoleRepresentation roleRep = new RoleRepresentation();
		//Strip out spaces and replace with underscore to prevent composite crashes when using rolename as key!
		roleRep.setName(userRole.getName().replaceAll(" ","_").trim());
		roleRep.setDescription(userRole.getDescription());
		roleRep.setComposite(true);

		//Create the keycloak admin client and file the realm role
		KeycloakAdminClient keycloakClient = new KeycloakAdminClient();
		roleRep = keycloakClient.realms().roles().postRealmRole(roleRep);

		//This is the new Id - file against organisation
		String roleId = roleRep.getId();
		//TODO: File RoleId against OrganisationId: userRole.getOrganisation();

		//This is the newly created role name, should be same as what was passed in!
		String roleName = roleRep.getName();

		//Get all the linked role composites
		List<RoleRepresentation> clientRoles = new ArrayList<>();

		for (JsonEndUserRole jsonEndUserRole: userRole.getClientRoles()) {
			RoleRepresentation clientRole = new RoleRepresentation(jsonEndUserRole.getName(), jsonEndUserRole.getDescription(),false);
			clientRole.setId(jsonEndUserRole.getUuid().toString());
			clientRoles.add(clientRole);
		}

		//Save the role composites to the new role
		keycloakClient.realms().roles().composites().postComposite(roleName, clientRoles);

		clearLogbackMarkers();

		return Response
				.ok()
				.entity(roleRep)
				.build();
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/users/delete")
	@RequiresAdmin
	public Response deleteUser(@Context SecurityContext sc, @QueryParam("userId") String userId) throws Exception {
		super.setLogbackMarkers(sc);

		//TODO: Audit into MySQL?
		//userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
		//		"Users");

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

		//TODO: Audit into MySQL?
		//userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
		//		"Users");

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