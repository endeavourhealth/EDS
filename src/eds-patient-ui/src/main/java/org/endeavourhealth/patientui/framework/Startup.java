package org.endeavourhealth.patientui.framework;

import org.endeavourhealth.core.engineConfiguration.EngineConfigurationSerializer;
import org.endeavourhealth.patientui.framework.config.ConfigSerializer;
import org.endeavourhealth.patientui.framework.config.models.Config;
import org.endeavourhealth.patientui.framework.config.models.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public final class Startup implements ServletContextListener {
    private static final Logger LOG = LoggerFactory.getLogger(EngineConfigurationSerializer.class);

    public void contextInitialized(ServletContextEvent contextEvent) {
        Config config = ConfigSerializer.getConfig();

        //load common config
        try {
            EngineConfigurationSerializer.loadConfigFromPropertyIfPossible("eds.patientUiEngineConfiguration");
        } catch (Exception e) {
            LOG.error("Error loading engine config", e);
        }

        //load local config
        WebServer ws = config.getWebServer();
    }

    public void contextDestroyed(ServletContextEvent contextEvent) {
    }


}
