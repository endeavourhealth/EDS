package org.endeavourhealth.core.security;

import javax.ws.rs.core.SecurityContext;

/**
 * This class provides security role utilities for Keycloak integration.
 */
public class RoleUtils {

    public static String ROLE_SUPERUSER = "eds_superuser";
    public static String ROLE_ADMIN_CRUD = "eds_admin_crud";
    public static String ROLE_ADMIN = "eds_admin";
    public static String ROLE_USER_CRUD = "eds_user_crud";
    public static String ROLE_USER = "eds_user";
    public static String ROLE_MESSAGING_GET = "eds_messaging_get";
    public static String ROLE_MESSAGING_POST = "eds_messaging_post";

    public static boolean isUser(SecurityContext sc) {
        return SecurityUtils.hasRole(sc, ROLE_USER);
    }

    public static boolean isAdmin(SecurityContext sc) {
        return SecurityUtils.hasRole(sc, ROLE_ADMIN);
    }

    public static boolean isAdminCrud(SecurityContext sc) {
        return SecurityUtils.hasRole(sc, ROLE_ADMIN_CRUD);
    }

    public static boolean isUserCrud(SecurityContext sc) {
        return SecurityUtils.hasRole(sc, ROLE_USER_CRUD);
    }

    public static boolean isSuperUser(SecurityContext sc) {
        return SecurityUtils.hasRole(sc, ROLE_SUPERUSER);
    }

    public static boolean isMessagingGet(SecurityContext sc) {
        return SecurityUtils.hasRole(sc, ROLE_MESSAGING_GET);
    }

    public static boolean isMessagingPost(SecurityContext sc) {
        return SecurityUtils.hasRole(sc, ROLE_MESSAGING_POST);
    }
}
