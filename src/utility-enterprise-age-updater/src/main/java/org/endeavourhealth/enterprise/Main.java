package org.endeavourhealth.enterprise;

import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.enterprise.EnterpriseConnector;
import org.endeavourhealth.core.rdbms.transform.EnterpriseAge;
import org.endeavourhealth.core.rdbms.transform.EnterpriseAgeUpdater;
import org.endeavourhealth.core.rdbms.transform.TransformConnection;
import org.endeavourhealth.core.slack.SlackHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Connection;
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
            if (args.length != 1) {
                LOG.error("Parameter required: <enterprise config name>");
                return;
            }

            String enterpriseConfigName = args[0];
            LOG.info("Age updater starting for " + enterpriseConfigName);

            EntityManager entityManager = TransformConnection.getEntityManager(enterpriseConfigName);
            List<EnterpriseAge> agesToUpdate = findAgesToUpdate(entityManager);
            LOG.info("Found " + agesToUpdate.size() + " ages to update");

            Connection enterpriseConnection = EnterpriseConnector.openConnection(enterpriseConfigName);

            int progress = 0;
            for (EnterpriseAge ageToUpdate: agesToUpdate) {

                Integer[] ages = EnterpriseAgeUpdater.calculateAgeValues(ageToUpdate);

                updateEnterprise(ageToUpdate.getEnterprisePatientId(), ages, enterpriseConnection);

                //if we've successfully updated Enterprise, then it's time to save our updated map object
                //with the newly calculated date of next update
                entityManager.getTransaction().begin();
                entityManager.persist(ageToUpdate);
                entityManager.getTransaction().commit();

                progress ++;
                if (progress % 100 == 0) {
                    LOG.info("Done " + progress);
                }
            }

            if (enterpriseConnection != null) {
                enterpriseConnection.close();
            }

            entityManager.close();

            LOG.info("Age updates complete");

        } catch (Exception ex) {
            LOG.error("", ex);
            SlackHelper.sendSlackMessage(SlackHelper.Channel.ProductionAlerts, "Exception in Enterprise Age Updater", ex);
        }

        System.exit(0);
    }


    private static void updateEnterprise(long enterprisePatientId, Integer[] ages, Connection connection) throws Exception {

        //the enterprise patient database isn't managed using hibernate, so we need to simply write a simple update statement
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE patient SET ");
        sb.append("age_years = ?, ");
        sb.append("age_months = ?, ");
        sb.append("age_weeks = ? ");
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

        LOG.info("Updated patient " + enterprisePatientId + " to ages " + ages[EnterpriseAgeUpdater.UNIT_YEARS] + " y, " + ages[EnterpriseAgeUpdater.UNIT_MONTHS] + " m " + ages[EnterpriseAgeUpdater.UNIT_WEEKS] + " wks");
    }

    private static List<EnterpriseAge> findAgesToUpdate(EntityManager entityManager) {

        String sql = "select c"
                + " from"
                + " EnterpriseAge c"
                + " where c.dateNextChange <= :dateNextChange";


        Query query = entityManager.createQuery(sql, EnterpriseAge.class)
                .setParameter("dateNextChange", new Date());

        return query.getResultList();
    }

}
