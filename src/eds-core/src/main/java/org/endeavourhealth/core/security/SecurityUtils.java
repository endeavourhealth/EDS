package org.endeavourhealth.core.security;

import org.endeavourhealth.core.data.admin.models.EndUser;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.SecurityContext;
import java.util.UUID;

/**
 * This class provides security utilities for Keycloak integration.
 */
public class SecurityUtils {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityUtils.class);

    public static UUID getCurrentUserId(SecurityContext sc) {
        if(sc != null) {
            if(sc.getUserPrincipal() != null) {
                return UUID.fromString(sc.getUserPrincipal().getName());
            }
        }
        LOG.warn("Something appears to be wrong with the security configuration, SecurityContext is null.");
        return null;
    }

    public static KeycloakSecurityContext getKeycloakSecurityContext(SecurityContext sc) {
        if (sc != null && sc.getUserPrincipal() != null) {
            KeycloakPrincipal kp = (KeycloakPrincipal)sc.getUserPrincipal();
            return kp.getKeycloakSecurityContext();
        }
        LOG.warn("Something appears to be wrong with the security configuration, UserPrincipal is not as expected.");
        return null;
    }

    public static AccessToken getToken(SecurityContext sc) {

        if (sc != null && sc.getUserPrincipal() != null) {
            try {
                KeycloakPrincipal kp = (KeycloakPrincipal)sc.getUserPrincipal();
                return kp.getKeycloakSecurityContext().getToken();
            } catch(Exception e) {
                LOG.warn("Something appears to be wrong with the security configuration, UserPrincipal is not as expected.");
            }
        }
        return null;
    }

    public static boolean hasRole(SecurityContext sc, String role) {

        AccessToken token = getToken(sc);

        if (token != null) {
            for (String userRole : token.getRealmAccess().getRoles()) {
                if (userRole.equalsIgnoreCase(role)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static EndUser getCurrentUser(SecurityContext sc) {
        EndUser endUser = null;
        AccessToken token = getToken(sc);
        if(token != null) {
            endUser = new EndUser(
                    UUID.fromString(token.getSubject()),   // subject == keycloak user id
                    null,   // no title attribute, TODO: add custom user attribute to Keylcloak??
                    token.getGivenName(),
                    token.getFamilyName(),
                    token.getEmail(),
                    RoleUtils.isSuperUser(sc)
            );
        }
        return endUser;
    }
}
