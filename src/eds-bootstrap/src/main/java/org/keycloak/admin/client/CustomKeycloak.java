package org.keycloak.admin.client;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;

public class CustomKeycloak extends Keycloak {
    public CustomKeycloak(String serverUrl, String realm, String username, String password, String clientId, String clientSecret, String grantType, ResteasyClient resteasyClient) {
        super(serverUrl, realm, username, password, clientId, clientSecret, grantType, resteasyClient);
    }
}
