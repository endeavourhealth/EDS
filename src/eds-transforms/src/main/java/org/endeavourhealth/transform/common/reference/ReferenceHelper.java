package org.endeavourhealth.transform.common.reference;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import org.endeavourhealth.common.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public class ReferenceHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ReferenceHelper.class);

    private static EntityManagerFactory _connection;


    public static PostcodeReference getPostcodeReference(String postcode) throws Exception {

        return null;

        //because we've got no guarantee how/where the raw postcodes are spaced, we use the string
        //without spaces as our primary key
        /*postcode = postcode.replaceAll(" ", "");

        //if called with an empty postcode, just return null
        if (Strings.isNullOrEmpty(postcode)) {
            return null;
        }

        String sql = " select r"
                   + " from "
                   + "    PostcodeReference r"
                   + " where r.postcode_no_space = :postcode_no_space";

        EntityManager entityManager = getConnection().createEntityManager();
        Query query = entityManager
                .createQuery(sql, PostcodeReference.class)
                .setParameter("postcode_no_space", postcode);

        try {
            PostcodeReference ret = (PostcodeReference)query.getSingleResult();
            return ret;

        } catch (Exception e) {
            LOG.warn("No postcode reference found for postcode " + postcode);
            return null;
        } finally {
            entityManager.close();
        }*/

    }

    private static EntityManagerFactory createDatabaseConnection() throws Exception {

        JsonNode json = ConfigManager.getConfigurationAsJson("reference");
        String driverClass = json.get("driverClass").asText();
        String url = json.get("url").asText();
        String username = json.get("username").asText();
        String password = json.get("password").asText();

        Map<String, Object> properties = new HashMap<>();

        properties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        properties.put("hibernate.hikari.dataSource.url", url);

        if (!Strings.isNullOrEmpty(username)) {
            properties.put("hibernate.hikari.dataSource.user", username);
        }
        if (!Strings.isNullOrEmpty(password)) {
            properties.put("hibernate.hikari.dataSource.password", password);
        }

        LOG.trace("Config manager connecting to db...");
        return Persistence.createEntityManagerFactory("ReferenceDb", properties);
    }

    private static EntityManagerFactory getConnection() throws Exception {

        // Has the config manager been initialized?
        if (_connection == null) {
            throw new IllegalStateException("Configuration manager not initialized");
        }


        // Has the connection closed?
        if (!_connection.isOpen()) {
            LOG.info("Config DB connection closed - reconnecting...");
            _connection = null;
            // Attempt to reconnect
            createDatabaseConnection();
            if (_connection == null)
                throw new IllegalStateException("Unable to reconnect to database");
        }


        return _connection;
    }
}
