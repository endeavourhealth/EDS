package org.endeavourhealth.keycloak.rest;

import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.services.resources.admin.RealmAuth;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import java.util.List;

public class EndeavourGroupResource {
    public EndeavourGroupResource(RealmModel realm, KeycloakSession session, RealmAuth auth) {
        this.realm = realm;
        this.auth = auth;
        this.session = session;
    }

    private KeycloakSession session;
    private RealmModel realm;
    private RealmAuth auth;

    @GET
    @NoCache
    @Produces({"application/json"})
    public List<GroupRepresentation> getGroups() {
        this.auth.requireView();
        return ModelToRepresentation.toGroupHierarchy(this.realm, true);
    }
}
