package org.endeavourhealth.core.security.keycloak.client.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.keycloak.representations.idm.ClientRepresentation;

import java.io.IOException;
import java.util.List;

public class Clients extends KeycloakAdminClientBase {

    private static final TypeReference<ClientRepresentation> clientRepresentationTypeReference = new TypeReference<ClientRepresentation>() {};

    //
    // create client
    //
    public ClientRepresentation createClient(String realmId, String clientName) {
        return createClient(realmId, clientName, null, null);
    }

    public ClientRepresentation createClient(String realmId, String clientName, List<String> redirectUris, List<String> webOrigins) {

        assertKeycloakAdminClientInitialised();

        ClientRepresentation client = getDefaultClientRepresentation(clientName,
                redirectUris != null ? redirectUris : Lists.newArrayList("*"),
                webOrigins != null ? webOrigins : Lists.newArrayList("*"));

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = doPost(httpClient, keycloakDeployment.getAuthServerBaseUrl() + "/admin/realms/" + realmId + "/clients", client);
            client = toEntity(response, clientRepresentationTypeReference);
        } catch (IOException e) {
            LOG.error("Keycloak post client failed", e);
        }
        return client;
    }

    private ClientRepresentation getDefaultClientRepresentation(String clientName, List<String> redirectUris, List<String> webOrigins) {
        ClientRepresentation clientRepresentation;
        clientRepresentation = new ClientRepresentation();
        clientRepresentation.setId(clientName);
        clientRepresentation.setName(clientName);
        clientRepresentation.setPublicClient(true);
        clientRepresentation.setDirectAccessGrantsEnabled(true);
        clientRepresentation.setRedirectUris(redirectUris);
        clientRepresentation.setWebOrigins(webOrigins);
        return clientRepresentation;
    }
}
