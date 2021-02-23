package org.endeavourhealth.subscriber.filer;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.zaxxer.hikari.pool.HikariProxyConnection;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.datagenerator.SubscriberZipFileUUIDsDalI;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.database.rdbms.DeadlockHandler;
import org.endeavourhealth.core.database.rdbms.enterprise.EnterpriseConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SubscriberFiler {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriberFiler.class);

    private static final String COLUMN_CLASS_MAPPINGS = "ColumnClassMappings.json";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd kk:mm:ss";
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT;

    private static final String COL_IS_DELETE = "is_delete";
    private static final String COL_ID = "id";

    private static final int UPSERT_ATTEMPTS = 15;


    public static void file(UUID batchId, UUID queuedMessageId, String base64, String configName) throws Exception {

        byte[] bytes = Base64.getDecoder().decode(base64);
        LOG.debug("Filing " + FileUtils.byteCountToDisplaySize(bytes.length) + " from batch " + batchId + " into " + configName);

        //we may have multiple connections if we have replicas
        List<EnterpriseConnector.ConnectionWrapper> connectionWrappers = EnterpriseConnector.openSubscriberConnections(configName);

        for (EnterpriseConnector.ConnectionWrapper wrapper: connectionWrappers) {

            //if we have a direct DB connection, then write to the DB (do this first, so any failure here will mean
            //we won't already have written to the remote subscriber table, resulting in duplicates if we try again)
            if (wrapper.hasDatabaseConnection()) {
                file(wrapper, bytes);
            }

            //if we have a remote subscriber ID, then write the CSV data out for sending
            if (wrapper.hasRemoteSubscriberId()) {
                int subscriberId = wrapper.getRemoteSubscriberId().intValue();
                SubscriberZipFileUUIDsDalI szfudi = DalProvider.factorySubscriberZipFileUUIDs();
                szfudi.createSubscriberZipFileUUIDsEntity(subscriberId, batchId.toString(), queuedMessageId.toString(), base64);
                LOG.trace("Written data to remote sending table with subscriber ID " + subscriberId + " and queued message ID " + queuedMessageId);
            }
        }
    }

    private static void file(EnterpriseConnector.ConnectionWrapper connectionWrapper, byte[] bytes) throws Exception {

        Connection connection = connectionWrapper.getConnection();
        String keywordEscapeChar = connectionWrapper.getKeywordEscapeChar();
        int batchSize = connectionWrapper.getBatchSize();

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ZipInputStream zis = new ZipInputStream(bais);

        try {

            //the zip contains a JSON file giving type descriptors for all the columns of each file
            JsonNode columnClassMappings = null;

            //we process all upserts first, then deletes after, so have a list to farm them off to
            List<DeleteWrapper> deletes = new ArrayList<>();

            while (true) {
                ZipEntry entry = zis.getNextEntry();
                if (entry == null) {
                    break;
                }

                String entryFileName = entry.getName();
                byte[] entryBytes = readZipEntry(zis);

                //the first entry should always be the JSON file giving the column types for each file
                if (columnClassMappings == null) {
                    if (!entryFileName.equals(COLUMN_CLASS_MAPPINGS)) {
                        throw new Exception("" + COLUMN_CLASS_MAPPINGS + " expected as first file");
                    }
                    String jsonStr = new String(entryBytes);
                    columnClassMappings = ObjectMapperPool.getInstance().readTree(jsonStr);
                    continue;
                }

                processCsvEntry(entryFileName, entryBytes, columnClassMappings, connection, keywordEscapeChar, batchSize, deletes);
            }

            //now files the deletes once all upserts are done
            fileDeletes(deletes, connection);

        } catch (Exception ex) {
            //if we get an exception, write out the zip file, so we can investigate what was being filed
            writeZipFile(bytes);

            throw new Exception("Exception filing to " + connectionWrapper, ex);
        } finally {
            if (zis != null) {
                zis.close();
            }
            connection.close();
        }
    }

    private static void writeZipFile(byte[] bytes) {
        File f = new File("EnterpriseFileError.zip");
        try {
            FileUtils.writeByteArrayToFile(f, bytes);
            LOG.error("Written error dump ZIP file to " + f);

        } catch (IOException ex) {
            LOG.error("Failed to write error dump ZIP file to " + f, ex);
        }
    }

    private static void fileDeletes(List<DeleteWrapper> deletes, Connection connection) throws Exception {

        String currentTable = null;
        List<CSVRecord> currentRecords = null;
        List<String> currentColumns = null;
        Map<String, Class> currentColumnClasses = null;
        boolean patientWasDeleted = false;

        //go backwards, so we delete dependent records first
        for (int i=deletes.size()-1; i>=0; i--) {
            DeleteWrapper wrapper = deletes.get(i);
            String tableName = wrapper.getTableName();

            //if deleting anything from patient, then we need to do some special housekeeping
            if (tableName.equalsIgnoreCase("patient")) {
                patientWasDeleted = true;
            }

            if (currentTable == null
                    || !currentTable.equals(tableName)) {

                //file any deletes we've already built up
                if (currentRecords != null
                        && !currentRecords.isEmpty()) {
                    fileDeletesForTable(currentRecords, currentColumns, currentColumnClasses, currentTable, connection);
                }

                currentTable = tableName;
                currentRecords = new ArrayList<>();
                currentColumns = wrapper.getColumns();
                currentColumnClasses = wrapper.getColumnClasses();
            }

            currentRecords.add(wrapper.getRecord());
        }

        //file any deletes we've already built up
        if (currentRecords != null
                && !currentRecords.isEmpty()) {
            fileDeletesForTable(currentRecords, currentColumns, currentColumnClasses, currentTable, connection);
        }

        if (patientWasDeleted) {
            deleteLeftOverPersonRecords(connection);
            deleteLeftOverPatientUPRNRecords(connection);
        }
    }

    private static byte[] readZipEntry(ZipInputStream zis) throws Exception {

        byte[] buffer = new byte[2048];

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(baos, buffer.length);

        int len = 0;
        while ((len = zis.read(buffer)) > 0) {
            bos.write(buffer, 0, len);
        }

        bos.flush();
        bos.close();

        return baos.toByteArray();
    }

    private static ArrayList<String> getTableColumns(Connection connection, String tableName) throws Exception {

        HikariProxyConnection con = (HikariProxyConnection) connection;

        ArrayList<String> columns = new ArrayList<>();
        String schema = con.getCatalog();
        String sql = null;
        if (ConnectionManager.isSqlServer(connection) || ConnectionManager.isPostgreSQL(connection)) {
            sql = "select column_name as field from information_schema.columns where table_name = '" + tableName + "';";
        } else {
            sql = "describe " +  schema + "." +  tableName + ";";
        }
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.executeQuery();
        ResultSet resultSet = ps.getResultSet();
        String value = "";
        while (resultSet.next()) {
            value = resultSet.getString("field");
            columns.add(value);
        }
        resultSet.close();
        ps.close();
        return columns;
    }

    /**
     * processes a single entry from the zip file (i.e. one CSV file). Any deletes are simply
     * added to a list as all deletes are done after all upserts are out of the way.
     */
    private static void processCsvEntry(String entryFileName, byte[] csvBytes, JsonNode columnClassJson, Connection connection,
                                        String keywordEscapeChar, int batchSize, List<DeleteWrapper> allDeletes) throws Exception {

        String tableName = Files.getNameWithoutExtension(entryFileName);
        ArrayList<String> actualColumns = getTableColumns(connection, tableName);

        ByteArrayInputStream bais = new ByteArrayInputStream(csvBytes);
        InputStreamReader isr = new InputStreamReader(bais);
        CSVParser csvParser = new CSVParser(isr, CSV_FORMAT.withHeader());

        //find out what columns we've got
        List<String> columns = createHeaderList(csvParser);
        List<String> columnsRemove = new ArrayList<>();
        for (String column : columns) {
            if (!column.equals(COL_IS_DELETE) && !column.equals(COL_ID)) {
                if (!actualColumns.contains(column)) {
                    columnsRemove.add(column);
                }
            }
        }
        for (String column : columnsRemove) {
            LOG.debug("Removing column:" + column + " from the header map for table:" + tableName);
            columns.remove(column);
        }

        Map<String, Class> columnClasses = createHeaderColumnMap(entryFileName, columnClassJson, columns);

        //validate that file has "id" and "is_delete" always
        if (!columns.contains(COL_IS_DELETE)) {
            throw new Exception("" + entryFileName + " doesn't have an " + COL_IS_DELETE + " column");
        }
        if (!columns.contains(COL_ID)) {
            throw new Exception("" + entryFileName + " doesn't have an " + COL_ID + " column");
        }

        //since we're dealing with small volumes, we can just read keep all the records in memory
        List<CSVRecord> upserts = new ArrayList<>();
        List<DeleteWrapper> deletes = new ArrayList<>();

        int row = 0;

        Iterator<CSVRecord> csvIterator = csvParser.iterator();
        while (csvIterator.hasNext()) {
            CSVRecord csvRecord = csvIterator.next();
            String isDeleteStr = csvRecord.get(COL_IS_DELETE);
            if (Strings.isNullOrEmpty(isDeleteStr)) {
                throw new Exception("Empty " + COL_IS_DELETE + " value on row " + row + " of " + entryFileName);
            }
            boolean isDelete = Boolean.parseBoolean(isDeleteStr);

            if (isDelete) {
                //we have to play deletes in reverse, so just add to this list and they'll be processed after all the upserts
                deletes.add(new DeleteWrapper(tableName, csvRecord, columns, columnClasses));

            } else {
                upserts.add(csvRecord);
            }

            row ++;
        }

        LOG.trace("Got " + upserts.size() + " upserts and " + deletes.size() + " deletes for " + tableName);

        //add the deletes to this list to apply later
        allDeletes.addAll(deletes);

        if (!upserts.isEmpty()) {

            //when doing a bulk, we can have 300,000+ practitioners, so do them in batches, so we're
            //not keeping huge DB transactions open
            List<CSVRecord> batch = new ArrayList<>();
            for (CSVRecord record : upserts) {
                batch.add(record);

                //in testing, batches of 20000 seemed best, although there wasn't much difference between batches of 5000 up to 100000
                if (batch.size() >= batchSize) {
                    fileUpsertsWithRetry(batch, columns, columnClasses, tableName, connection, keywordEscapeChar);
                    batch = new ArrayList<>();
                }
            }
            if (!batch.isEmpty()) {
                fileUpsertsWithRetry(batch, columns, columnClasses, tableName, connection, keywordEscapeChar);
            }
        }
    }

    private static List<String> createHeaderList(CSVParser csvParser) {
        Map<String, Integer> csvHeaderMap = csvParser.getHeaderMap();

        //the parser stores the columns in a map, with values being the integer ordinal
        String[] arr = new String[csvHeaderMap.size()];
        for (Map.Entry<String, Integer> entry : csvHeaderMap.entrySet()) {
            arr[entry.getValue()] = entry.getKey();
        }

        List<String> ret = new ArrayList<>();

        for (String column: arr) {
            ret.add(column);
        }

        return ret;
    }

    private static Map<String, Class> createHeaderColumnMap(String entryFileName, JsonNode allColumnClassMappings, List<String> columns) throws Exception {

        Map<String, Class> ret = new HashMap<>();

        JsonNode columnClassMappings = allColumnClassMappings.get(entryFileName);

        for (String column: columns) {
            JsonNode node = columnClassMappings.get(column);
            if (node == null) {
                throw new Exception("Failed to find class for column " + column + " in " + entryFileName);
            }
            String className = node.asText();
            Class cls = null;

            //getting the name of the primitive types returns "int", but
            //this doesn't work in reverse, trying to get them using Class.forName("int")
            //so we have to manually check for these primitive types
            if (className.equals("int")) {
                cls = Integer.TYPE;
            } else if (className.equals("long")) {
                cls = Long.TYPE;
            } else if (className.equals("boolean")) {
                cls = Boolean.TYPE;
            } else if (className.equals("byte")) {
                cls = Byte.TYPE;
            } else if (className.equals("double")) {
                cls = Double.TYPE;
            } else {
                cls = Class.forName(className);
            }

            ret.put(column, cls);
        }

        return ret;
    }


    private static void fileDeletesForTable(List<CSVRecord> csvRecords, List<String> columns, Map<String, Class> columnClasses,
                                            String tableName, Connection connection) throws Exception {

        if (csvRecords.isEmpty()) {
            return;
        }

        LOG.trace("Deleting " + csvRecords.size() + " records from " + tableName);

        PreparedStatement psDelete = null;
        try {
            psDelete = connection.prepareStatement("delete from " + tableName + " where id = ?");

            for (CSVRecord csvRecord : csvRecords) {

                addToStatement(psDelete, csvRecord, COL_ID, columnClasses, 1);
                psDelete.addBatch();
            }

            psDelete.executeBatch();
            connection.commit();

            //LOG.trace("Deletes committed " + csvRecords.size() + " records from " + tableName);

        } catch (Exception ex) {
            connection.rollback();
            LOG.error("Exception with upsert " + psDelete.toString());
            throw new Exception("Exception with upsert. Details in log " +  ex);

        } finally {
            if (psDelete != null) {
                psDelete.close();
            }
        }
    }

    private static void addToStatement(PreparedStatement statement, CSVRecord csvRecord, String column, Map<String, Class> columnClasses, int index) throws Exception {

        String value = csvRecord.get(column);
        Class fieldCls = columnClasses.get(column);

        if (fieldCls == String.class) {
            if (Strings.isNullOrEmpty(value)) {
                statement.setNull(index, Types.VARCHAR);
            } else {
                statement.setString(index, (String)value);
            }

        } else if (fieldCls == Date.class) {
            if (Strings.isNullOrEmpty(value)) {
                statement.setNull(index, Types.DATE);
            } else {
                //Date d = new SimpleDateFormat(DATE_FORMAT).parse(value);
                //statement.setTimestamp(index, new Timestamp(d.getTime()));
                Date d = null;
                try {
                    d = new SimpleDateFormat(DATE_TIME_FORMAT).parse(value);
                } catch (ParseException e) {
                    d = new SimpleDateFormat(DATE_FORMAT).parse(value);
                }
                statement.setTimestamp(index, new Timestamp(d.getTime()));
            }

        } else if (fieldCls == BigDecimal.class) {
            if (Strings.isNullOrEmpty(value)) {
                statement.setNull(index, Types.DECIMAL);
            } else {
                BigDecimal bd = new BigDecimal(value);
                statement.setBigDecimal(index, bd);
            }

        } else if (fieldCls == Integer.class
            || fieldCls == Integer.TYPE) {

            if (Strings.isNullOrEmpty(value)) {
                statement.setNull(index, Types.INTEGER);
            } else {
                int i = Integer.parseInt(value);
                statement.setInt(index, i);
            }

        } else if (fieldCls == Long.class
            || fieldCls == Long.TYPE) {

            if (Strings.isNullOrEmpty(value)) {
                statement.setNull(index, Types.BIGINT);
            } else {
                long l = Long.parseLong(value);
                statement.setLong(index, l);
            }

        } else if (fieldCls == Boolean.class
            || fieldCls == Boolean.TYPE) {

            //explicitly test for expected values rather than using Boolean.parseBoolean(..) so anything
            //not expected (e.g. 1 or 0) is treated as an error
            if (Strings.isNullOrEmpty(value)) {
                statement.setNull(index, Types.BOOLEAN);

            } else if (value.equalsIgnoreCase("true")
                    || value.equalsIgnoreCase("1")) {
                statement.setBoolean(index, true);

            } else if (value.equalsIgnoreCase("false")
                    || value.equalsIgnoreCase("0")) {
                statement.setBoolean(index, false);

            } else {
                throw new Exception("Unexpected boolean value [" + value + "] in column [" + column + "]");
            }

        } else {
            throw new Exception("Unsupported value class " + fieldCls);
        }
    }



    private static PreparedStatement createUpsertPreparedStatement(String tableName, List<String> columns, Connection connection, String keywordEscapeChar) throws Exception {

        if (ConnectionManager.isSqlServer(connection)) {

            //keywordEscapeChar = "";

            /*
            SQL Server doesn't support upserts in a single statement, so we need to handle this with an attempted update
            and then an insert if that didn't work

             e.g.
            UPDATE dbo.AccountDetails
            SET Etc = @Etc
            WHERE Email = @Email

            INSERT dbo.AccountDetails ( Email, Etc )
            SELECT @Email, @Etc
            WHERE @@ROWCOUNT=0
             */

            //first write out the prepared statement for the UPDATE
            //the columns are written to the prepared statement in the order supplied, meaning ID is
            //always first. So we need to format this prepared statement in such a way that we can accept it
            //as the first parameter, even though syntax means it is needed last
            StringBuilder sql = new StringBuilder();
            sql.append("DECLARE @id_tmp bigint;");
            sql.append("SET @id_tmp = ?;");
            sql.append("UPDATE " + tableName + " SET ");

            for (int i = 0; i < columns.size(); i++) {
                String column = columns.get(i);
                if (column.equals(COL_ID)) {
                    continue;
                }

                sql.append(keywordEscapeChar + column + keywordEscapeChar + " = ?");
                if (i + 1 < columns.size()) {
                    sql.append(", ");
                }
            }
            sql.append(" WHERE " + keywordEscapeChar + COL_ID + keywordEscapeChar + " = @id_tmp;");

            //then write out SQL for an insert to run if the above update affected zero rows
            sql.append("INSERT INTO " + tableName + "(");

            for (int i = 0; i < columns.size(); i++) {
                String column = columns.get(i);
                sql.append(keywordEscapeChar + column + keywordEscapeChar);
                if (i + 1 < columns.size()) {
                    sql.append(", ");
                }
            }

            sql.append(") SELECT ");

            for (int i = 0; i < columns.size(); i++) {
                sql.append("?");
                if (i + 1 < columns.size()) {
                    sql.append(", ");
                }
            }

            sql.append(" WHERE @@ROWCOUNT=0;");

            return connection.prepareStatement(sql.toString());

        } else if (ConnectionManager.isPostgreSQL(connection)) {

            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO " + tableName + " (");

            for (int i = 0; i < columns.size(); i++) {
                String column = columns.get(i);
                sql.append(keywordEscapeChar + column + keywordEscapeChar);
                if (i + 1 < columns.size()) {
                    sql.append(", ");
                }
            }

            sql.append(") VALUES (");

            for (int i = 0; i < columns.size(); i++) {
                sql.append("?");
                if (i + 1 < columns.size()) {
                    sql.append(", ");
                }
            }

            sql.append(") ON CONFLICT (");
            sql.append(COL_ID);
            sql.append(") DO UPDATE SET ");

            for (int i = 0; i < columns.size(); i++) {
                String column = columns.get(i);
                if (column.equals(COL_ID)) {
                    continue;
                }

                sql.append(keywordEscapeChar + column + keywordEscapeChar + " = EXCLUDED." + keywordEscapeChar + column + keywordEscapeChar);
                if (i + 1 < columns.size()) {
                    sql.append(", ");
                }
            }

            sql.append(";");

            return connection.prepareStatement(sql.toString());

        } else {

            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO " + tableName + "(");

            for (int i = 0; i < columns.size(); i++) {
                String column = columns.get(i);
                sql.append(keywordEscapeChar + column + keywordEscapeChar);
                if (i + 1 < columns.size()) {
                    sql.append(", ");
                }
            }

            sql.append(") VALUES (");

            for (int i = 0; i < columns.size(); i++) {
                sql.append("?");
                if (i + 1 < columns.size()) {
                    sql.append(", ");
                }
            }

            sql.append(") ON DUPLICATE KEY UPDATE ");

            for (int i = 0; i < columns.size(); i++) {
                String column = columns.get(i);
                if (column.equals(COL_ID)) {
                    continue;
                }

                sql.append(keywordEscapeChar + column + keywordEscapeChar + " = VALUES(" + keywordEscapeChar + column + keywordEscapeChar + ")");
                if (i + 1 < columns.size()) {
                    sql.append(", ");
                }
            }

            sql.append(";");

            return connection.prepareStatement(sql.toString());
        }
    }

    /**
     * When multiple subscriber queue readers are writing to the same subscriber DB, we regularly see one of them
     * killed due to a deadlock. This seems to be a transient issue, so backing off for a few seconds and trying
     * again should mitigate it
     */
    private static void fileUpsertsWithRetry(List<CSVRecord> csvRecords, List<String> columns, Map<String, Class> columnClasses,
                                    String tableName, Connection connection, String keywordEscapeChar) throws Exception {

        DeadlockHandler h = new DeadlockHandler();
        h.setDelayBackOff(true);
        h.setMaxAttempts(20); //this will give it a couple of hours
        while (true) {
            try {
                fileUpsertImpl(csvRecords, columns, columnClasses, tableName, connection, keywordEscapeChar);
                break;

            } catch (Exception ex) {
                if (!h.canHandleError(ex)) {
                    //if we've had too many goes or its an unexpected error, then log the details of the batch and throw the exception up
                    logBatchError(csvRecords, columns);
                    throw ex;
                }
            }
        }
    }

    private static void logBatchError(List<CSVRecord> csvRecords, List<String> columns) {
        LOG.error("Failed on batch:");
        try {
            LOG.error("" + String.join(", ", columns));
            for (CSVRecord record : csvRecords) {
                List<String> recordList = new ArrayList<>();
                for (String col : columns) {
                    String val = record.get(col);
                    recordList.add(val);
                }
                LOG.error("" + String.join(", ", recordList));
            }
        } catch (Throwable t) {
            LOG.error("ERROR LOGGING FAILED BATCH", t);
        }
    }


    private static void fileUpsertImpl(List<CSVRecord> csvRecords, List<String> columns, Map<String, Class> columnClasses,
                                       String tableName, Connection connection, String keywordEscapeChar) throws Exception {

        if (csvRecords.isEmpty()) {
            return;
        }

        if (tableName.equalsIgnoreCase("patient_additional")) {
            LOG.warn("Skipping upsert to patient_additional table");
            return;
        }

        //the first element in the columns list is the save mode, so remove that
        columns = new ArrayList<>(columns);
        columns.remove(COL_IS_DELETE);

        PreparedStatement psInsert = null;

        //wrap in try/catch so we can log out the SQL that failed
        try {
            //LOG.trace("Upserting batch of " + csvRecords.size() + " to " + tableName);

            psInsert = createUpsertPreparedStatement(tableName, columns, connection, keywordEscapeChar);

            for (CSVRecord csvRecord: csvRecords) {

                int index = 1;
                for (String column: columns) {
                    addToStatement(psInsert, csvRecord, column, columnClasses, index);
                    index ++;
                }

                //if SQL Server, then we need to add the values a SECOND time because the UPSEERT syntax used needs it
                if (ConnectionManager.isSqlServer(connection)) {
                    for (String column: columns) {
                        addToStatement(psInsert, csvRecord, column, columnClasses, index);
                        index ++;
                    }
                }

                psInsert.addBatch();
            }

            psInsert.executeBatch();

            connection.commit();

            //LOG.trace("Upsert committed " + csvRecords.size() + " to " + tableName);

        } catch (Exception ex) {
            connection.rollback();
            LOG.error("Exception with upsert to " + tableName + ": " + ex.getMessage());

            //throw the original exception up
            throw ex;

        } finally {

            if (psInsert != null) {
                psInsert.close();
            }
        }
    }

    private static void deleteLeftOverPersonRecords(Connection connection) throws Exception {
        PreparedStatement delete =
                connection.prepareStatement("DELETE FROM person WHERE NOT EXISTS (SELECT 1 FROM patient WHERE patient.person_id = person.id);");
        delete.executeBatch();
        delete.close();
        connection.commit();
    }

    private static void deleteLeftOverPatientUPRNRecords(Connection connection) throws Exception {
        PreparedStatement delete =
                connection.prepareStatement("DELETE FROM patient_uprn WHERE NOT EXISTS (SELECT 1 FROM patient WHERE patient.id = patient_uprn.patient_id);");
        delete.executeBatch();
        delete.close();
        connection.commit();
    }

    /*private static int getBatchSize(String url) {
        return batchSizes.get(url).intValue();
    }

    private static String getKeywordEscapeChar(String url) {
        return escapeCharacters.get(url);
    }*/



    /*public static Connection openConnection(JsonNode config) throws Exception {

        String url = config.get("enterprise_url").asText();
        HikariDataSource pool = connectionPools.get(url);
        if (pool == null) {
            synchronized (connectionPools) {
                pool = connectionPools.get(url);
                if (pool == null) {

                    String driverClass = config.get("driverClass").asText();
                    String username = config.get("enterprise_username").asText();
                    String password = config.get("enterprise_password").asText();

                    //force the driver to be loaded
                    Class.forName(driverClass);

                    pool = new HikariDataSource();
                    pool.setJdbcUrl(url);
                    pool.setUsername(username);
                    pool.setPassword(password);
                    pool.setMaximumPoolSize(3);
                    pool.setMinimumIdle(1);
                    pool.setIdleTimeout(60000);
                    pool.setPoolName("EnterpriseFilerConnectionPool" + url);
                    pool.setAutoCommit(false);

                    connectionPools.put(url, pool);

                    //cache the escape string too, since getting the metadata each time is extra load
                    Connection conn = pool.getConnection();
                    String escapeStr = conn.getMetaData().getIdentifierQuoteString();
                    escapeCharacters.put(url, escapeStr);
                    conn.close();

                    //and catch the batch size
                    int batchSize = 50;
                    if (config.has("batch_size")) {
                        batchSize = config.get("batch_size").asInt();
                        if (batchSize <= 0) {
                            throw new Exception("Invalid batch size");
                        }
                    }
                    batchSizes.put(url, new Integer(batchSize));
                }
            }
        }

        return pool.getConnection();
    }*/



}

