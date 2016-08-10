package org.endeavourhealth.ui.framework.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.endeavourhealth.core.data.config.ConfigurationRepository;
import org.endeavourhealth.core.data.config.models.ConfigurationResource;
import org.endeavourhealth.ui.framework.config.models.AppConfig;
import org.endeavourhealth.ui.framework.config.models.AuthConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigService.class);

    private static String EDS_APP_CONFIG = "804e0ab5-f314-4e0e-a53c-fe117a4e85d4";

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
            try {
                ConfigurationResource configurationResource = new ConfigurationRepository().getByKey(UUID.fromString(ConfigService.EDS_APP_CONFIG));
                ObjectMapper objectMapper = new ObjectMapper();
                appConfig = objectMapper.readValue(configurationResource.getConfigurationData(), AppConfig.class);

            } catch (Exception e) {
                LOG.error("Configuration Repository error: {}", e.getMessage());
            }
        }

        if(appConfig == null) {

            // development fallback
            LOG.warn("Falling back to default AppConfig properties...");
            appConfig = new AppConfig();
            appConfig.setAppUrl("http://localhost:8080");
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

                // development fallback
                LOG.warn("Falling back to default AuthConfig properties...");
                authConfig = new AuthConfig(
                        "endeavour",
                        "https://keycloak.eds.c.healthforge.io/auth",
                        "eds-ui",
                        "http://localhost:8080"
                );
            }
        }

        return authConfig;
    }

}
