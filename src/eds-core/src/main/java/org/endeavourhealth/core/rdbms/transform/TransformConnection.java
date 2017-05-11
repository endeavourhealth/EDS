package org.endeavourhealth.core.rdbms.transform;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.config.ConfigManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransformConnection {

    private static Map<String, EntityManagerFactory> entityManagerFactoryMap = new ConcurrentHashMap<>();
    //private static EntityManagerFactory entityManagerFactory;

    public static EntityManager getEntityManager(String enterpriseConfigName) throws Exception {

        EntityManagerFactory entityManagerFactory = entityManagerFactoryMap.get(enterpriseConfigName);
        if (entityManagerFactory == null
                || !entityManagerFactory.isOpen()) {
            createEntityManager(enterpriseConfigName);

            //get it again
            entityManagerFactory = entityManagerFactoryMap.get(enterpriseConfigName);
        }

        return entityManagerFactory.createEntityManager();
    }

    private static synchronized void createEntityManager(String enterpriseConfigName) throws Exception {

        EntityManagerFactory entityManagerFactory = entityManagerFactoryMap.get(enterpriseConfigName);
        if (entityManagerFactory != null
                && entityManagerFactory.isOpen()) {
            return;
        }

        JsonNode json = ConfigManager.getConfigurationAsJson(enterpriseConfigName, "enterprise");
        String url = json.get("transform_url").asText();
        String user = json.get("transform_username").asText();
        String pass = json.get("transform_password").asText();

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        properties.put("hibernate.hikari.dataSource.url", url);
        properties.put("hibernate.hikari.dataSource.user", user);
        properties.put("hibernate.hikari.dataSource.password", pass);

        entityManagerFactory = Persistence.createEntityManagerFactory("TransformDb", properties);
        entityManagerFactoryMap.put(enterpriseConfigName, entityManagerFactory);
    }

    /*public static EntityManager getEntityManager() throws Exception {

        if (entityManagerFactory == null
                || !entityManagerFactory.isOpen()) {
            createEntityManager();
        }

        return entityManagerFactory.createEntityManager();
    }

    private static synchronized void createEntityManager() throws Exception {

        if (entityManagerFactory != null
                && entityManagerFactory.isOpen()) {
            return;
        }

        JsonNode json = ConfigManager.getConfigurationAsJson("transform_db");
        String url = json.get("url").asText();
        String user = json.get("username").asText();
        String pass = json.get("password").asText();

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        properties.put("hibernate.hikari.dataSource.url", url);
        properties.put("hibernate.hikari.dataSource.user", user);
        properties.put("hibernate.hikari.dataSource.password", pass);

        entityManagerFactory = Persistence.createEntityManagerFactory("TransformDb", properties);
    }*/
}
