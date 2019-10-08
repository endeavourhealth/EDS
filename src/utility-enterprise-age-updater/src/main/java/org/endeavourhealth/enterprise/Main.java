package org.endeavourhealth.enterprise;

import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.subscriberTransform.EnterpriseAgeUpdaterlDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.models.EnterpriseAge;
import org.endeavourhealth.core.database.rdbms.enterprise.EnterpriseConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
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

            EnterpriseAgeUpdaterlDalI enterpriseAgeUpdaterDal = DalProvider.factoryEnterpriseAgeUpdaterlDal(enterpriseConfigName);
            List<EnterpriseAge> agesToUpdate = enterpriseAgeUpdaterDal.findAgesToUpdate();
            LOG.info("Found " + agesToUpdate.size() + " ages to update");

            List<EnterpriseConnector.ConnectionWrapper> connectionWrappers = EnterpriseConnector.openConnection(enterpriseConfigName);

            //NOTE: the agesToUpdate list doesn't contain deceased patients, as they are deleted
            //out of the enterprise_age table when the date_of_death is set

            int progress = 0;
            for (EnterpriseAge ageToUpdate: agesToUpdate) {

                Integer[] ages = enterpriseAgeUpdaterDal.reCalculateAgeValues(ageToUpdate);

                for (EnterpriseConnector.ConnectionWrapper connectionWrapper: connectionWrappers) {
                    Connection enterpriseConnection = connectionWrapper.getConnection();

                    updateEnterprisePatient(ageToUpdate.getEnterprisePatientId(), ages, enterpriseConnection);

                    //no need to manually update the person table, as updates to patient will fire off a trigger that does it for us
                    //updateEnterprisePerson(ageToUpdate.getEnterprisePatientId(), ages, enterpriseConnection);
                    enterpriseConnection.close();
                }

                //if we've successfully updated Enterprise, then it's time to save our updated map object
                //with the newly calculated date of next update
                enterpriseAgeUpdaterDal.save(ageToUpdate);

                progress ++;
                if (progress % 100 == 0) {
                    LOG.info("Done " + progress);
                }
            }

            LOG.info("Age updates complete");

        } catch (Exception ex) {
            LOG.error("", ex);
            SlackHelper.sendSlackMessage(SlackHelper.Channel.EnterpriseAgeUpdaterAlerts, "Exception in Enterprise Age Updater", ex);
        }

        System.exit(0);
    }


    private static void updateEnterprisePatient(long enterprisePatientId, Integer[] ages, Connection connection) throws Exception {

        //the enterprise patient database isn't managed using hibernate, so we need to simply write a simple update statement
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE patient SET ");
        sb.append("age_years = ?, ");
        sb.append("age_months = ?, ");
        sb.append("age_weeks = ? ");
        sb.append("WHERE id = ?");

        PreparedStatement update = connection.prepareStatement(sb.toString());

        if (ages[EnterpriseAge.UNIT_YEARS] == null) {
            update.setNull(1, Types.INTEGER);
        } else {
            update.setInt(1, ages[EnterpriseAge.UNIT_YEARS]);
        }

        if (ages[EnterpriseAge.UNIT_MONTHS] == null) {
            update.setNull(2, Types.INTEGER);
        } else {
            update.setInt(2, ages[EnterpriseAge.UNIT_MONTHS]);
        }

        if (ages[EnterpriseAge.UNIT_WEEKS] == null) {
            update.setNull(3, Types.INTEGER);
        } else {
            update.setInt(3, ages[EnterpriseAge.UNIT_WEEKS]);
        }

        update.setLong(4, enterprisePatientId);

        update.addBatch();
        update.executeBatch();

        connection.commit();

        //LOG.info("Updated patient " + enterprisePatientId + " to ages " + ages[EnterpriseAge.UNIT_YEARS] + " y, " + ages[EnterpriseAge.UNIT_MONTHS] + " m " + ages[EnterpriseAge.UNIT_WEEKS] + " wks");
    }

    /*private static void updateEnterprisePerson(long enterprisePatientId, Integer[] ages, Connection connection) throws Exception {

        //update the age fields on the person table where the person is for our patient and their pseudo IDs match
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE patient, person SET ");
        sb.append("person.age_years = ?, ");
        sb.append("person.age_months = ?, ");
        sb.append("person.age_weeks = ? ");
        sb.append("WHERE patient.id = ? ");
        sb.append("AND patient.person_id = person.id ");
        sb.append("AND patient.pseudo_id = person.pseudo_id");

        PreparedStatement update = connection.prepareStatement(sb.toString());

        if (ages[EnterpriseAge.UNIT_YEARS] == null) {
            update.setNull(1, Types.INTEGER);
        } else {
            update.setInt(1, ages[EnterpriseAge.UNIT_YEARS]);
        }

        if (ages[EnterpriseAge.UNIT_MONTHS] == null) {
            update.setNull(2, Types.INTEGER);
        } else {
            update.setInt(2, ages[EnterpriseAge.UNIT_MONTHS]);
        }

        if (ages[EnterpriseAge.UNIT_WEEKS] == null) {
            update.setNull(3, Types.INTEGER);
        } else {
            update.setInt(3, ages[EnterpriseAge.UNIT_WEEKS]);
        }

        update.setLong(4, enterprisePatientId);

        update.addBatch();
        update.executeBatch();

        connection.commit();
    }*/



}
