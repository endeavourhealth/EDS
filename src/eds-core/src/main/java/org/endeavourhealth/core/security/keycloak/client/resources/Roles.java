package org.endeavourhealth.core.security.keycloak.client.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.keycloak.representations.idm.RoleRepresentation;

import java.io.IOException;
import java.util.List;

public class Roles extends KeycloakAdminClientBase {

    private static final TypeReference<RoleRepresentation> roleRepresentationTypeReference = new TypeReference<RoleRepresentation>() {};
    private static final TypeReference<List<RoleRepresentation>> listRoleRepresentationTypeReference = new TypeReference<List<RoleRepresentation>>() {};


    public Roles() {
        composites = new RoleComposites();
    }

    private RoleComposites composites;

    public RoleComposites composites() {
        return composites;
    }

    //
    // get roles
    //

    public List<RoleRepresentation> getRealmRoles() {
        assertKeycloakAdminClientInitialised();
        return getRealmRoles(keycloakDeployment.getRealm());
    }

    public List<RoleRepresentation> getRealmRoles(String realm) {
        assertKeycloakAdminClientInitialised();

        List<RoleRepresentation> roles = null;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doGet(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/roles");
            roles = toEntity(response, listRoleRepresentationTypeReference);
        } catch (IOException e) {
            LOG.error("Keycloak get roles failed", e);
        }
        return roles;
    }

    //
    // get role
    //

    public RoleRepresentation getRole(String roleName) {
        assertKeycloakAdminClientInitialised();
        return getRole(keycloakDeployment.getRealm(), roleName);
    }

    public RoleRepresentation getRole(String realm, String roleName) {
        assertKeycloakAdminClientInitialised();

        RoleRepresentation roleRepresentation = null;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doGet(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/roles/" + roleName.trim());
            roleRepresentation = toEntity(response, roleRepresentationTypeReference);
        } catch (IOException e) {
            LOG.error("Keycloak get role failed", e);
        }
        return roleRepresentation;
    }

    //
    // post role
    //

    public RoleRepresentation postRealmRole(RoleRepresentation role) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();
        return postRealmRole(keycloakDeployment.getRealm(), role);
    }

    public RoleRepresentation postRealmRole(String realm, RoleRepresentation role) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doPost(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/roles", role);
            if(isHttpOkStatus(response)) {
                response = doGet(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/roles/" + getIdFromLocation(response));
                role = toEntity(response, roleRepresentationTypeReference);
            } else {
                throw new KeycloakClientException("Failed to post role", response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            LOG.error("Keycloak post role failed", e);
        }
        return role;
    }

    //
    // put role
    //

    public RoleRepresentation putRealmRole(RoleRepresentation role) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();
        return putRealmRole(keycloakDeployment.getRealm(), role);
    }

    public RoleRepresentation putRealmRole(String realm, RoleRepresentation role) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doPut(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/roles/" + role.getName(), role);
            if(isHttpOkStatus(response)) {
                response = doGet(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/roles/" + role.getName());
                role = toEntity(response, roleRepresentationTypeReference);
            } else {
                throw new KeycloakClientException("Failed to put role", response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            LOG.error("Keycloak put role failed", e);
        }
        return role;
    }

    //
    // delete role
    //

    public void deleteRealmRole(String roleName) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();
        deleteRealmRole(keycloakDeployment.getRealm(), roleName);
    }

    public void deleteRealmRole(String realm, String roleName) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doDelete(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/roles/" + roleName.trim());
            if(!isHttpOkStatus(response)) {
                throw new KeycloakClientException("Failed to delete role", response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            LOG.error("Keycloak delete role failed", e);
        }
    }

}
