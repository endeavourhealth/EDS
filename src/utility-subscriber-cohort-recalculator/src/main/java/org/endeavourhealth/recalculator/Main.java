package org.endeavourhealth.recalculator;

import com.google.common.base.Strings;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ScheduledTaskAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.ScheduledTaskAudit;
import org.endeavourhealth.core.database.dal.usermanager.caching.ProjectCache;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.database.rdbms.datasharingmanager.models.ProjectEntity;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.transform.subscriber.SubscriberConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static Map <String, Date> subscriberFeedProjectStartDate = new HashMap<>();
    private static String appId = "subscriber-cohort-recalculator";

    /**
     * utility to re-calculate cohorts on a daily basis, and manually insert messages into the
     * processing queues for any patients where their cohort inclusion/exclusion has changed to
     * trigger the bulk addition (or removal, if configured to) of data.
     * Usage:
     * =========================================================================================
     * No parameters
     */
    public static void main(String[] args) throws Exception {

        ConfigManager.Initialize(appId);

        LOG.info("Subscriber Cohort Re-calculation starting...... ");

        //get all the subscriber configurations
        Map<String, String> subscriberConfigs = ConfigManager.getConfigurations("db_subscriber");
        LOG.info("Found " + subscriberConfigs.size() + " subscriber configurations");

        for (String subscriberConfigId: subscriberConfigs.keySet()) {
            //String configData = subscriberConfigs.get(subscriberConfigId);
            LOG.debug("Checking subscriber configuration: " + subscriberConfigId);
            //LOG.debug(configData);
            try {
                //parse each config record to see if it contains the minimum age limit entries which trigger the re-calculation
                SubscriberConfig configRecord = SubscriberConfig.readFromConfig(subscriberConfigId);
                Integer minPatientAgeYears = configRecord.getMinPatientAgeYears();

                //if no minimum age config set for this subscriber configuration, then skip it
                if (minPatientAgeYears == null) {
                    continue;
                }
                //get a list of all services which publish into this subscriber configuration
                List<Service> publishingServices = getSubscriberFeedPublishers (subscriberConfigId);

                //get the audit history for the subscriberConfigId task
                List<ScheduledTaskAudit> taskAudit = getTaskAuditHistory(subscriberConfigId);

                //get the last successful run date for this subscriber config task to use in the DOB range check SQL
                Date lastSuccessfulRunDate = getScheduledTaskLastSuccessfulRunDate(taskAudit);
                if (lastSuccessfulRunDate == null) {

                    //if the task audit has now successfully run, or has never run, use the subscriber feed start date
                    if (subscriberFeedProjectStartDate.get(subscriberConfigId) != null) {

                        lastSuccessfulRunDate = subscriberFeedProjectStartDate.get(subscriberConfigId);
                        //LOG.debug("subscriberFeedProjectStartDate: "+subscriberFeedStartDate);
                    } else {

                        //and if that is null, use today as a fall back starting point
                        lastSuccessfulRunDate = new Date();
                    }
                }

                //for each publishing service, get a list of patient UUIDs which now fall into the
                //minimum age based criteria since (between) the last successful report run date and now
                for (Service publishingService: publishingServices) {

                    //LOG.debug("lastSuccessfulRunDate: "+lastSuccessfulRunDate);

                    UUID serviceId = publishingService.getId();
                    List<UUID> patientUuids
                            = getServicePatientsWithinDobDateRange(serviceId, lastSuccessfulRunDate, minPatientAgeYears);

                    if (!patientUuids.isEmpty()) {

                        LOG.info(patientUuids.size()
                                + " patient UUID(s) found now meeting minimum age (years) cohort criteria for publishing service: "
                                + publishingService.getName());

                        //add the subscriber config name
                        Set<String> specificSubscriberConfigNames = new HashSet<>();
                        specificSubscriberConfigNames.add(subscriberConfigId);

                        //send the list of new service patients which now meet the cohort criteria to the subscriber
                        //transform configuration queue which will process then through the subscriber pipeline
                        QueueHelper.queueUpPatientsForSubscriberTransform(
                                serviceId,
                                false,
                                false,
                                false,
                                specificSubscriberConfigNames,
                                patientUuids,
                                "Recalculated patients now meet minimum age (years) cohort criteria: " + minPatientAgeYears);
                    } else {

                        LOG.info("No patient UUIDs found now meeting minimum age (years) cohort criteria");
                    }
                }

                LOG.info("Subscriber configuration: "+subscriberConfigId+" cohort re-calculation completed");
                auditSuccess(subscriberConfigId, args);

            } catch (Exception ex) {

                LOG.error("Error re-calculating the cohort for subscriber configuration: " + subscriberConfigId, ex);
                SlackHelper.sendSlackMessage(SlackHelper.Channel.SubscriberCohortRecalculator, "Exception in Subscriber Cohort Re-calculator (" + subscriberConfigId + ")", ex);
                auditFailure(subscriberConfigId, args, ex);
            }
        }
        LOG.info("Subscriber Cohort Re-calculation complete. ");
        System.exit(0);
    }

    private static void auditSuccess(String queryName, String[] args) throws Exception {
        ScheduledTaskAuditDalI dal = DalProvider.factoryScheduledTaskAuditDal();
        dal.auditTaskSuccess(queryName, args);
    }

    private static void auditFailure(String queryName, String[] args, Throwable t) throws Exception {
        ScheduledTaskAuditDalI dal = DalProvider.factoryScheduledTaskAuditDal();
        dal.auditTaskFailure(queryName, args, t);
    }

    private static List<ScheduledTaskAudit> getTaskAuditHistory (String taskName) throws Exception {

        ScheduledTaskAuditDalI dal = DalProvider.factoryScheduledTaskAuditDal();
        Date dFrom = new Date(0);  //1-1-1970
        Date dTo = new Date();
        List<ScheduledTaskAudit> ret = dal.getHistory(appId, taskName, dFrom, dTo);

        return ret;
    }

    private static Date getScheduledTaskLastSuccessfulRunDate (List<ScheduledTaskAudit> taskAudits) {

        if (taskAudits == null || taskAudits.isEmpty()) {
            return null;
        }
        //LOG.debug("Task audits history count: "+taskAudits.size());

        //loop through and get latest successful run.
        //The list is ordered by run date so break out when first one found
        Date lastSuccessfulRunDate = null;
        for (ScheduledTaskAudit taskAudit: taskAudits) {
            //LOG.debug("Task audit date: "+taskAudit.getTimestamp()+", success: "+taskAudit.isSuccess());
            if (taskAudit.isSuccess()) {

                lastSuccessfulRunDate = taskAudit.getTimestamp();
                break;
            }
        }
        return lastSuccessfulRunDate;
    }

    //get a list of all the Services and project start dates which publish data to this Subscriber configuration
    private static List<Service> getSubscriberFeedPublishers(String subscriberConfigId) throws Exception  {

        List<Service> subscriberPublishers = new ArrayList<>();

        ServiceDalI serviceDal = DalProvider.factoryServiceDal();
        List<Service> allServices = serviceDal.getAll();
        for (Service service: allServices) {

            //only looking for actual data publisher services
            String publisherConfig = service.getPublisherConfigName();
            if (Strings.isNullOrEmpty(publisherConfig)) {
                continue;
            }

            //retrieve the current distribution projects for the service
            String odsCode = service.getLocalId();
            List<ProjectEntity> distributionProjects = ProjectCache.getValidDistributionProjectsForPublisher(odsCode);
            if (distributionProjects != null) {
                for (ProjectEntity pe : distributionProjects) {

                    //get the subscriber configuration for this service distribution project
                    String configName = pe.getConfigName();

                    //if the publisher service has the subscriber configuration in it's list of
                    //distribution projects then add  the service into the subscriber publishers list
                    if (!Strings.isNullOrEmpty(configName) && configName.equalsIgnoreCase(subscriberConfigId)) {

                        LOG.info("Publishing service: "+service.getName()+" found for configuration: "+configName);
                        subscriberPublishers.add(service);

                        //add the subscriber config project start date to use as a fall back date
                        //to perform the dob checks if the audit utility application has not run already
                        subscriberFeedProjectStartDate.put(subscriberConfigId, pe.getStartDate());
                    }
                }
            }
        }
        return subscriberPublishers;
    }

    private static List<UUID> getServicePatientsWithinDobDateRange(UUID serviceId,
                                                       Date lastAuditRunDate,
                                                       Integer minPatientAgeYears) throws Exception {

        Connection connection = ConnectionManager.getEdsConnection();
        changeTransactionIsolationLevel(connection);

        PreparedStatement ps = null;
        try {

            String sql = " SELECT patient_id"
                    + " FROM patient_search"
                    + " WHERE service_id = ?"
                    + " AND date_of_birth BETWEEN DATE_SUB(?, INTERVAL ? YEAR) and DATE_SUB(?, INTERVAL ? YEAR) "
                    + " AND dt_deleted IS NULL";

            ps = connection.prepareStatement(sql);
            ps.setString(1, serviceId.toString());
            ps.setDate(2, new java.sql.Date (lastAuditRunDate.getTime()));
            ps.setInt(3, minPatientAgeYears);
            ps.setDate(4, new java.sql.Date(new Date().getTime()));   //now
            ps.setInt(5, minPatientAgeYears);

            List<UUID> ret = new ArrayList<>();
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {

                UUID patientId = UUID.fromString(rs.getString(1));
                ret.add(patientId);
            }

            return ret;

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }

    /**
     * change our connection to read uncommitted to reduce risk of us causing any table locks
     */
    private static void changeTransactionIsolationLevel(Connection connection) throws Exception {

        String sql = "SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED";
        Statement statement = connection.createStatement();
        statement.execute(sql);
        statement.close();
    }
}