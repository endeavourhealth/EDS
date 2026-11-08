package org.endeavourhealth.queuereader.routines;

import org.endeavourhealth.common.security.keycloak.client.KeycloakAdminClient;
import org.endeavourhealth.core.database.dal.usermanager.caching.UserCache;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SD518 {
    private static final Logger LOG = LoggerFactory.getLogger(SD518.class);

    public static void lookUpUser(String userUuid) {
        LOG.debug("Looking up user details for [" + userUuid + "]");

        try {
            LOG.info("----using user cache-------------------------------------");
            UserRepresentation user = UserCache.getUserDetails(userUuid);
            if (user == null) {
                LOG.error("No user found");
            } else {
                LOG.debug("Found user " + user.getFirstName() + " " + user.getUsername() + " = " + user.getUsername());
            }

            LOG.info("----using inline codee-------------------------------------");
            KeycloakAdminClient keycloakClient = new KeycloakAdminClient();
            user = keycloakClient.realms().users().getUser(userUuid);

            //SD-518 - This bug meant it was impossible to edit machine-realm users in User Manager
            //this defaults to the default realm only so just doesn't work for any user in the "machine" realm
            //so as a quick fix, I'm changing it to iterate through all known realms if the original lookup fails
            if (user == null) {
                LOG.trace("Failed to find user " + userUuid + " in default realm so checking other realms");
                List<RealmRepresentation> realms = keycloakClient.realms().getRealms();
                for (RealmRepresentation realm : realms) {
                    String realmStr = realm.getRealm();
                    LOG.trace("Checking realm [" + realmStr + "]");
                    user = keycloakClient.realms().users().getUser(realmStr, userUuid);
                    if (user != null) {
                        LOG.trace("Found user " + userUuid + " in realm " + realmStr);
                        break;
                    }
                }
            }
            if (user == null) {
                LOG.error("No user found");
            } else {
                LOG.debug("Found user " + user.getFirstName() + " " + user.getUsername() + " = " + user.getUsername());
            }

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }
}
