package org.endeavourhealth.ui.json.security;

import io.swagger.annotations.ApiModelProperty;
import org.endeavourhealth.ui.utility.SecurityRoleHelper;

import java.util.UUID;

public class JsonRole {

    @ApiModelProperty(example = "00000000-0000-0000-0000-000000000000")
    private UUID roleProfileId;

    @ApiModelProperty(example = SecurityRoleHelper.ROLE_PREFIX_PREFIX + "my-role")
    private String name;

    @ApiModelProperty(example = "My role")
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
