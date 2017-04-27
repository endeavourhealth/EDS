package org.endeavourhealth.enterprise;

import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.enterprise.EnterpriseConnector;
import org.endeavourhealth.core.rdbms.eds.PatientLinkHelper;
import org.endeavourhealth.core.rdbms.eds.PatientLinkPair;
import org.endeavourhealth.core.rdbms.transform.EnterpriseIdHelper;
import org.endeavourhealth.core.rdbms.transform.EnterprisePersonIdMap;
import org.endeavourhealth.core.rdbms.transform.EnterprisePersonUpdateHelper;
import org.endeavourhealth.core.slack.SlackHelper;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;

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
            if (args.length != 0) {
                LOG.error("No parameters required.");
                return;
            }

            LOG.info("Person updater starting");

            //create this date BEFORE we get the date we last run, so there's no risk of a gap
            Date dateNextRun = new Date();

            Date dateLastRun = EnterprisePersonUpdateHelper.findDatePersonUpdaterLastRun();
            LOG.info("Looking for Person ID changes since " + dateLastRun);
            List<PatientLinkPair> changes = PatientLinkHelper.getChangesSince(dateLastRun);

            //strip out any that are just telling us NEW person IDs
            for (int i=changes.size()-1; i>=0; i--) {
                PatientLinkPair change = changes.get(i);
                if (change.getPreviousPersonId() == null) {
                    changes.remove(i);
                }
            }
            LOG.info("Found " + changes.size() + " changes in Person ID");

            //find the Enterprise Person ID for each of the changes, hashing them by the enterprise instance they're on
            Map<String, List<UpdateJob>> changesMap = convertChangesToEnterprise(changes);

            for (String enterpriseConfigName: changesMap.keySet()) {

                List<UpdateJob> updates = changesMap.get(enterpriseConfigName);
                Connection connection = EnterpriseConnector.openConnection(enterpriseConfigName);

                LOG.info("Updating " + updates.size() + " person IDs on " + enterpriseConfigName);

                try {
                    for (UpdateJob update: updates) {
                        changePersonId(update, connection);
                    }

                } finally {
                    connection.close();
                }
            }

            EnterprisePersonUpdateHelper.updatePersonUpdaterLastRun(dateNextRun);

            LOG.info("Person updates complete");

        } catch (Exception ex) {
            LOG.error("", ex);
            SlackHelper.sendSlackMessage(SlackHelper.Channel.ProductionAlerts, "Exception in Enterprise Person Updater", ex);
        }

        System.exit(0);
    }


    private static Map<String, List<UpdateJob>> convertChangesToEnterprise(List<PatientLinkPair> changes) throws Exception {
        Map<String, List<UpdateJob>> map = new HashMap<>();
        for (PatientLinkPair change: changes) {

            String oldDiscoveryPersonId = change.getPreviousPersonId();
            String newDiscoveryPersonId = change.getNewPersonId();
            String discoveryPatientId = change.getPatientId();

            Long enterprisePatientId = EnterpriseIdHelper.findEnterpriseId("patient", ResourceType.Patient.toString(), discoveryPatientId);

            //if this patient has never gone to enterprise, then skip it
            if (enterprisePatientId == null) {
                continue;
            }

            List<EnterprisePersonIdMap> mappings = EnterpriseIdHelper.findEnterprisePersonMapsForPersonId(oldDiscoveryPersonId);
            for (EnterprisePersonIdMap mapping: mappings) {

                String enterpriseConfigName = mapping.getEnterpriseConfigName();
                Long oldEnterprisePersonId = mapping.getEnterprisePersonId();
                Long newEnterprisePersonId = EnterpriseIdHelper.findOrCreateEnterprisePersonId(newDiscoveryPersonId, enterpriseConfigName);

                List<UpdateJob> updatesForConfig = map.get(enterpriseConfigName);
                if (updatesForConfig == null) {
                    updatesForConfig = new ArrayList<>();
                    map.put(enterpriseConfigName, updatesForConfig);
                }

                updatesForConfig.add(new UpdateJob(enterprisePatientId, oldEnterprisePersonId, newEnterprisePersonId));
            }
        }

        return map;
    }

    private static void changePersonId(UpdateJob change, Connection connection) throws Exception {

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
    }


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


