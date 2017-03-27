package org.endeavourhealth.coreui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonClient {
    private UUID uuid = null;
    private String name = null;
    private String description = null;
    private List<JsonEndUserRole> clientRoles = null;

    public JsonClient() {
    }

    public JsonClient(ClientRepresentation keycloakClientRepresentation, List<RoleRepresentation> keycloakClientRoles) {
        this.uuid = UUID.fromString(keycloakClientRepresentation.getId());
        this.name = keycloakClientRepresentation.getName();
        this.description = keycloakClientRepresentation.getDescription();

        //set the linked client roles
        this.clientRoles = new ArrayList<>();
        for (RoleRepresentation userClientRole : keycloakClientRoles) {
            JsonEndUserRole endUserClientRole = new JsonEndUserRole(userClientRole, true);
            this.setClientRole(endUserClientRole);
        }
    }

    /**
     * gets/sets
     */
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<JsonEndUserRole> getClientRoles() {
        return clientRoles;
    }

    public void setClientRoles(List<JsonEndUserRole> clientRoles) {
        this.clientRoles = clientRoles;
    }

    public void setClientRole(JsonEndUserRole clientRole) {
        this.clientRoles.add(clientRole);
    }

}