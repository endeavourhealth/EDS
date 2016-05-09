package org.endeavourhealth.messagingapi.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.endeavourhealth.core.audit.AuditEvent;
import org.endeavourhealth.core.audit.AuditWriter;
import org.endeavourhealth.core.logging.CassandraDbAppender;
import org.endeavourhealth.core.engineConfiguration.EngineConfigurationSerializer;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.HashMap;

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
