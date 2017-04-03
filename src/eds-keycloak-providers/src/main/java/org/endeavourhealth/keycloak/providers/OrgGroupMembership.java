package org.endeavourhealth.keycloak.providers;

import java.util.ArrayList;
import java.util.List;

public class OrgGroupMembership {
    private String group;
    private String groupId;
    private String organisationId;
    private List<String> roles;

    public OrgGroupMembership() {
        roles = new ArrayList<>();
    }

    public String getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(String organisationId) {
        this.organisationId = organisationId;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
