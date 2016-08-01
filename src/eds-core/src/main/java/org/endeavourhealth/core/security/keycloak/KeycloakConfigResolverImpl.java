package org.endeavourhealth.core.security.keycloak;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.OIDCHttpFacade;

/**
 * Provides the keycloak configuration file to the Tomcat adapter.
 *
 * Multi-tenant and special config can be injected here.
 */
public class KeycloakConfigResolverImpl implements KeycloakConfigResolver {

    @Override
    public KeycloakDeployment resolve(OIDCHttpFacade.Request request) {

        return KeycloakDeploymentBuilder.build(getClass().getResourceAsStream("/keycloak.json"));    // TODO: store in database as config
    }

}