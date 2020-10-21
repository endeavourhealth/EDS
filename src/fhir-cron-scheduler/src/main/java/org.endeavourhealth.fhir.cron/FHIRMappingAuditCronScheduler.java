package org.endeavourhealth.fhir.cron;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.fhir.cron.utilities.FHIRAuditConstants;
import org.endeavourhealth.fhir.cron.utilities.FHIRAuditUtil;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * FHIRMappingAuditScheduler is a scheduler component and will run everyday at the scheduled time
 * The scheduler will do below jobs
 * 1. fetch all audit records for the day from database
 * 2. build csv file
 * 3. write the csv file to AWS S3 using FileHelper class
 * 4. delete the audit records from database
 */
public class FHIRMappingAuditCronScheduler implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(FHIRMappingAuditCronScheduler.class);
    private static FHIRAuditUtil propertyUtil = new FHIRAuditUtil();
    private static String propFileName = "fhir_audit.properties";

    public static void main(String[] args) throws Exception {
        LOG.info("Initializing  fhir-mappings-cron-scheduler!!");
        String awsEnabledFlag = propertyUtil.getProperty(propFileName,
                FHIRAuditConstants.ENVIRONMENT_AWS_FLAG);
        if(awsEnabledFlag != null && awsEnabledFlag.equals("true")) {
            int hour = Integer.parseInt(propertyUtil.getProperty(propFileName,
                    FHIRAuditConstants.SCHEDULER_TIME_HOUR));
            int minute = Integer.parseInt(propertyUtil.getProperty(propFileName,
                    FHIRAuditConstants.SCHEDULER_TIME_MINUTE));
            JobDetail jobDetail = JobBuilder.newJob(FHIRMappingAuditCronScheduler.class).build();

            Trigger trigger = TriggerBuilder.newTrigger().withIdentity("FHIR Cron Trigger").
                    withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(hour, minute)).build();
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            scheduler.scheduleJob(jobDetail, trigger);
            LOG.info("Scheduler scheduled!!");
        }
    }

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            LOG.info("Execution of the job!!");
            String permDir = propertyUtil.getProperty(propFileName, FHIRAuditConstants.S3_PATH);
            String tempDir = propertyUtil.getProperty(propFileName, FHIRAuditConstants.LOCAL_TMP_PATH);
            String dstFileName = propertyUtil.getProperty(propFileName, FHIRAuditConstants.FILE_NAME);
            String filePrefix = propertyUtil.getProperty(propFileName, FHIRAuditConstants.S3_FILE_PREFIX);
            long msStart = System.currentTimeMillis();
            //get audit records for last 24 hrs from database and write to a csv file
            writeFHIRMappingAuditRecordsFromDBToFile(tempDir + dstFileName);
            long msEnd = System.currentTimeMillis();
            LOG.info("Took " + ((msEnd - msStart) / 1000) + " s to get audit records for the the day from database and write to csv file");
            //write csv file to s3
            long msStart1 = System.currentTimeMillis();
            loadFHIRMappingAuditRecordstoS3(permDir, tempDir + dstFileName, filePrefix);
            long msEnd1 = System.currentTimeMillis();
            LOG.info("Took " + ((msEnd1 - msStart1) / 1000) + " s to write csv file to s3");
            //clean up audit records from database for day-2, since we will keep always day one records
            deleteFHIRMappingAuditRecordsFromDB();

        } catch (Throwable t) {
            LOG.error("Exception occurred in executing job" + t);
        }
    }

    public static void writeFHIRMappingAuditRecordsFromDBToFile(String dstFileName)  throws Exception {
        LOG.info(" Start writeFHIRMappingAuditRecordsFromDBToFile " );
        Connection conn = null;
        PreparedStatement ps = null;
        String sql = null;
        try {
            conn = ConnectionManager.getSftpReaderNonPooledConnection();
            sql =
                    "SELECT " +
                        " rfm.service_id, " +
                        " rfm.resource_id," +
                        " rfm.resource_type," +
                        " rfm.version," +
                        " rfm.created_at," +
                        " rfm.mappings_json" +
                    " FROM " +
                         " publisher_transform_a.resource_field_mappings_s3  rfm" +
                         " WHERE " +
                         //" rfm.service_id = '3ec22b12-5b2e-4665-bc87-34072725ef21'" +
                         //" AND rfm.resource_id = '0009a815-30a6-4b26-bea8-dd4a50527d2e'" ; //+
                        " rfm.created_at > now() - interval 1 day";
            ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
            FileOutputStream fos = new FileOutputStream(dstFileName);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            CSVPrinter csvPrinter = new CSVPrinter(osw, CSVFormat.DEFAULT.withHeader(rs));
            csvPrinter.printRecords(rs);
            ps.close();
            csvPrinter.close();

        } catch (Exception e) {
            LOG.error("Error while getting audit records for T-1 day", e);
        } finally {
            if (ps != null) {
                ps.close();
            }
            conn.close();
        }
        LOG.info(" End writeFHIRMappingAuditRecordsFromDBToFile " );
    }

    public static void deleteFHIRMappingAuditRecordsFromDB()  throws Exception {
        LOG.info(" Start deleteFHIRMappingAuditRecordsFromDB " );
        Connection conn = null;
        Statement st = null;
        String sql = null;
        long msStart = System.currentTimeMillis();
        try {
            conn = ConnectionManager.getSftpReaderNonPooledConnection();
            sql =
                    "DELETE " +
                    " FROM " +
                        " publisher_transform_a.resource_field_mappings_s3  rfm" +
                    " WHERE " +
                        " rfm.created_at > now() - interval 2 day AND rfm.created_at < now() - interval 1 day";
            st = conn.createStatement();
            int records  = st.executeUpdate(sql);
            conn.commit();
        } catch (Exception e) {
            LOG.error("Error while deleting audit records for T-2 day", e);
        } finally {
            if (st != null) {
                st.close();
            }
            conn.close();
        }
        LOG.info(" End deleteFHIRMappingAuditRecordsFromDB " );
    }
    public static void loadFHIRMappingAuditRecordstoS3(String permDir, String dstFileName, String filePrefix)  throws Exception {
        String dateString = new SimpleDateFormat("dd-MM-yyyy hh-mm-ss").format(new Date());
        String storageFilePath = permDir + filePrefix + dateString;
        File dstFile = new File(dstFileName);
        //write to S3
        FileHelper.writeFileToSharedStorage(storageFilePath, dstFile);
        //delete the file
        dstFile.delete();
    }

}



