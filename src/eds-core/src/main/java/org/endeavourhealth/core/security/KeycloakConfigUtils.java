package org.endeavourhealth.core.security;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.core.data.config.ConfigurationRepository;
import org.endeavourhealth.core.data.config.models.ConfigurationResource;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class KeycloakConfigUtils {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakConfigUtils.class);

    public static KeycloakDeployment getConfig(UUID configKey) {
        ConfigurationResource keycloakConfig = null;

        try {
            keycloakConfig = new ConfigurationRepository().getByKey(configKey);
        } catch (Exception e) {
            LOG.error("Configuration Repository error", e);
        }

        if (keycloakConfig != null && StringUtils.isNotEmpty(keycloakConfig.getConfigurationData())) {
            InputStream stream = new ByteArrayInputStream(keycloakConfig.getConfigurationData().getBytes(StandardCharsets.UTF_8));
            return KeycloakDeploymentBuilder.build(stream);
        }

        return null;
    }
}
