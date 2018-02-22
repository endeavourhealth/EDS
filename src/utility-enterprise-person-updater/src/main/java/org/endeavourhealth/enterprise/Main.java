package org.endeavourhealth.enterprise;

import org.apache.commons.io.FilenameUtils;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.eds.PatientLinkDalI;
import org.endeavourhealth.core.database.dal.eds.models.PatientLinkPair;
import org.endeavourhealth.core.database.dal.subscriberTransform.EnterpriseIdDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.EnterprisePersonUpdaterHistoryDalI;
import org.endeavourhealth.core.database.rdbms.enterprise.EnterpriseConnector;
import org.endeavourhealth.transform.enterprise.outputModels.AbstractEnterpriseCsvWriter;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
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

        ConfigManager.Initialize("EnterprisePersonUpdater");

        try {
            if (args.length != 1) {
                LOG.error("Parameter required: <enterprise config name>");
                return;
            }

            String enterpriseConfigName = args[0];
            LOG.info("Person updater starting for " + enterpriseConfigName);

            //create this date BEFORE we get the date we last run, so there's no risk of a gap
            Date dateNextRun = new Date();

            EnterprisePersonUpdaterHistoryDalI enterprisePersonUpdaterHistoryDal = DalProvider.factoryEnterprisePersonUpdateHistoryDal(enterpriseConfigName);
            Date dateLastRun = enterprisePersonUpdaterHistoryDal.findDatePersonUpdaterLastRun();
            LOG.info("Looking for Person ID changes since " + dateLastRun);

            PatientLinkDalI patientLinkDal = DalProvider.factoryPatientLinkDal();
            List<PatientLinkPair> changes = patientLinkDal.getChangesSince(dateLastRun);

            //strip out any that are just telling us NEW person IDs
            for (int i=changes.size()-1; i>=0; i--) {
                PatientLinkPair change = changes.get(i);
                if (change.getPreviousPersonId() == null) {
                    changes.remove(i);
                }
            }
            LOG.info("Found " + changes.size() + " changes in Person ID");

            //find the Enterprise Person ID for each of the changes, hashing them by the enterprise instance they're on
            List<UpdateJob> updates = convertChangesToEnterprise(enterpriseConfigName, changes);

            Connection connection = EnterpriseConnector.openConnection(enterpriseConfigName);

            LOG.info("Updating " + updates.size() + " person IDs on " + enterpriseConfigName);

            try {
                for (UpdateJob update: updates) {
                    changePersonId(update, connection);
                }

            } finally {
                connection.close();
            }

            enterprisePersonUpdaterHistoryDal.updatePersonUpdaterLastRun(dateNextRun);

            LOG.info("Person updates complete");

        } catch (Exception ex) {
            LOG.error("", ex);
            SlackHelper.sendSlackMessage(SlackHelper.Channel.EnterprisePersonUpdaterAlerts, "Exception in Enterprise Person Updater", ex);
        }

        System.exit(0);
    }


    private static List<UpdateJob> convertChangesToEnterprise(String enterpriseConfigName, List<PatientLinkPair> changes) throws Exception {
        List<UpdateJob> updatesForConfig = new ArrayList<>();

        for (PatientLinkPair change: changes) {

            String oldDiscoveryPersonId = change.getPreviousPersonId();
            String newDiscoveryPersonId = change.getNewPersonId();
            String discoveryPatientId = change.getPatientId();

            EnterpriseIdDalI enterpriseIdDalI = DalProvider.factoryEnterpriseIdDal(enterpriseConfigName);
            Long enterprisePatientId = enterpriseIdDalI.findEnterpriseId(ResourceType.Patient.toString(), discoveryPatientId);

            //if this patient has never gone to enterprise, then skip it
            if (enterprisePatientId == null) {
                continue;
            }

            List<Long> mappings = enterpriseIdDalI.findEnterprisePersonIdsForPersonId(oldDiscoveryPersonId);
            for (Long oldEnterprisePersonId: mappings) {
                Long newEnterprisePersonId = enterpriseIdDalI.findOrCreateEnterprisePersonId(newDiscoveryPersonId);

                updatesForConfig.add(new UpdateJob(enterprisePatientId, oldEnterprisePersonId, newEnterprisePersonId));
            }
        }

        return updatesForConfig;
    }

    private static void changePersonId(UpdateJob change, Connection connection) throws Exception {

        OutputContainer outputContainer = new OutputContainer(true); //doesn't matter what we pass into the constructor

        //the csv writers are mapped to the tables in the database, so we can use them to discover
        //what tables have person and patient ID columns
        List<AbstractEnterpriseCsvWriter> csvWriters = outputContainer.getCsvWriters();

        //the writers are in dependency order (least dependent -> most) so we need to go backwards to avoid
        //upsetting any foreign key constraints
        for (int i=csvWriters.size()-1; i>=0; i--) {
            AbstractEnterpriseCsvWriter csvWriter = csvWriters.get(i);

            String[] csvHeaders = csvWriter.getCsvHeaders();
            for (String header: csvHeaders) {
                if (header.equalsIgnoreCase("person_id")) {

                    String fileName = csvWriter.getFileName();
                    String tableName = FilenameUtils.removeExtension(fileName);
                    changePersonIdOnTable(tableName, change, connection);
                    break;
                }
            }
        }

        connection.commit();

        LOG.info("Updated person ID from " + change.getOldEnterprisePersonId() + " to " + change.getNewEnterprisePersonId() + " for patient " + change.getEnterprisePatientId());
    }

    /*private static void changePersonId(UpdateJob change, Connection connection) throws Exception {

        //can't think of a good way to make this list dynamic
        changePersonIdOnTable("allergy_intolerance", change, connection);
        changePersonIdOnTable("appointment", change, connection);
        changePersonIdOnTable("encounter", change, connection);
        changePersonIdOnTable("medication_order", change, connection);
        changePersonIdOnTable("medication_statement", change, connection);
        changePersonIdOnTable("observation", change, connection);
        changePersonIdOnTable("procedure_request", change, connection);
        changePersonIdOnTable("referral_request", change, connection);
        changePersonIdOnTable("episode_of_care", change, connection);
        changePersonIdOnTable("patient", change, connection);

        connection.commit();

        LOG.info("Updated person ID from " + change.getOldEnterprisePersonId() + " to " + change.getNewEnterprisePersonId() + " for patient " + change.getEnterprisePatientId());
    }*/


    private static void changePersonIdOnTable(String tableName, UpdateJob change, Connection connection) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ");
        sb.append(tableName);
        sb.append(" SET ");
        sb.append("person_id = ? ");
        sb.append("WHERE ");

        if (tableName.equals("patient")) {
            sb.append("id = ? ");
        } else {
            sb.append("patient_id = ? ");
        }

        sb.append("AND person_id = ?");

        PreparedStatement update = connection.prepareStatement(sb.toString());

        update.setLong(1, change.getNewEnterprisePersonId());
        update.setLong(2, change.getEnterprisePatientId());
        update.setLong(3, change.getOldEnterprisePersonId());

        update.addBatch();
        update.executeBatch();
    }

}


