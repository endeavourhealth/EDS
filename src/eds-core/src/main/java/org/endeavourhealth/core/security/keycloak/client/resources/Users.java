package org.endeavourhealth.core.security.keycloak.client.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

public class Users extends KeycloakAdminClientBase {

    private static final TypeReference<UserRepresentation> userRepresentationTypeReference = new TypeReference<UserRepresentation>() {};
    private static final TypeReference<List<UserRepresentation>> listUserRepresentationTypeReference = new TypeReference<List<UserRepresentation>>() {};


    public List<UserRepresentation> getUsers(int offset, int limit) {
        return getUsers(null, offset, limit);
    }

    public List<UserRepresentation> getUsers(String search, int offset, int limit) {
        assertKeycloakAdminClientInitialised();

        List<UserRepresentation> users = null;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            // The URL is this: http://localhost:9080/auth/admin/realms/endeavour/users?first=0&max=20 (NOTE: paging in the query string)
            String url = keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + keycloakDeployment.getRealm() + "/users?" + String.format("first=%d&max=%d", offset, limit);
            if(StringUtils.isNotBlank(search)) {
                url += "&search=" + URLEncoder.encode(search, "UTF-8");
            }
            HttpResponse response = doGet(httpClient, url);
            users = toEntity(response, listUserRepresentationTypeReference);
        } catch (IOException e) {
            LOG.error("Keycloak get users failed", e);
        }
        return users;
    }

    //
    // get user
    //

    public UserRepresentation getUser(String userId) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();
        return getUser(keycloakDeployment.getRealm(), userId);
    }

    public UserRepresentation getUser(String realm, String userId) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();

        UserRepresentation userRepresentation = null;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doGet(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/users/" + userId.trim());
            if(isHttpOkStatus(response)) {
                userRepresentation = toEntity(response, userRepresentationTypeReference);
            } else {
                throw new KeycloakClientException("Failed to get user", response.getStatusLine().getReasonPhrase());
            }

        } catch (IOException e) {
            LOG.error("Keycloak get user failed", e);
        }
        return userRepresentation;
    }

    //
    // post user
    //

    public UserRepresentation postUser(UserRepresentation user) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();
        return postUser(keycloakDeployment.getRealm(), user);
    }

    public UserRepresentation postUser(String realm, UserRepresentation user) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doPost(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/users", user);

            if(isHttpOkStatus(response)) {
                response = doGet(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/users/" + getIdFromLocation(response));
                user = toEntity(response, userRepresentationTypeReference);
            } else {
                throw new KeycloakClientException("Failed to create user", response.getStatusLine().getReasonPhrase());
            }

        } catch (IOException e) {
            LOG.error("Keycloak post user failed", e);
        }
        return user;
    }

    //
    // put user
    //

    public UserRepresentation putUser(UserRepresentation user) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();
        return putUser(keycloakDeployment.getRealm(), user);
    }

    public UserRepresentation putUser(String realm, UserRepresentation user) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doPut(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/users/" + user.getId(), user);
            if(isHttpOkStatus(response)) {
                response = doGet(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/users/" + user.getId());
                user = toEntity(response, userRepresentationTypeReference);
            } else {
                throw new KeycloakClientException("Failed to update user", response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            LOG.error("Keycloak put user failed", e);
        }
        return user;
    }
    

    //
    // delete user
    //

    public boolean deleteUser(String userId) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();
        return deleteUser(keycloakDeployment.getRealm(), userId);
    }

    public boolean deleteUser(String realm, String userId) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doDelete(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/users/" + userId.trim());
            if(!isHttpOkStatus(response)) {
                throw new KeycloakClientException("Failed to delete user", response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            LOG.error("Keycloak delete user failed", e);
        }

        return false;
    }

    //
    // join group
    //

    public boolean joinGroup(String userId, String groupId) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();
        return joinGroup(keycloakDeployment.getRealm(), userId, groupId);
    }

    public boolean joinGroup(String realm, String userId, String groupId) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doPut(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/users/" + userId.trim() + "/groups/" + groupId.trim(), null);
            if(!isHttpOkStatus(response)) {
                throw new KeycloakClientException("Failed to join group", response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            LOG.error("Keycloak user join group failed", e);
        }

        return false;
    }


    //
    // leave group
    //

    public boolean leaveGroup(String userId, String groupId) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();
        return leaveGroup(keycloakDeployment.getRealm(), userId, groupId);
    }

    public boolean leaveGroup(String realm, String userId, String groupId) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doDelete(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/users/" + userId.trim() + "/groups/" + groupId.trim());
            if(!isHttpOkStatus(response)) {
                throw new KeycloakClientException("Failed to leave group", response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            LOG.error("Keycloak user leave group failed", e);
        }

        return false;
    }

    //
    // add realm role
    //

    public boolean addRealmRole(String userId, List<RoleRepresentation> roles) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();
        return addRealmRole(keycloakDeployment.getRealm(), userId, roles);
    }

    public boolean addRealmRole(String realm, String userId, List<RoleRepresentation> roles) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doPost(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/users/" + userId.trim() + "/role-mappings/realm", roles);
            if(!isHttpOkStatus(response)) {
                throw new KeycloakClientException("Failed to add roles", response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            LOG.error("Keycloak user join group failed", e);
        }

        return false;
    }


    //
    // remove realm role
    //

    public boolean removeRealmRole(String userId, List<RoleRepresentation> roles) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();
        return removeRealmRole(keycloakDeployment.getRealm(), userId, roles);
    }

    public boolean removeRealmRole(String realm, String userId, List<RoleRepresentation> roles) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doDelete(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/users/" + userId.trim() + "/role-mappings/realm", roles);
            if(!isHttpOkStatus(response)) {
                throw new KeycloakClientException("Failed to remove roles", response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            LOG.error("Keycloak user leave group failed", e);
        }

        return false;
    }

}
