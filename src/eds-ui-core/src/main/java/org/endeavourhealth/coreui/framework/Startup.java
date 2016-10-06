package org.endeavourhealth.coreui.framework;

import org.endeavourhealth.core.engineConfiguration.EngineConfigurationSerializer;
import org.endeavourhealth.coreui.framework.config.ConfigSerializer;
import org.endeavourhealth.coreui.framework.config.models.Config;
import org.endeavourhealth.coreui.framework.config.models.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public final class Startup implements ServletContextListener {
    private static final Logger LOG = LoggerFactory.getLogger(EngineConfigurationSerializer.class);

    public void contextInitialized(ServletContextEvent contextEvent) {
        Config config = ConfigSerializer.getConfig();

        String propertyName = contextEvent.getServletContext().getInitParameter("engine_config_property");
        if (propertyName == null || propertyName.isEmpty())
            propertyName = "eds.engineConfiguration";

        //load common config
        try {
            EngineConfigurationSerializer.loadConfigFromPropertyIfPossible(propertyName);
        } catch (Exception e) {
            LOG.error("Error loading engine config", e);
        }

        //load local config
        WebServer ws = config.getWebServer();
    }

    public void contextDestroyed(ServletContextEvent contextEvent) {
    }


}
