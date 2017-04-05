package org.endeavourhealth.coreui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class JsonEndUserRole {
    private UUID uuid = null;
    private String name = null;
    private String description = null;
    private JsonOrganisation organisation = null;
    private List<JsonEndUserRole> clientRoles = null;

    public JsonEndUserRole() {
    }

    public JsonEndUserRole(RoleRepresentation keycloakUserRoleRepresentation, Boolean isClientRole) {
        this.uuid = UUID.fromString(keycloakUserRoleRepresentation.getId());
        this.name = keycloakUserRoleRepresentation.getName();
        this.description = keycloakUserRoleRepresentation.getDescription();
        this.organisation = new JsonOrganisation();     //TODO: needs linking to the Role/Organisation table
        this.clientRoles = new ArrayList<>();
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

    public void setClientRole(JsonEndUserRole clientRole) {this.clientRoles.add(clientRole); }

    public JsonEndUserRole getClientRole (int index) { return this.clientRoles.get(index); }

    public JsonOrganisation getOrganisation () { return this.organisation; }

    public void setOrganisation(JsonOrganisation organisation) {this.organisation = organisation; }
}
