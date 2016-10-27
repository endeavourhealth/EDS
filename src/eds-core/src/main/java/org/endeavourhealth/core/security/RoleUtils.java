package org.endeavourhealth.core.security;

import javax.ws.rs.core.SecurityContext;

/**
 * This class provides security role utilities for Keycloak integration.
 */
public class RoleUtils {

    public static String ROLE_EDS_SUPERUSER = "eds_superuser";
    public static String ROLE_EDS_ADMIN = "eds_admin";
    public static String ROLE_EDS_USER = "eds_user";

    public static boolean isEDSUser(SecurityContext sc) {
        return SecurityUtils.hasRole(sc, ROLE_EDS_USER);
    }

    public static boolean isEDSAdmin(SecurityContext sc) {
        return SecurityUtils.hasRole(sc, ROLE_EDS_ADMIN);
    }

    public static boolean isEDSSuperUser(SecurityContext sc) {
        return SecurityUtils.hasRole(sc, ROLE_EDS_SUPERUSER);
    }

}
