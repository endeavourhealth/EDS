package org.endeavour.eds.test;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.endeavourhealth.common.security.OrgRoles;
import org.endeavourhealth.common.security.keycloak.client.KeycloakClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AuthHelper {
    private static final Logger LOG = LoggerFactory.getLogger(AuthHelper.class);

    private static boolean isInit = false;

    public static void auth() {
        auth(false, "admin", "Test1234");
    }

    public static void logout() {
        synchronized (AuthHelper.class) {
            isInit = false;
        }
    }

    public static void auth(boolean forceInit, String username, String password) {
        synchronized (AuthHelper.class) {
            if (!isInit || forceInit) {
                KeycloakClient.init("https://auth.endeavourhealth.net/auth", "endeavour", username, password, "eds-ui");
                LOG.info("Authenticated, user = {}", username);
                isInit = true;
            }
        }
    }

    public static Header getDefaultOrgHeader() {
        return new BasicHeader(OrgRoles.HEADER_ORGANISATION_ID, OrgRoles.ROOT_ORGANISATION_ID);
    }

    public static Header getUnauthorisedOrgHeader() {
        // this org id doesn't exist and should never be authorised
        return new BasicHeader(OrgRoles.HEADER_ORGANISATION_ID, "c5c968d3-9999-9999-9999-999999999999");
    }
}
