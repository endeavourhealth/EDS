package org.endeavourhealth.ui.utility;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.ui.json.security.JsonRole;
import org.endeavourhealth.ui.json.security.JsonRoleProfile;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SecurityRoleHelper {

    public final static String ROLE_PREFIX_PREFIX = "eds_";
    public final static String ROLE_PROFILE_PREFIX_PREFIX = "eds_role_profile_";

    public static JsonRoleProfile toJsonRoleProfile(RoleRepresentation r, List<RoleRepresentation> composites) {

        List<String> compositeRoles = new ArrayList<>();
        if(composites != null && composites.size() > 0) {
            compositeRoles = composites.stream().map(c -> c.getName()).collect(Collectors.toList());
        }
        JsonRoleProfile roleProfile = new JsonRoleProfile(toUUID(r.getId()), r.getName(), r.getDescription(), compositeRoles);
        return roleProfile;
    }

    public static RoleRepresentation toRoleRepresentation(JsonRoleProfile r) {
        RoleRepresentation role = new RoleRepresentation();

        if(r.getRoleProfileId() != null) {
            role.setId(r.getRoleProfileId().toString());
        }
        String name = r.getName().trim().replaceAll("\\W", "_").toLowerCase();
        if(!name.startsWith(ROLE_PROFILE_PREFIX_PREFIX)) {
            name = ROLE_PROFILE_PREFIX_PREFIX + name;
        }
        role.setName(name);
        role.setDescription(r.getDescription());

        if(r.getRoles() != null && r.getRoles().size() > 0) {
            role.setComposite(true);
        }

        return role;
    }

    public static UUID toUUID(String uuidString) {
        if(StringUtils.isBlank(uuidString)) {
            return null;
        }
        return UUID.fromString(uuidString);
    }

    public static List<RoleRepresentation> toCompositeRoleRepresentation(JsonRoleProfile roleIn) {
        if(roleIn.getRoles() == null || roleIn.getRoles().size() < 1) {
            return new ArrayList<>();
        }

        return roleIn.getRoles().stream().map(c -> {
            RoleRepresentation r = new RoleRepresentation();
            r.setId(c);
            return r;
        }).collect(Collectors.toList());
    }

    public static JsonRole toJsonRole(RoleRepresentation r) {
        return new JsonRole(toUUID(r.getId()), r.getName(), r.getDescription(), r.isComposite());
    }
}
