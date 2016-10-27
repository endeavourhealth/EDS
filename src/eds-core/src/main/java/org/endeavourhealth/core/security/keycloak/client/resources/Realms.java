package org.endeavourhealth.core.security.keycloak.client.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;

import java.io.IOException;
import java.util.List;

public class Realms extends KeycloakAdminClientBase {

    private static final TypeReference<RealmRepresentation> realmRepresentationTypeReference = new TypeReference<RealmRepresentation>() {};
    private static final TypeReference<List<RealmRepresentation>> listRealmRepresentationTypeReference = new TypeReference<List<RealmRepresentation>>() {};


    public Realms() {
        users = new Users();
        clients = new Clients();
        roles = new Roles();
        groups = new Groups();
    }

    private Users users;
    private Clients clients;
    private Roles roles;
    private Groups groups;


    public Users users() {
        return users;
    }

    public Clients clients() {
        return clients;

    }

    public Roles roles() {
        return roles;
    }

    public Groups groups() {
        return groups;
    }

    //
    // get realms
    //

    public List<RealmRepresentation> getRealms() {
        assertKeycloakAdminClientInitialised();

        List<RealmRepresentation> roles = null;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doGet(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms");
            roles = toEntity(response, listRealmRepresentationTypeReference);
        } catch (IOException e) {
            LOG.error("Keycloak get realms failed", e);
        }
        return roles;
    }

    //
    // get realm
    //

    public RealmRepresentation getRealm(String realmId) {
        assertKeycloakAdminClientInitialised();

        RealmRepresentation role = null;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doGet(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realmId);
            role = toEntity(response, realmRepresentationTypeReference);
        } catch (IOException e) {
            LOG.error("Keycloak get realm failed", e);
        }
        return role;
    }

    //
    // post realm
    //

    public RealmRepresentation postRealm(RealmRepresentation role) {
        assertKeycloakAdminClientInitialised();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doPost(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms", role);
            role = toEntity(response, realmRepresentationTypeReference);
        } catch (IOException e) {
            LOG.error("Keycloak post realm failed", e);
        }
        return role;
    }

    //
    // delete realm
    //

    public void deleteRealm(String realmId) {
        assertKeycloakAdminClientInitialised();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doDelete(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realmId);
        } catch (IOException e) {
            LOG.error("Keycloak delete realm failed", e);
        }
    }

}
