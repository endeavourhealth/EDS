package org.endeavourhealth.patientexplorer.database;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
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
                String codingDbJson = ConfigManager.getConfiguration("coding");
                JsonNode codingDb = ObjectMapperPool.getInstance().readTree(codingDbJson);

                Map<String, Object> override = new HashMap<>();

                if (codingDb.has("url"))
                    override.put("hibernate.hikari.dataSource.url", codingDb.get("url").asText());
                if (codingDb.has("username"))
                    override.put("hibernate.hikari.dataSource.user", codingDb.get("username").asText());
                if (codingDb.has("password"))
                    override.put("hibernate.hikari.dataSource.password", codingDb.get("password").asText());

                emFactory = Persistence.createEntityManagerFactory("coding", override);
            }
            catch (Exception e) {
                LOG.error("Error initializing persistence manager", e);
            }
        }

        return emFactory;
    }
}
