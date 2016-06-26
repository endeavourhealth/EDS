package org.endeavourhealth.patientui.framework;

import org.endeavourhealth.core.data.logging.LogbackCassandraAppender;
import org.endeavourhealth.core.engineConfiguration.EngineConfiguration;
import org.endeavourhealth.core.engineConfiguration.EngineConfigurationSerializer;
import org.endeavourhealth.patientui.framework.config.ConfigSerializer;
import org.endeavourhealth.patientui.framework.config.models.Config;
import org.endeavourhealth.patientui.framework.config.models.WebServer;
import org.endeavourhealth.patientui.framework.security.SecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public final class Startup implements ServletContextListener {
    private static final Logger LOG = LoggerFactory.getLogger(EngineConfigurationSerializer.class);

    public void contextInitialized(ServletContextEvent contextEvent) {

        //load common config
        try {
            EngineConfigurationSerializer.loadConfigFromPropertyIfPossible("eds.patientUiEngineConfiguration");
        } catch (Exception e) {
            LOG.error("Error loading engine config", e);
        }

        //load local config
        Config config = ConfigSerializer.getConfig();
        WebServer ws = config.getWebServer();
        String cookieDomain = ws.getCookieDomain();
        SecurityConfig.AUTH_COOKIE_VALID_DOMAIN = cookieDomain;

        //start off Cassandra DB logging
        LogbackCassandraAppender.tryRegisterDbAppender();
    }

    public void contextDestroyed(ServletContextEvent contextEvent) {
    }


}
