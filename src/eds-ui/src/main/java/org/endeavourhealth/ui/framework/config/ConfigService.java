package org.endeavourhealth.ui.framework.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.endeavourhealth.core.data.config.ConfigurationRepository;
import org.endeavourhealth.core.data.config.models.ConfigurationResource;
import org.endeavourhealth.ui.framework.config.models.AppConfig;
import org.endeavourhealth.ui.framework.config.models.AuthConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigService.class);

    private static ConfigService instance;

    public static ConfigService instance() {
        if(instance == null) {
            instance = new ConfigService();
        }
        return instance;
    }

    private AuthConfig authConfig;
    private AppConfig appConfig;

    public AppConfig getAppConfig() {

        if(appConfig == null) {
            appConfig = new AppConfig();
            appConfig.setAppUrl("http://localhost:8080");                 // TODO: put in database config
        }

        return appConfig;
    }

    public AuthConfig getAuthConfig() {

        if(authConfig == null) {

            ConfigurationResource keycloakConfig = null;

            try {
                keycloakConfig = new ConfigurationRepository().getByKey(ConfigurationRepository.KEYCLOAK_CONFIG);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode json = objectMapper.readTree(keycloakConfig.getConfigurationData());

                authConfig = new AuthConfig(
                        json.get("realm").asText(),
                        json.get("auth-server-url").asText(),
                        json.get("resource").asText(),
                        getAppConfig().getAppUrl()
                );

            } catch (Exception e) {
                LOG.error("Configuration Repository error: {}", e.getMessage());
            }

            if (keycloakConfig == null) {

                // debug fallback
                authConfig = new AuthConfig(
                        "endeavour",
                        "http://localhost:9080/auth",
                        "eds-ui",
                        "http://localhost:8080"
                );
            }
        }

        return authConfig;
    }

}
