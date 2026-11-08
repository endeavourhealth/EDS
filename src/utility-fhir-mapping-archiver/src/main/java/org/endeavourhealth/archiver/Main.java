package org.endeavourhealth.archiver;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ScheduledTaskAuditDalI;
import org.endeavourhealth.core.database.dal.publisherTransform.SourceFileMappingDalI;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);


    /**
     * utility to archive content from the fhir_mapping_audit table on a publisher_transform database to a file in a file system
     *
     * Usage
     * =================================================================================
     * <publisher_config_name> - publisher config name
     * <copy_batch_size> - optional parameter to change batch size used for copying
     * <test_mode> - optional parameter to prevent it actually copying to S3 or deleting from the database
     */
    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            LOG.error("Parameter required: <publisher_config_name>");
            return;
        }

        String publisherConfigName = args[0];
        ConfigManager.initialize("fhir-mapping-archiver", publisherConfigName);

        int copyBatchSize = 100;
        if (args.length > 1) {
            copyBatchSize = Integer.parseInt(args[1]);
        }

        boolean testMode = false;
        if (args.length > 2) {
            testMode = Boolean.parseBoolean(args[2]);
        }

        boolean overrideLock = false;
        if (args.length > 3) {
            overrideLock = Boolean.parseBoolean(args[3]);
        }

        UUID restrictServiceId = null;
        if (args.length > 4) {
            restrictServiceId = UUID.fromString(args[4]);
        }

        LOG.info("Archiving FHIR mappings for " + publisherConfigName + " with batch size = " + copyBatchSize + " and test mode = " + testMode);
        if (restrictServiceId != null) {
            LOG.info("Restricting to service " + restrictServiceId);
        }

        if (overrideLock) {
            LOG.info("Not checking for lock file");

        } else if (checkIfLockFileExists(publisherConfigName)) {
            LOG.error("Application already running (lock file found)");
            return;

        } else {
            LOG.info("Lock file not found - OK to run");
        }

        try {
            archiveMappings(publisherConfigName, copyBatchSize, testMode, restrictServiceId);

            auditSuccess(publisherConfigName, args);

        } catch (Throwable t) {

            LOG.error("Error with FHIR Mapping Archiver for " + publisherConfigName, t);
            SlackHelper.sendSlackMessage(SlackHelper.Channel.FhirMappingArchiver, "Error running FHIR Mapping Archiver for [" + publisherConfigName + "]", t);
            auditFailure(publisherConfigName, args, t);
        }

        deleteLockFile(publisherConfigName);
    }

    /**
     * things might get messy if we allow multiple instances of this application to run concurrently for the same publisher
     * so use a local file in the temp dir to
     */
    private static boolean checkIfLockFileExists(String publisherConfigName) {
        File f = getAppLockFile(publisherConfigName);
        if (f.exists()) {
            LOG.trace("Lock file " + f + " found");
            return true;
        }

        try {
            f.createNewFile();
            LOG.trace("Created lock file " + f);
            return false;
        } catch (Exception ex) {
            LOG.error("Failed to create lock file " + f);
            return true;
        }
    }

    private static File getAppLockFile(String publisherConfigName) {

        try {
            File tempTopLevelPath = FileHelper.getTempDir();
            return new File(tempTopLevelPath, "LOCK_" + publisherConfigName + ".txt");

        } catch (Exception ex) {
            LOG.error("Failed to create temp directory for lock file");
            return null;
        }
    }

    private static void archiveMappings(String publisherConfigName, int copyBatchSize, boolean testMode, UUID restrictServiceId) throws Exception {

        //we just need any service ID that's configured for this publisher, so we can get a DB connection
        UUID serviceId = findAnyServiceIdForPublisher(publisherConfigName);

        //find distinct path names from new mappings
        List<Period> periods = findNewDataPeriods(serviceId, restrictServiceId);
        if (periods.isEmpty()) {
            LOG.debug("Nothing to archive, so returning");
            return;
        }

        LOG.debug("Found " + periods.size() + " services to archive");
        for (Period period: periods) {
            archiveMappingsForPeriod(period, copyBatchSize, testMode);
        }
        LOG.debug("Completed archiving");
    }

    /**
     * ConnectionManager requires a service ID to return a DB connection, so just find any service
     * for the publisher we're running for
     */
    private static UUID findAnyServiceIdForPublisher(String publisherConfigName) throws Exception {
        ServiceDalI serviceDal = DalProvider.factoryServiceDal();
        List<Service> services = serviceDal.getAll();
        for (Service service: services) {
            if (service.getPublisherConfigName() != null
                && service.getPublisherConfigName().equals(publisherConfigName)) {
                return service.getId();
            }
        }

        throw new Exception("Failed to find a service for publisher config [" + publisherConfigName + "]");
    }


    /**
     * finds the details of the new data in the table to be archived
     */
    private static List<Period> findNewDataPeriods(UUID exampleServiceId, UUID restrictServiceId) throws Exception {
        Connection connection = ConnectionManager.getPublisherTransformConnection(exampleServiceId);
        PreparedStatement ps = null;
        try {

            String sql = "SELECT service_id, min(created_at), max(created_at)"
                + " FROM fhir_mapping_audit"
                + " GROUP BY service_id";
            ps = connection.prepareStatement(sql);

            List<Period> ret = new ArrayList<>();

            //we don't want to attempt to archive anything from the current hour. There's no point archiving
            //when we know we'll need to append to it later
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date cutoff = cal.getTime();

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int col = 1;
                String serviceIdStr = rs.getString(col++);
                java.util.Date minDate = new java.util.Date(rs.getTimestamp(col++).getTime());
                java.util.Date maxDate = new java.util.Date(rs.getTimestamp(col++).getTime());

                if (restrictServiceId != null) {
                    if (!restrictServiceId.toString().equals(serviceIdStr)) {
                        //LOG.debug("Skipping audits for " + serviceIdStr + " as restricting on service ID " + restrictServiceId);
                        continue;
                    }
                }

                UUID serviceId = UUID.fromString(serviceIdStr);

                if (maxDate.after(cutoff)) {
                    maxDate = cutoff;
                }

                Period p = new Period(serviceId, minDate, maxDate);
                ret.add(p);

                LOG.trace("Found audits for " + serviceIdStr);
            }

            return ret;

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }

    }

    private static void archiveMappingsForPeriod(Period period, int copyBatchSize, boolean testMode) throws Exception {

        UUID serviceId = period.getServiceId();
        Date minDateTime = period.getMinDate();
        Date maxDateTime = period.getMaxDate();

        Date minDate = convertToDate(minDateTime);
        Date maxDate = convertToDate(maxDateTime);

        Date d = minDate;
        while (!d.after(maxDate)) {

            //check if we have any mappings for this DATE
            Date tomorrow = addToDate(d, Calendar.DAY_OF_YEAR, 1);
            if (haveMappingsToArchive(serviceId, d, tomorrow)) {

                //if we've got mappings for the DATE, then check for each HOUR
                while (d.before(tomorrow)) {

                    Date nextHour = addToDate(d, Calendar.HOUR_OF_DAY, 1);
                    if (haveMappingsToArchive(serviceId, d, nextHour)) {

                        //if we've got mappings for the HOUR, then check for each MINUTE
                        while (d.before(nextHour)) {

                            Date nextMinute = addToDate(d, Calendar.MINUTE, 1);
                            if (haveMappingsToArchive(serviceId, d, nextMinute)) {

                                //if we've got mappings for the HOUR, then we need to archive them
                                archiveMappingsForServiceAndDates(serviceId, d, nextMinute, copyBatchSize, testMode);
                            }

                            //move to next minute
                            d = nextMinute;
                        }
                    }

                    //move to next hour
                    d = nextHour;
                }
            }

            //move to next day
            d = tomorrow;
        }
    }

    /**
     * removes all time elements from a date
     */
    private static Date convertToDate(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private static Date addToDate(Date d, int element, int amount) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(element, amount);
        return cal.getTime();
    }

    /**
     * checks if there are mappings between two dates for the given service
     */
    private static boolean haveMappingsToArchive(UUID serviceId, Date dFrom, Date dTo) throws Exception {

        LOG.trace("Checking for mappings for " + serviceId + " between " + dFrom + " and " + dTo);

        Connection connection = ConnectionManager.getPublisherTransformConnection(serviceId);
        PreparedStatement ps = null;
        try {
            String sql = "SELECT 1"
                + " FROM fhir_mapping_audit"
                + " WHERE service_id = ?"
                + " AND created_at >= ?"
                + " AND created_at < ?"
                + " LIMIT 1";
            ps = connection.prepareStatement(sql);

            int col = 1;
            ps.setString(col++, serviceId.toString());
            ps.setTimestamp(col++, new java.sql.Timestamp(dFrom.getTime()));
            ps.setTimestamp(col++, new java.sql.Timestamp(dTo.getTime()));

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }

    private static void archiveMappingsForServiceAndDates(UUID serviceId, Date dFrom, Date dTo, int copyBatchSize, boolean testMode) throws Exception {

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        DateFormat timeFormat = new SimpleDateFormat("HHmm");

        String dateStr = dateFormat.format(dFrom);
        String timeStr = timeFormat.format(dFrom);

        String fileSubPath = FileHelper.concatFilePath(serviceId.toString(), dateStr, timeStr + ".csv");
        LOG.debug("Archiving mappings to " + fileSubPath);

        SourceFileMappingDalI dal = DalProvider.factorySourceFileMappingDal();
        String permanentTopLevelPath = dal.getPermanentStoragePath();
        if (Strings.isNullOrEmpty(permanentTopLevelPath)) {
            throw new Exception("No permanent storage location set in config");
        }
        String permanentPath = FileHelper.concatFilePath(permanentTopLevelPath, fileSubPath);

        File tempTopLevelPath = FileHelper.getTempDir();
        String tempPath = FileHelper.concatFilePath(tempTopLevelPath.getAbsolutePath(), fileSubPath);

        //create the temp directories
        File tempDir = new File(tempPath).getParentFile();
        FileHelper.deleteRecursiveIfExists(tempDir); //delete first to make sure any old stuff is gone
        FileHelper.createDirectoryIfNotExists(tempDir);

        //see if the file already exists in S3, in which case we need to APPEND the new data to it
        if (!testMode && FileHelper.fileExists(permanentPath)) {
            LOG.debug("File already exists in permanent storage so will need to copy out");

            InputStream is = FileHelper.readFileFromSharedStorage(permanentPath);
            LOG.trace("Copying from " + permanentPath);
            LOG.trace("Copying to " + tempPath);
            Files.copy(is, new File(tempPath).toPath());
            is.close();
            LOG.debug("Copied existing file from " + permanentPath + " to " + tempPath);
        }

        //add the new audit data to the file
        String tempTableName = ConnectionManager.generateTempTableName("fhir_archiver_" + fileSubPath);
        Connection connection = ConnectionManager.getPublisherTransformNonPooledConnection(serviceId); //don't use the pool as we're keeping the connection for a while
        connection.setAutoCommit(true); //set auto-commit true to avoid having to commit
        changeTransactionIsolationLevel(connection);
        copyMappingsToTemporaryTable(connection, tempTableName, serviceId, dFrom, dTo);
        copyMappingsToFile(connection, tempTableName, tempPath, copyBatchSize);

        //copy the file into permanent storage
        if (!testMode) {
            FileHelper.writeFileToSharedStorage(permanentPath, new File(tempPath));
        }

        //delete the audit data from the database now it's safely in S3
        if (!testMode) {
            deleteMappingsFromTable(connection, tempTableName);
        }

        //drop the temp table
        dropTempTable(connection, tempTableName);

        //close connection
        //because the connection wasn't from our connection pool, we don't need to worry about setting autoCommit back to false
        connection.close();

        //delete temp file
        if (!testMode) {
            new File(tempPath).delete();
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

    /**
     * copies the mappings for the given service, date and hour to a temporary table
     */
    private static void copyMappingsToTemporaryTable(Connection connection, String tempTableName, UUID serviceId, Date dFrom, Date dTo) throws Exception {

        PreparedStatement ps = null;
        try {

            String sql = "CREATE TABLE " + tempTableName + " AS"
                + " SELECT *, 0 as `done`" //copy all cols and add a column called "done"
                + " FROM fhir_mapping_audit"
                + " WHERE service_id = ?"
                + " AND created_at >= ?"
                + " AND created_at < ?";
            ps = connection.prepareStatement(sql);

            int col = 1;
            ps.setString(col++, serviceId.toString());
            ps.setTimestamp(col++, new java.sql.Timestamp(dFrom.getTime()));
            ps.setTimestamp(col++, new java.sql.Timestamp(dTo.getTime()));

            ps.executeUpdate();

        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    /**
     * copies the mappings from the temporary table, in batches, and appends them to the CSV file
     */
    private static void copyMappingsToFile(Connection connection, String tempTableName, String tempPath, int copyBatchSize) throws Exception {

        CSVPrinter csvPrinter = null;

        File tempFile = new File(tempPath);
        if (tempFile.exists()) {
            //if we're appending to an existing CSV file, we don't want to write the headers out again
            FileOutputStream fos = new FileOutputStream(tempPath, true); //append mode
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bufferedWriter = new BufferedWriter(osw);
            csvPrinter = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT); //don't specify any header

        } else {
            //if we're creating a new CSV file, we want to write the headers
            File tempDir = tempFile.getParentFile();
            FileHelper.createDirectoryIfNotExists(tempDir);

            FileOutputStream fos = new FileOutputStream(tempFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bufferedWriter = new BufferedWriter(osw);
            csvPrinter = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT.withHeader("resource_id", "resource_type", "version", "created_at", "mappings_json"));
        }

        Statement statement = connection.createStatement();

        String sql = "CREATE INDEX ix ON " + tempTableName + " (done)";
        statement.execute(sql);

        sql = "CREATE INDEX ix2 ON " + tempTableName + " (resource_id, resource_type, version)";
        statement.execute(sql);

        String tempTableName2 = ConnectionManager.generateTempTableName("fhir_archiver_batch");

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int done = 0;

        while (true) {
            //LOG.trace("Starting batch of " + copyBatchSize);

            //create a table of a batch
            sql = "CREATE TABLE " + tempTableName2 + " ENGINE=MEMORY AS"
                + " SELECT resource_id, resource_type, version"
                + " FROM " + tempTableName
                + " WHERE done = 0"
                + " LIMIT " + copyBatchSize;
            statement.executeUpdate(sql);
            //LOG.trace("Created batch in temp table 2");

            //select from batch
            int count = 0;
            sql = "SELECT t2.resource_id, t2.resource_type, t2.version, t1.created_at, t1.mappings_json"
                + " FROM " + tempTableName2 + " t2 "
                + " JOIN " + tempTableName + " t1 "
                + " ON t1.resource_id = t2.resource_id"
                + " AND t1.resource_type = t2.resource_type"
                + " AND t1.version = t2.version";
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                int col = 1;
                String resourceId = rs.getString(col++);
                String resourceType = rs.getString(col++);
                String version = rs.getString(col++);
                java.util.Date createdAt = new java.util.Date(rs.getTimestamp(col++).getTime());
                String mappingJson = rs.getString(col++);

                csvPrinter.printRecord(resourceId, resourceType, version, dateFormat.format(createdAt), mappingJson);
                count++;
            }
            //LOG.trace("Copied batch to file");

            //index - not required - update uses existing index on temp table 1 and will always scan temp table 2 anyway!
            //sql = "CREATE INDEX ix ON " + tempTableName2 + " (resource_id, resource_type, version)";
            //statement.executeUpdate(sql);
            //LOG.trace("Indexed temp table 2");

            //mark as done
            sql = "UPDATE " + tempTableName + " t1"
                + " INNER JOIN " + tempTableName2 + " t2"
                + " ON t2.resource_id = t1.resource_id"
                + " AND t2.resource_type = t1.resource_type"
                + " AND t2.version = t1.version"
                + " SET t1.done = 1";
            statement.executeUpdate(sql);
            //LOG.trace("Batch marked as done");

            //drop temp table
            sql = "DROP TABLE " + tempTableName2;
            statement.executeUpdate(sql);
            //LOG.trace("Temp table 2 dropped");

            done += count;
            if (done % 5000 == 0) {
                LOG.debug("Copied " + done + " mappings to " + tempPath);
            }

            //if our batch was smaller than the batch size then we're done
            if (count < copyBatchSize) {
                break;
            }
        }
        LOG.debug("Finished copying " + done + " mappings to " + tempPath);

        csvPrinter.close();
    }

    private static void deleteMappingsFromTable(Connection connection, String tempTableName) throws Exception {

        Statement statement = connection.createStatement();

        String sql = "CREATE INDEX ix3 ON " + tempTableName + " (resource_id, resource_type, created_at, version)";
        statement.execute(sql);

        sql = "DELETE m"
            + " FROM fhir_mapping_audit m"
            + " INNER JOIN " + tempTableName + " t"
            + " ON t.resource_id = m.resource_id"
            + " AND t.resource_type = m.resource_type"
            + " AND t.created_at = m.created_at"
            + " AND t.version = m.version";
        statement.execute(sql);
    }

    private static void dropTempTable(Connection connection, String tempTableName) throws Exception {
        Statement statement = connection.createStatement();

        String sql = "DROP TABLE " + tempTableName;
        statement.executeUpdate(sql);
    }

    private static void auditSuccess(String publisherConfigName, String[] args) throws Exception {
        ScheduledTaskAuditDalI dal = DalProvider.factoryScheduledTaskAuditDal();
        dal.auditTaskSuccess(publisherConfigName, args);
    }

    private static void auditFailure(String publisherConfigName, String[] args, Throwable t) throws Exception {
        ScheduledTaskAuditDalI dal = DalProvider.factoryScheduledTaskAuditDal();
        dal.auditTaskFailure(publisherConfigName, args, t);
    }

    /**
     * when the app exits, we should delete the lock file so it will run OK next time
     */
    private static void deleteLockFile(String publisherConfigName) {
        File f = getAppLockFile(publisherConfigName);
        //the lock file won't exist if we're running with the option to ignore it
        if (f.exists() && !f.delete()) {
            LOG.error("Failed to delete lock file " + f);
        }
    }

    static class Period {
        private UUID serviceId;
        private Date minDate;
        private Date maxDate;

        public Period(UUID serviceId, Date minDate, Date maxDate) {
            this.serviceId = serviceId;
            this.minDate = minDate;
            this.maxDate = maxDate;
        }

        public UUID getServiceId() {
            return serviceId;
        }

        public Date getMinDate() {
            return minDate;
        }

        public Date getMaxDate() {
            return maxDate;
        }
    }
}
