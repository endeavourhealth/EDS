package org.endeavourhealth.patientexplorer.database;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.core.cache.ObjectMapperPool;
import org.endeavourhealth.core.data.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public enum PersistenceManager {
    INSTANCE;
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
                String logbackDbJson = ConfigManager.getConfiguration("coding");
                JsonNode logbackDb = ObjectMapperPool.getInstance().readTree(logbackDbJson);

                Map<String, Object> override = new HashMap<>();

                if (logbackDb.has("url"))
                    override.put("hibernate.connection.url", logbackDb.get("url").asText());
                if (logbackDb.has("username"))
                    override.put("hibernate.connection.username", logbackDb.get("username").asText());
                if (logbackDb.has("password"))
                    override.put("hibernate.connection.password", logbackDb.get("password").asText());

                emFactory = Persistence.createEntityManagerFactory("coding", override);
            }
            catch (Exception e) {
                LOG.error("Error initializing persistence manager", e);
            }
        }

        return emFactory;
    }
}
