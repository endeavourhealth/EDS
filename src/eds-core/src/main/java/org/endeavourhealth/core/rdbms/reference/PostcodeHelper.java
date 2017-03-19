package org.endeavourhealth.core.rdbms.reference;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import org.endeavourhealth.common.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

public class PostcodeHelper {
    private static final Logger LOG = LoggerFactory.getLogger(PostcodeHelper.class);

    private static EntityManagerFactory entityManagerFactory;


    public static void save(PostcodeReference postcodeReference) throws Exception {
        EntityManager entityManager = getEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(postcodeReference);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    public static PostcodeReference getPostcodeReference(String postcode) throws Exception {

        //if called with an empty postcode, just return null
        if (Strings.isNullOrEmpty(postcode)) {
            return null;
        }

        //we force everything to upper case when creating the table, so do that now
        postcode = postcode.toUpperCase();

        //because we've got no guarantee how/where the raw postcodes are spaced, we use the string without spaces as our primary key
        postcode = postcode.replaceAll(" ", "");

        String sql = "select r"
                   + " from PostcodeReference r"
                   + " where r.postcodeNoSpace = :postcodeNoSpace";

        EntityManager entityManager = getEntityManager();

        Query query = entityManager
                .createQuery(sql, PostcodeReference.class)
                .setParameter("postcodeNoSpace", postcode);

        try {
            return (PostcodeReference)query.getSingleResult();

        } catch (NoResultException e) {
            return null;

        } finally {
            entityManager.close();
        }
    }


    private static EntityManager getEntityManager() throws Exception {

        if (entityManagerFactory == null
                || !entityManagerFactory.isOpen()) {
            createEntityManager();
        }

        return entityManagerFactory.createEntityManager();
    }

    private static synchronized void createEntityManager() throws Exception {

        JsonNode json = ConfigManager.getConfigurationAsJson("reference_db");
        String url = json.get("url").asText();
        String user = json.get("username").asText();
        String pass = json.get("password").asText();

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        properties.put("hibernate.hikari.dataSource.url", url);
        properties.put("hibernate.hikari.dataSource.user", user);
        properties.put("hibernate.hikari.dataSource.password", pass);

        entityManagerFactory = Persistence.createEntityManagerFactory("ReferenceDB", properties);
    }

}
