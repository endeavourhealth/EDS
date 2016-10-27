package org.endeavour.eds.test.api.security.roleProfiles;

import java.util.UUID;

public class JsonRole {

    private UUID roleProfileId;
    private String name;
    private String description;

    private Boolean hasChildRoles;

    public JsonRole() {
    }

    public JsonRole(UUID roleProfileId, String name, String description, Boolean hasChildRoles) {
        this.roleProfileId = roleProfileId;
        this.name = name;
        this.description = description;
        this.hasChildRoles = hasChildRoles;
    }

    public UUID getRoleProfileId() {
        return roleProfileId;
    }

    public void setRoleProfileId(UUID roleProfileId) {
        this.roleProfileId = roleProfileId;
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

    public Boolean getHasChildRoles() {
        return hasChildRoles;
    }

    public void setHasChildRoles(Boolean hasChildRoles) {
        this.hasChildRoles = hasChildRoles;
    }
}
