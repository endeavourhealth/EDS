package org.endeavourhealth.core.security.keycloak;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.core.data.config.ConfigurationRepository;
import org.endeavourhealth.core.data.config.models.ConfigurationResource;
import org.endeavourhealth.core.security.KeycloakConfigUtils;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.OIDCHttpFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Provides the keycloak configuration file to the Tomcat adapter.
 *
 * Multi-tenant and special config can be injected here.
 */
public class KeycloakConfigResolverImpl implements KeycloakConfigResolver {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakConfigResolverImpl.class);

    private static KeycloakDeployment keycloakDeployment = null;

    @Override
    public KeycloakDeployment resolve(OIDCHttpFacade.Request request) {

        if(keycloakDeployment == null) {

            keycloakDeployment = KeycloakConfigUtils.getConfig(ConfigurationRepository.KEYCLOAK_CONFIG);

            if(keycloakDeployment == null) {
                LOG.warn("Cannot get Keycloak config from configuration repository, falling back to fixed internal configuration.");
                keycloakDeployment = KeycloakDeploymentBuilder.build(getClass().getResourceAsStream("/keycloak.json"));
            }
        }

        return keycloakDeployment;
    }

}