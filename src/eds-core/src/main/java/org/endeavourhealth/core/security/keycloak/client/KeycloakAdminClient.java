package org.endeavourhealth.core.security.keycloak.client;

import org.endeavourhealth.core.security.keycloak.client.resources.Realms;

public class KeycloakAdminClient {

    private static KeycloakAdminClient instance;

    public static KeycloakAdminClient instance() {
        synchronized (KeycloakClient.class) {
            if (instance == null) {
                instance = new KeycloakAdminClient();
            }
        }
        return instance;
    }

    public KeycloakAdminClient() {
        realms = new Realms();
    }

    private Realms realms;

    public Realms realms() {
        return realms;
    }
}
