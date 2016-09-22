package org.endeavourhealth.ui.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public enum PersistenceManager {
    INSTANCE;
    private static final String JDBC_URL_ENV_VAR = "LOGBACK_JDBC_URL";
    private static final String JDBC_USER_ENV_VAR = "LOGBACK_JDBC_USERNAME";
    private static final String JDBC_PASSWORD_ENV_VAR = "LOGBACK_JDBC_PASSWORD";
    private static final Logger LOG = LoggerFactory.getLogger(PersistenceManager.class);

    private EntityManagerFactory emFactory;


    public EntityManager getEntityManager() {
        return getEmFactory().createEntityManager();
    }

    public void close() {
        getEmFactory().close();
    }

    private synchronized EntityManagerFactory getEmFactory() {
        if (emFactory == null) {
            try {
                Map<String, String> envVars = System.getenv();
                Map<String, Object> override = new HashMap<>();

                if (envVars.containsKey(JDBC_URL_ENV_VAR))
                    override.put("hibernate.connection.url", envVars.get(JDBC_URL_ENV_VAR));
                if (envVars.containsKey(JDBC_USER_ENV_VAR))
                    override.put("hibernate.connection.username", envVars.get(JDBC_USER_ENV_VAR));
                if (envVars.containsKey(JDBC_PASSWORD_ENV_VAR))
                    override.put("hibernate.connection.password", envVars.get(JDBC_PASSWORD_ENV_VAR));

                emFactory = Persistence.createEntityManagerFactory("NewPersistenceUnit", override);
            }
            catch (Exception e) {
                LOG.error("Error initializing persistence manager", e);
            }
        }

        return emFactory;
    }
}
