package org.endeavourhealth.core.security.keycloak.client.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.io.IOException;
import java.util.List;

public class Groups extends KeycloakAdminClientBase {

    private static final TypeReference<GroupRepresentation> groupRepresentationTypeReference = new TypeReference<GroupRepresentation>() {};
    private static final TypeReference<List<GroupRepresentation>> listGroupRepresentationTypeReference = new TypeReference<List<GroupRepresentation>>() {};
    private static final TypeReference<List<UserRepresentation>> listUserRepresentationTypeReference = new TypeReference<List<UserRepresentation>>() {};

    //
    // get groups
    //

    public List<GroupRepresentation> getGroups() {
        assertKeycloakAdminClientInitialised();
        return getGroups(keycloakDeployment.getRealm());
    }

    public List<GroupRepresentation> getGroups(String realm) {
        assertKeycloakAdminClientInitialised();

        List<GroupRepresentation> groups = null;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doGet(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/groups");
            groups = toEntity(response, listGroupRepresentationTypeReference);
        } catch (IOException e) {
            LOG.error("Keycloak get groups failed", e);
        }
        return groups;
    }

    //
    // get group
    //

    public GroupRepresentation getGroup(String groupName) {
        assertKeycloakAdminClientInitialised();
        return getGroup(keycloakDeployment.getRealm(), groupName);
    }

    public GroupRepresentation getGroup(String realm, String groupName) {
        assertKeycloakAdminClientInitialised();

        GroupRepresentation groupRepresentation = null;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doGet(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/groups/" + groupName.trim());
            groupRepresentation = toEntity(response, groupRepresentationTypeReference);
        } catch (IOException e) {
            LOG.error("Keycloak get group failed", e);
        }
        return groupRepresentation;
    }

    //
    // post group
    //

    public GroupRepresentation postGroup(GroupRepresentation group) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();
        return postGroup(keycloakDeployment.getRealm(), group);
    }

    public GroupRepresentation postGroup(String realm, GroupRepresentation group) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doPost(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/groups", group);
            if(isHttpOkStatus(response)) {
                response = doGet(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/groups/" + getIdFromLocation(response));
                group = toEntity(response, groupRepresentationTypeReference);
            } else {
                throw new KeycloakClientException("Failed to post group", response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            LOG.error("Keycloak post group failed", e);
        }
        return group;
    }

    //
    // put group
    //

    public GroupRepresentation putGroup(GroupRepresentation group) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();
        return putGroup(keycloakDeployment.getRealm(), group);
    }

    public GroupRepresentation putGroup(String realm, GroupRepresentation group) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doPut(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/groups/" + group.getId(), group);
            if(isHttpOkStatus(response)) {
                response = doGet(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/groups/" + group.getId());
                group = toEntity(response, groupRepresentationTypeReference);
            } else {
                throw new KeycloakClientException("Failed to put group", response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            LOG.error("Keycloak put group failed", e);
        }
        return group;
    }

    //
    // delete group
    //

    public void deleteGroup(String groupName) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();
        deleteGroup(keycloakDeployment.getRealm(), groupName);
    }

    public void deleteGroup(String realm, String groupName) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doDelete(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/groups/" + groupName.trim());
            if(!isHttpOkStatus(response)) {
                throw new KeycloakClientException("Failed to delete group", response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            LOG.error("Keycloak delete group failed", e);
        }
    }

    //
    // child group
    //

    public GroupRepresentation postChildGroup(String parentGroupId, GroupRepresentation group) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();
        return postChildGroup(keycloakDeployment.getRealm(), parentGroupId, group);
    }

    public GroupRepresentation postChildGroup(String realm, String parentGroupId, GroupRepresentation group) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doPost(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/groups/" + parentGroupId.trim() + "/children", group);
            group = toEntity(response, groupRepresentationTypeReference);
            if(!isHttpOkStatus(response)) {
                throw new KeycloakClientException("Failed to post child group", response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            LOG.error("Keycloak post group failed", e);
        }
        return group;
    }

    //
    // get group membership
    //

    public List<UserRepresentation> getGroupMembers(String groupId, int offset, int limit) {
        assertKeycloakAdminClientInitialised();
        return getGroupMembers(keycloakDeployment.getRealm(), groupId, offset, limit);
    }

    public List<UserRepresentation> getGroupMembers(String realm, String groupId, int offset, int limit) {
        assertKeycloakAdminClientInitialised();

        List<UserRepresentation> users = null;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            String queryString = String.format("first=%d&max=%d", offset, limit);
            HttpResponse response = doGet(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/groups/" + groupId.trim() + "/members?" + queryString);
            users = toEntity(response, listUserRepresentationTypeReference);
        } catch (IOException e) {
            LOG.error("Keycloak get group failed", e);
        }
        return users;
    }

}
