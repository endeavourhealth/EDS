package org.endeavourhealth.ui.framework;

import org.endeavourhealth.core.engineConfiguration.EngineConfigurationSerializer;
import org.endeavourhealth.ui.database.PersistenceManager;
import org.endeavourhealth.ui.email.EmailProvider;
import org.endeavourhealth.ui.framework.config.ConfigSerializer;
import org.endeavourhealth.ui.framework.config.models.*;
import org.endeavourhealth.ui.framework.security.SecurityConfig;
import org.endeavourhealth.ui.utility.MessagingQueueProvider;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.List;

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
        String cookieDomain = ws.getCookieDomain();
        SecurityConfig.AUTH_COOKIE_VALID_DOMAIN = cookieDomain;

    }

    public void contextDestroyed(ServletContextEvent contextEvent) {

        PersistenceManager.INSTANCE.close();

    }


}
