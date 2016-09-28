package org.endeavour.eds.test.api.security.roleProfiles;

import java.util.List;
import java.util.UUID;

public class JsonRoleProfile {

    private UUID roleProfileId;
    private String name;
    private String description;
    private List<String> roles;

    public JsonRoleProfile(UUID roleProfileId, String name, String description, List<String> roles) {
        this.roleProfileId = roleProfileId;
        this.name = name;
        this.description = description;
        this.roles = roles;
    }

    public JsonRoleProfile() {

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

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
