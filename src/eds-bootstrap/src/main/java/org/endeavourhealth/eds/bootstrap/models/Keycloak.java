package org.endeavourhealth.eds.bootstrap.models;

public class Keycloak {

    private KeycloakServer server;
    private String realm;

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public KeycloakServer getServer() {
        return server;
    }

    public void setServer(KeycloakServer server) {
        this.server = server;
    }
}
