package org.endeavourhealth.ui.database;

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

    private EntityManagerFactory emFactory;

    private PersistenceManager() {
        Map<String, String> envVars = System.getenv();
        Map<String, Object> override = new HashMap<>();

        if (envVars.containsKey(JDBC_URL_ENV_VAR)) override.put("hibernate.connection.url", envVars.get(JDBC_URL_ENV_VAR));
        if (envVars.containsKey(JDBC_USER_ENV_VAR)) override.put("hibernate.connection.username", envVars.get(JDBC_USER_ENV_VAR));
        if (envVars.containsKey(JDBC_PASSWORD_ENV_VAR)) override.put("hibernate.connection.password", envVars.get(JDBC_PASSWORD_ENV_VAR));

        emFactory = Persistence.createEntityManagerFactory("NewPersistenceUnit", override);
    }

    public EntityManager getEntityManager() {
        return emFactory.createEntityManager();
    }

    public void close() {
        emFactory.close();
    }
}
