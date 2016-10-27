package org.endeavourhealth.ui.utility;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.ui.json.security.JsonKeycloakUser;
import org.endeavourhealth.ui.json.security.JsonOrgRole;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class SecurityGroupHelper {

    public final static String ORGANISATION_ID = "organisation-id";
    public final static String DESCRIPTION = "description";

    public static String getAttribute(String attributeId, GroupRepresentation group) {
        if(group.getAttributes() != null) {
            List<String> orgIds = group.getAttributes().getOrDefault(attributeId, null);
            if (orgIds != null && orgIds.size() == 1) {
                return orgIds.get(0);
            }
        }
        return null;
    }

    public static String getOrganisationId(GroupRepresentation group) {
        return getAttribute(ORGANISATION_ID, group);
    }

    public static String getDescription(GroupRepresentation group) {
        return getAttribute(DESCRIPTION, group);
    }

    public static JsonOrgRole toJsonOrgRole(GroupRepresentation g) {
        return new JsonOrgRole(toUUID(g.getId()), g.getName(), getDescription(g), toUUID(getOrganisationId(g)), g.getRealmRoles());
    }

    public static UUID toUUID(String uuidString) {
        if(StringUtils.isBlank(uuidString)) {
            return null;
        }
        return UUID.fromString(uuidString);
    }

    public static GroupRepresentation toGroupRepresentation(JsonOrgRole orgRole) {
        GroupRepresentation g = new GroupRepresentation();

        g.setId(orgRole.getOrgRoleId() != null ? orgRole.getOrgRoleId().toString() : null);
        g.setName(orgRole.getName());

        if(orgRole.getRoles() != null && orgRole.getRoles().size() > 0) {
            g.setRealmRoles(orgRole.getRoles());
        }

        Map<String, List<String>> attr = new HashMap<>();
        if(StringUtils.isNotBlank(orgRole.getDescription())) {
            attr.put(DESCRIPTION, Lists.newArrayList(orgRole.getDescription()));
        }
        if(orgRole.getOrganisationId()!= null) {
            attr.put(ORGANISATION_ID, Lists.newArrayList(orgRole.getOrganisationId().toString()));
        }
        if(attr.size() > 0) {
            g.setAttributes(attr);
        }
        return g;
    }

    public static List<JsonKeycloakUser> toJsonKeycloakUserList(List<UserRepresentation> users) {
        return users.stream().map(u -> new JsonKeycloakUser(
                toUUID(u.getId()), u.getUsername(), u.getEmail(), u.getFirstName(), u.getLastName()
        )).collect(Collectors.toList());
    }
}
