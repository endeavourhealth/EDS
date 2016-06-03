package org.endeavourhealth.ui.framework;

import org.endeavourhealth.core.engineConfiguration.EngineConfigurationSerializer;
import org.endeavourhealth.ui.email.EmailProvider;
import org.endeavourhealth.ui.framework.config.ConfigSerializer;
import org.endeavourhealth.ui.framework.config.models.*;
import org.endeavourhealth.ui.framework.security.SecurityConfig;
import org.endeavourhealth.ui.utility.MessagingQueueProvider;
import org.endeavourhealth.ui.database.DatabaseManager;

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

        //set up our DB
        String url = config.getDatabase().getUrl();
        String username = config.getDatabase().getUsername();
        String password = config.getDatabase().getPassword();
        DatabaseManager.getInstance().setConnectionProperties(url, username, password);

        //tell our database manager to set up logging to db
        DatabaseManager.getInstance().registerLogbackDbAppender();

        //domain for our cookies
        WebServer ws = config.getWebServer();
        String cookieDomain = ws.getCookieDomain();
        SecurityConfig.AUTH_COOKIE_VALID_DOMAIN = cookieDomain;

        //set up our email provision
        Email emailSettings = config.getEmail();
        if (emailSettings != null) {
            url = emailSettings.getUrl();
            username = emailSettings.getUsername();
            password = emailSettings.getPassword();
            List<Template> templates = emailSettings.getTemplate();
            EmailProvider.getInstance().setConnectionProperties(url, username, password, templates);
        }

        //messaging queue
        MessagingQueue queueSettings = config.getMessagingQueue();
        if (queueSettings != null) {
            url = queueSettings.getUrl();
            username = queueSettings.getUsername();
            password = queueSettings.getPassword();
            String queueName = queueSettings.getQueueName();
            MessagingQueueProvider.getInstance().setConnectionProperties(url, username, password, queueName);
        }

    }

    public void contextDestroyed(ServletContextEvent contextEvent) {
        DatabaseManager.getInstance().deregisterLogbackDbAppender();
    }


}
