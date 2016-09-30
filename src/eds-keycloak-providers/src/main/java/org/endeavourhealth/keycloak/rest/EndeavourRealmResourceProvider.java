package org.endeavourhealth.keycloak.rest;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class EndeavourRealmResourceProvider implements RealmResourceProvider {

    private KeycloakSession session;

    public EndeavourRealmResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return new EndeavourRestResource(session);
    }

    @Override
    public void close() {
    }

}
