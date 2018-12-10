package org.endeavourhealth.subscriber.filer;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PCRFiler {
    private static final Logger LOG = LoggerFactory.getLogger(PCRFiler.class);

    private static final String COLUMN_CLASS_MAPPINGS = "ColumnClassMappings.json";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT;

    private static final String COL_SAVE_MODE = "save_mode";
    private static final String COL_ID = "id";

    private static final String UPSERT = "Upsert";
    private static final String DELETE = "Delete";

    private static final int UPSERT_ATTEMPTS = 10;
    //TODO maybe more need add to list but only these 2 seem to hit so far
    private static List<String> dependencyNames = Arrays.asList("medication_statement.csv", "medication_order.csv", "patient_address.csv");

    private static Map<String, HikariDataSource> connectionPools = new ConcurrentHashMap<>();
    private static Map<String, String> escapeCharacters = new ConcurrentHashMap<>();
    private static Map<String, Integer> batchSizes = new ConcurrentHashMap<>();

    public static void file(UUID batchId, String base64, String configName) throws Exception {

        byte[] bytes = Base64.getDecoder().decode(base64);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ZipInputStream zis = new ZipInputStream(bais);

        LOG.trace("Filing " + bytes.length + "b from batch " + batchId + " into " + configName);

        JsonNode columnClassMappings = null;

        //for example [db_subscriber, subscriber_pophealth_v1]
        JsonNode config = ConfigManager.getConfigurationAsJson(configName, "db_subscriber");
        Connection connection = openConnection(config);

        String url = config.get("pcr_url").asText();
        String keywordEscapeChar = getKeywordEscapeChar(url);
        int batchSize = getBatchSize(url);
        List<String> pendingFiles = new ArrayList<>();
        ZipInputStream zisPend = null;
        try {
            List<DeleteWrapper> deletes = new ArrayList<>();

            while (true) {
                ZipEntry entry = zis.getNextEntry();
                if (entry == null) {
                    break;
                }

                String entryFileName = entry.getName();
                // Some tables refer to foreign keys in other tables. We need to file the entries containing foreign
                // keys (eg MedicationAmount) before the entry that refers to its id (eg MedicationStatement)
                if (dependencyNames.contains(entryFileName)) {
                    pendingFiles.add(entryFileName);  //Process at end.
                    continue;
                }
                byte[] entryBytes = readZipEntry(zis);

                if (entryFileName.equals(COLUMN_CLASS_MAPPINGS)) {
                    String jsonStr = new String(entryBytes);
                    columnClassMappings = ObjectMapperPool.getInstance().readTree(jsonStr);

                } else {
                    processCsvData(entryFileName, entryBytes, columnClassMappings, connection, keywordEscapeChar, batchSize, deletes);
                }
            }
            //now file pending upserts for pending files
            ByteArrayInputStream baisPend = new ByteArrayInputStream(bytes);
            zisPend = new ZipInputStream(baisPend); // new input to reset start
            while (true) {
                ZipEntry entry = zisPend.getNextEntry();
                if (entry == null) {
                    break;
                }
                String entryFileName = entry.getName();
                if (!pendingFiles.contains(entryFileName)) {
                    continue;
                }
                byte[] entryBytes = readZipEntry(zisPend);
                processCsvData(entryFileName, entryBytes, columnClassMappings, connection, keywordEscapeChar, batchSize, deletes);
            }

            //now files the deletes
            fileDeletes(deletes, connection);

        } catch (Exception ex) {
            //if we get an exception, write out the zip file, so we can investigate what was being filed
            writeZipFile(bytes);

            throw new Exception("Exception filing to " + url, ex);

        } finally {
            if (zis != null) {
                zis.close();
            }
            if (zisPend != null) {
                zisPend.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    private static void writeZipFile(byte[] bytes) {
        File f = new File("PCRFileError.zip");
        try {
            FileUtils.writeByteArrayToFile(f, bytes);
            LOG.error("Written ZIP file to " + f);

        } catch (IOException ex) {
            LOG.error("Failed to write ZIP file to " + f, ex);
        }
    }

    private static void fileDeletes(List<DeleteWrapper> deletes, Connection connection) throws Exception {

        String currentTable = null;
        List<CSVRecord> currentRecords = null;
        List<String> currentColumns = null;
        HashMap<String, Class> currentColumnClasses = null;

        //go backwards, so we delete dependent records first
        for (int i=deletes.size()-1; i>=0; i--) {
            DeleteWrapper wrapper = deletes.get(i);
            String tableName = wrapper.getTableName();

            if (currentTable == null
                    || !currentTable.equals(tableName)) {

                //file any deletes we've already built up
                if (currentRecords != null
                        && !currentRecords.isEmpty()) {
                    LOG.trace("Deleting " + currentRecords.size() + " from " + currentTable);
                    fileDeletes(currentRecords, currentColumns, currentColumnClasses, currentTable, connection);
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
            LOG.trace("Deleting " + currentRecords.size() + " from " + currentTable);
            fileDeletes(currentRecords, currentColumns, currentColumnClasses, currentTable, connection);
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

    private static void processCsvData(String entryFileName, byte[] csvBytes, JsonNode allColumnClassMappings, Connection connection, String keywordEscapeChar, int batchSize, List<DeleteWrapper> deletes) throws Exception {

        String tableName = Files.getNameWithoutExtension(entryFileName);
        ByteArrayInputStream bais = new ByteArrayInputStream(csvBytes);
        InputStreamReader isr = new InputStreamReader(bais);
        CSVParser csvParser = new CSVParser(isr, CSV_FORMAT.withHeader());

        //find out what columns we've got
        Map<String, Integer> csvHeaderMap = csvParser.getHeaderMap();
        List<String> columns = new ArrayList<>();
        HashMap<String, Class> columnClasses = new HashMap<>();
        createHeaderColumnMap(csvHeaderMap, entryFileName, allColumnClassMappings, columns, columnClasses);

        //since we're dealing with small volumes, we can just read keep all the records in memory
        List<CSVRecord> upserts = new ArrayList<>();

        Iterator<CSVRecord> csvIterator = csvParser.iterator();
        while (csvIterator.hasNext()) {
            CSVRecord csvRecord = csvIterator.next();
            String saveMode = csvRecord.get(COL_SAVE_MODE);

            if (saveMode.equalsIgnoreCase(DELETE)) {
                //we have to play deletes in reverse, so just add to this list and they'll be processed after all the upserts
                deletes.add(new DeleteWrapper(tableName, csvRecord, columns, columnClasses));

            } else if (saveMode.equalsIgnoreCase(UPSERT)) {
                upserts.add(csvRecord);

            } else {
                throw new Exception("Unknown save mode " + saveMode);
            }
        }

        //when doing a bulk, we can have 300,000+ practitioners, so do them in batches, so we're
        //not keeping huge DB transactions open
        List<CSVRecord> batch = new ArrayList<>();
        for (CSVRecord record: upserts) {
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

    private static void createHeaderColumnMap(Map<String, Integer> csvHeaderMap,
                                                String entryFileName,
                                                JsonNode allColumnClassMappings,
                                                List<String> columns,
                                                HashMap<String, Class> columnClasses) throws Exception {
        //get the column names, ordered
        String[] arr = new String[csvHeaderMap.size()];
        for (Map.Entry<String, Integer> entry : csvHeaderMap.entrySet())
            arr[entry.getValue()] = entry.getKey();

        for (String column: arr)
            columns.add(column);

        //sort out column classes
        JsonNode columnClassMappings = allColumnClassMappings.get(entryFileName);

        for (String column: csvHeaderMap.keySet()) {
            JsonNode node = columnClassMappings.get(column);
            if (node == null) {
                throw new Exception("Failed to find class for column " + column + " in " + entryFileName);
            }
            String className = node.asText();
            Class cls = null;

            //getting the name of the primative types returns "int", but
            //this doesn't work in reverse, trying to get them using Class.forName("int")
            //so we have to manually check for these primative types
            if (className.equals("int")) {
                cls = Integer.TYPE;
            } else if (className.equals("long")) {
                cls = Long.TYPE;
            } else if (className.equals("boolean")) {
                cls = Boolean.TYPE;
            } else if (className.equals("char")) {
                cls = Character.TYPE;
            } else if (className.equals("double")) {
                cls = Double.TYPE;
            } else {
                cls = Class.forName(className);
            }

            columnClasses.put(column, cls);
        }
    }

    private static void fileDeletes(List<CSVRecord> csvRecords, List<String> columns, HashMap<String, Class> columnClasses,
                                    String tableName, Connection connection) throws Exception {

        if (csvRecords.isEmpty()) {
            return;
        }

        PreparedStatement delete = connection.prepareStatement("delete from " + tableName + " where id = ?");

        for (CSVRecord csvRecord: csvRecords) {

            addToStatement(delete, csvRecord, COL_ID, columnClasses, 1);
            delete.addBatch();
        }

        delete.executeBatch();
        delete.close(); //was forgetting to do this
        connection.commit();
    }

    private static void addToStatement(PreparedStatement statement, CSVRecord csvRecord, String column, HashMap<String, Class> columnClasses, int index) throws Exception {

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
                Date d = new SimpleDateFormat(DATE_FORMAT).parse(value);
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

            if (Strings.isNullOrEmpty(value)) {
                statement.setNull(index, Types.BOOLEAN);
            } else {
                boolean b = Boolean.parseBoolean(value);
                statement.setBoolean(index, b);
            }
        } else if (fieldCls == Character.class
                    || fieldCls == Character.TYPE) {

                if (Strings.isNullOrEmpty(value)) {
                    statement.setNull(index, Types.TINYINT);
                } else {
                    statement.setString(index, (String) value);
                }

        } else {
            throw new Exception("Unsupported value class " + fieldCls);
        }
    }

    private static PreparedStatement createUpsertPreparedStatement(String tableName, List<String> columns, Connection connection, String keywordEscapeChar) throws Exception {

        StringBuilder sql = new StringBuilder();

        sql.append("insert into " + tableName + "(");
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            sql.append(keywordEscapeChar + column + keywordEscapeChar);
            if (i + 1 < columns.size()) {
                sql.append(", ");
            }
        }

        sql.append(") values (");
        for (int i = 0; i < columns.size(); i++) {
            sql.append("?");
            if (i + 1 < columns.size()) {
                sql.append(", ");
            }
        }

        sql.append(") on duplicate key update ");
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

    /**
     * When multiple subscriber queue readers are writing to the same subscriber DB, we regularly see one of them
     * killed due to a deadlock. This seems to be a transient issue, so backing off for a few seconds and trying
     * again should mitigate it
     */
    private static void fileUpsertsWithRetry(List<CSVRecord> csvRecords, List<String> columns, HashMap<String, Class> columnClasses,
                                    String tableName, Connection connection, String keywordEscapeChar) throws Exception {

        int attemptsMade = 0;
        while (true) {

            try {
                fileUpserts(csvRecords, columns, columnClasses, tableName, connection, keywordEscapeChar);

                //if we execute without error, break out
                break;

            } catch (BatchUpdateException ex) {
                String msg = ex.getMessage();
                if (attemptsMade < UPSERT_ATTEMPTS
                        && msg != null
                        && msg.startsWith("Deadlock found when trying to get lock; try restarting transaction")) {

                    //if the message matches the deadlock one, then wait a while and try again
                    attemptsMade ++;
                    long secondsWait = attemptsMade;
                    LOG.error("Upsert to " + tableName + " failed due to deadlock, so will try again in " + secondsWait + " seconds");
                    Thread.sleep(1000 * secondsWait);
                    continue;

                } else {
                    //log the records we failed on
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

                    //if the message isn't exactly the one we're looking for, just throw the exception as normal
                    throw ex;
                }
            }

        }
    }

    private static void fileUpserts(List<CSVRecord> csvRecords, List<String> columns, HashMap<String, Class> columnClasses,
                                    String tableName, Connection connection, String keywordEscapeChar) throws Exception {

        if (csvRecords.isEmpty()) {
            return;
        }

        //the first element in the columns list is the save mode, so remove that
        columns = new ArrayList<>(columns);
        columns.remove(COL_SAVE_MODE);

        PreparedStatement insert = createUpsertPreparedStatement(tableName, columns, connection, keywordEscapeChar);

        for (CSVRecord csvRecord: csvRecords) {

            int index = 1;
            for (String column: columns) {
                addToStatement(insert, csvRecord, column, columnClasses, index);
                index ++;
            }

            insert.addBatch();
        }

        //wrap in try/catch so we can log out the SQL that failed
        try {
            insert.executeBatch();

        } catch (Exception ex) {
            LOG.error(insert.toString());
            throw ex;
        }

        insert.close();
        connection.commit();
    }



    private static int getBatchSize(String url) {
        return batchSizes.get(url).intValue();
    }

    private static String getKeywordEscapeChar(String url) {
        return escapeCharacters.get(url);
    }

    public static Connection openConnection(JsonNode config) throws Exception {

        String url = config.get("pcr_url").asText();
        HikariDataSource pool = connectionPools.get(url);
        if (pool == null) {
            synchronized (connectionPools) {
                pool = connectionPools.get(url);
                if (pool == null) {

                    String driverClass = config.get("driverClass").asText();
                    String username = config.get("pcr_username").asText();
                    String password = config.get("pcr_password").asText();

                    //force the driver to be loaded
                    Class.forName(driverClass);

                    pool = new HikariDataSource();
                    pool.setJdbcUrl(url);
                    pool.setUsername(username);
                    pool.setPassword(password);
                    pool.setMaximumPoolSize(3);
                    pool.setMinimumIdle(1);
                    pool.setIdleTimeout(60000);
                    pool.setPoolName("PCRFilerConnectionPool" + url);
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
    }
}