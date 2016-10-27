package org.endeavourhealth.core.security.keycloak.client.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.keycloak.representations.idm.RoleRepresentation;

import java.io.IOException;
import java.util.List;

public class RoleComposites extends KeycloakAdminClientBase {

    private static final TypeReference<List<RoleRepresentation>> listRoleRepresentationTypeReference = new TypeReference<List<RoleRepresentation>>() {};

    //
    // get composites
    //

    public List<RoleRepresentation> getComposites(String roleName) {
        assertKeycloakAdminClientInitialised();
        return getComposites(keycloakDeployment.getRealm(), roleName);
    }

    public List<RoleRepresentation> getComposites(String realm, String roleName) {
        assertKeycloakAdminClientInitialised();

        List<RoleRepresentation> composites = null;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doGet(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/roles/" + roleName + "/composites");
            composites = toEntity(response, listRoleRepresentationTypeReference);
        } catch (IOException e) {
            LOG.error("Keycloak get composites failed", e);
        }
        return composites;
    }

    //
    // post composite
    //

    public List<RoleRepresentation> postComposite(String roleName, RoleRepresentation composite) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();
        return postComposite(keycloakDeployment.getRealm(), roleName, Lists.newArrayList(composite));
    }

    public List<RoleRepresentation> postComposite(String roleName, List<RoleRepresentation> composites) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();
        return postComposite(keycloakDeployment.getRealm(), roleName, composites);
    }

    public List<RoleRepresentation> postComposite(String realm, String roleName, List<RoleRepresentation> composites) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doPost(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/roles/" + roleName + "/composites", composites);
            if(isHttpOkStatus(response)) {
                response = doGet(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/roles/" + roleName + "/composites");
                return toEntity(response, listRoleRepresentationTypeReference);
            } else {
                throw new KeycloakClientException("Failed to post composite", response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            LOG.error("Keycloak post composite failed", e);
        }
        return null;
    }

    //
    // delete composite
    //

    public void deleteComposite(String roleName, List<RoleRepresentation> composites) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();
        deleteComposite(keycloakDeployment.getRealm(), roleName, composites);
    }

    public void deleteComposite(String realm, String roleName, List<RoleRepresentation> composites) throws KeycloakClientException {
        assertKeycloakAdminClientInitialised();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doDelete(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realm + "/roles/" + roleName + "/composites", composites);
            if(!isHttpOkStatus(response)) {
                throw new KeycloakClientException("Failed to delete composite", response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            LOG.error("Keycloak delete composite failed", e);
        }
    }
}
