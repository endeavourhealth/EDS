package org.endeavour.eds.test;

import org.endeavourhealth.core.security.keycloak.client.KeycloakClient;

public class AuthHelper {
    private static boolean isInit = false;

    public static void auth() {
        synchronized (AuthHelper.class) {
            if (!isInit) {
                KeycloakClient.init("http://localhost:9080/auth", "endeavour", "admin", "Test1234", "eds-ui");
                isInit = true;
            }
        }
    }
}
