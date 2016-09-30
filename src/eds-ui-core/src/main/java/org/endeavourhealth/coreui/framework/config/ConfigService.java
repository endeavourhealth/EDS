package org.endeavourhealth.coreui.framework.config;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.core.cache.ObjectMapperPool;
import org.endeavourhealth.core.data.config.ConfigurationRepository;
import org.endeavourhealth.core.data.config.models.ConfigurationResource;
import org.endeavourhealth.coreui.framework.config.models.AppConfig;
import org.endeavourhealth.coreui.framework.config.models.AuthConfig;
import org.endeavourhealth.coreui.framework.config.models.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

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
            try {
                Config config = ConfigSerializer.getConfig();
                ConfigurationResource configurationResource = new ConfigurationRepository().getByKey(UUID.fromString(config.getWebServer().getAppConfigId()));
                appConfig = ObjectMapperPool.getInstance().readValue(configurationResource.getConfigurationData(), AppConfig.class);

            } catch (Exception e) {
                LOG.error("Configuration Repository error", e);
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
                Config config = ConfigSerializer.getConfig();
                keycloakConfig = new ConfigurationRepository().getByKey(UUID.fromString(config.getWebServer().getAuthConfigId()));

                JsonNode json = ObjectMapperPool.getInstance().readTree(keycloakConfig.getConfigurationData());

                authConfig = new AuthConfig(
                        json.get("realm").asText(),
                        json.get("auth-server-url").asText(),
                        json.get("resource").asText(),
                        getAppConfig().getAppUrl()
                );

            } catch (Exception e) {
                LOG.error("Configuration Repository error", e);
            }

            if (keycloakConfig == null) {

                // development fallback
                LOG.warn("Falling back to default AuthConfig properties...");
                authConfig = new AuthConfig(
                        "endeavour",
                        //"https://keycloak.eds.c.healthforge.io/auth",
                        "http://localhost:9080/auth",
                        "eds-ui",
                        "http://localhost:8080"
                );
            }
        }

        return authConfig;
    }

}
