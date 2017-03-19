package org.endeavourhealth.enterprise;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.rdbms.transform.EnterpriseAge;
import org.endeavourhealth.core.rdbms.transform.EnterpriseAgeUpdater;
import org.endeavourhealth.core.rdbms.transform.TransformConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Date;
import java.util.List;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    /**
     * utility to update the Age columns in Enterprise
     *
     * Usage
     * =================================================================================
     * No parameters
     */
    public static void main(String[] args) throws Exception {

        ConfigManager.Initialize("EnterpriseAgeUpdater");

        try {
            if (args.length != 0) {
                LOG.error("No parameters required.");
                return;
            }

            LOG.info("Age updater starting");

            EntityManager entityManager = TransformConnection.getEntityManager();
            List<EnterpriseAge> agesToUpdate = findAgesToUpdate(entityManager);
            LOG.info("Found " + agesToUpdate.size() + " ages to update");

            String currentConfigName = null;
            Connection currentConnection = null;

            int progress = 0;
            for (EnterpriseAge ageToUpdate: agesToUpdate) {

                Integer[] ages = EnterpriseAgeUpdater.calculateAgeValues(ageToUpdate);

                //see if we're on a different enterprise DB yet, loading the config if needed
                if (currentConfigName == null
                        || !currentConfigName.equalsIgnoreCase(ageToUpdate.getEnterpriseConfigName())) {

                    if (currentConnection != null) {
                        currentConnection.close();
                    }
                    currentConfigName = ageToUpdate.getEnterpriseConfigName();
                    JsonNode config = ConfigManager.getConfigurationAsJson(currentConfigName, "enterprise");
                    currentConnection = openConnection(config);
                }

                updateEnterprise(ageToUpdate.getEnterprisePatientId(), ages, currentConnection);

                //if we've successfully updated Enterprise, then it's time to save our updated map object
                //with the newly calculated date of next update
                entityManager.persist(ageToUpdate);

                progress ++;
                if (progress % 100 == 0) {
                    LOG.info("Done " + progress);
                }
            }

            if (currentConnection != null) {
                currentConnection.close();
            }

            entityManager.close();

            LOG.info("Age updates complete");

        } catch (Exception ex) {
            LOG.error("", ex);
        }
    }

    private static Connection openConnection(JsonNode config) throws Exception {

        String driverClass = config.get("driverClass").asText();
        String url = config.get("url").asText();
        String username = config.get("username").asText();
        String password = config.get("password").asText();

        //force the driver to be loaded
        Class.forName(driverClass);

        Connection conn = DriverManager.getConnection(url, username, password);
        conn.setAutoCommit(false);

        return conn;
    }

    private static void updateEnterprise(long enterprisePatientId, Integer[] ages, Connection connection) throws Exception {

        //the enterprise patient database isn't managed using hibernate, so we need to simply write a simple update statement
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE patient SET ");
        sb.append("age_years = ?, ");
        sb.append("age_months = ?, ");
        sb.append("age_weeks = ? ,");
        sb.append("WHERE id = ?");

        PreparedStatement update = connection.prepareStatement(sb.toString());

        if (ages[EnterpriseAgeUpdater.UNIT_YEARS] == null) {
            update.setNull(1, Types.INTEGER);
        } else {
            update.setInt(1, ages[EnterpriseAgeUpdater.UNIT_YEARS]);
        }

        if (ages[EnterpriseAgeUpdater.UNIT_MONTHS] == null) {
            update.setNull(2, Types.INTEGER);
        } else {
            update.setInt(2, ages[EnterpriseAgeUpdater.UNIT_MONTHS]);
        }

        if (ages[EnterpriseAgeUpdater.UNIT_WEEKS] == null) {
            update.setNull(3, Types.INTEGER);
        } else {
            update.setInt(3, ages[EnterpriseAgeUpdater.UNIT_WEEKS]);
        }

        update.setLong(4, enterprisePatientId);

        update.addBatch();
        update.executeBatch();

        connection.commit();
    }

    private static List<EnterpriseAge> findAgesToUpdate(EntityManager entityManager) {

        String sql = "select c"
                + " from"
                + " EnterpriseAge c"
                + " where c.dateNextChange <= :dateNextChange"
                + " order by c.enterpriseConfigName";


        Query query = entityManager.createQuery(sql, EnterpriseAge.class)
                .setParameter("dateNextChange", new Date());

        return query.getResultList();
    }

}
