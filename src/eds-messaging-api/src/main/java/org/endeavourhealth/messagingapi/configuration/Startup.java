package org.endeavourhealth.messagingapi.configuration;

import org.endeavourhealth.core.logging.CassandraDbAppender;
import org.endeavourhealth.core.engineConfiguration.EngineConfigurationSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public final class Startup implements ServletContextListener {
    private static final Logger LOG = LoggerFactory.getLogger(Startup.class);

    public void contextInitialized(ServletContextEvent contextEvent) {

        try {
            ConfigManager.loadConfigurationFromProperty("eds.apiConfiguration");
        } catch (Exception e) {
            LOG.error("Error leading API configuration", e);
        }

        //load common config
        try {
            EngineConfigurationSerializer.loadConfigFromPropertyIfPossible("eds.apiEngineConfiguration");
        } catch (Exception e) {
            LOG.error("Error leading engine configuration", e);
        }

        //logging
        CassandraDbAppender.tryRegisterDbAppender();
        LOG.error("EDS API WebServer Startup Complete");

    }

    public void contextDestroyed(ServletContextEvent contextEvent) {
        //put any shutdown code here
    }


}
