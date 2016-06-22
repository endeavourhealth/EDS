package org.endeavourhealth.patientui.framework;

import org.endeavourhealth.patientui.framework.config.ConfigSerializer;
import org.endeavourhealth.patientui.framework.config.models.Config;
import org.endeavourhealth.patientui.framework.config.models.WebServer;
import org.endeavourhealth.patientui.framework.security.SecurityConfig;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public final class Startup implements ServletContextListener {

    public void contextInitialized(ServletContextEvent contextEvent) {

        Config config = ConfigSerializer.getConfig();

        //domain for our cookies
        WebServer ws = config.getWebServer();
        String cookieDomain = ws.getCookieDomain();
        SecurityConfig.AUTH_COOKIE_VALID_DOMAIN = cookieDomain;

    }

    public void contextDestroyed(ServletContextEvent contextEvent) {
    }


}
