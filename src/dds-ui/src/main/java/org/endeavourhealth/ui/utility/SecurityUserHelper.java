package org.endeavourhealth.ui.utility;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.ui.json.security.JsonUser;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.UUID;

public class SecurityUserHelper {

    public static JsonUser toJsonUser(UserRepresentation u) {
        return new JsonUser(toUUID(u.getId()), u.getUsername(), u.getEmail(), u.getFirstName(), u.getLastName());
    }

    public static UserRepresentation toUserRepresentation(JsonUser jsonUser) {
        UserRepresentation u = new UserRepresentation();

        if(jsonUser.getUserId() != null) {
            u.setId(jsonUser.getUserId().toString());
        }
        u.setUsername(jsonUser.getUsername());
        u.setEmail(jsonUser.getEmail());
        u.setFirstName(jsonUser.getFirstName());
        u.setLastName(jsonUser.getLastName());
        return u;
    }

    public static UUID toUUID(String uuidString) {
        if(StringUtils.isBlank(uuidString)) {
            return null;
        }
        return UUID.fromString(uuidString);
    }

}
