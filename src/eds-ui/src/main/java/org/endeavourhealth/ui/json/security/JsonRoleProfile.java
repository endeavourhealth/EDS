package org.endeavourhealth.ui.json.security;

import io.swagger.annotations.ApiModelProperty;
import org.endeavourhealth.ui.utility.SecurityRoleHelper;

import java.util.List;
import java.util.UUID;

public class JsonRoleProfile {

    @ApiModelProperty(example = "00000000-0000-0000-0000-000000000000")
    private UUID roleProfileId;

    @ApiModelProperty(example = SecurityRoleHelper.ROLE_PROFILE_PREFIX_PREFIX + "my-profile")
    private String name;

    @ApiModelProperty(example = "My profile")
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
