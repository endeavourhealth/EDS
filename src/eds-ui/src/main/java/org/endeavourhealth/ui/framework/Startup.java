package org.endeavourhealth.ui.framework;

import org.endeavourhealth.core.engineConfiguration.EngineConfigurationSerializer;
import org.endeavourhealth.ui.database.PersistenceManager;
import org.endeavourhealth.ui.framework.config.ConfigSerializer;
import org.endeavourhealth.ui.framework.config.models.Config;
import org.endeavourhealth.ui.framework.config.models.WebServer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public final class Startup implements ServletContextListener {

    public void contextInitialized(ServletContextEvent contextEvent) {

        Config config = ConfigSerializer.getConfig();

        //load common config
        try {
            EngineConfigurationSerializer.loadConfigFromPropertyIfPossible("eds.uiEngineConfiguration");
        } catch (Exception e) {
        }

        //domain for our cookies
        WebServer ws = config.getWebServer();
    }

    public void contextDestroyed(ServletContextEvent contextEvent) {

        PersistenceManager.INSTANCE.close();

    }


}
