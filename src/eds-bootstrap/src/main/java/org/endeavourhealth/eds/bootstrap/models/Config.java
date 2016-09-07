package org.endeavourhealth.eds.bootstrap.models;

public class Config {

    private Cassandra cassandra;
    private Keycloak keycloak;

    public Cassandra getCassandra() {
        return cassandra;
    }

    public void setCassandra(Cassandra cassandra) {
        this.cassandra = cassandra;
    }

    public Keycloak getKeycloak() {
        return keycloak;
    }

    public void setKeycloak(Keycloak keycloak) {
        this.keycloak = keycloak;
    }
}
