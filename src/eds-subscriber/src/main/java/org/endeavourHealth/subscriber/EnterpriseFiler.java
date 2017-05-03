package org.endeavourhealth.subscriber;

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

import javax.xml.crypto.dsig.TransformException;
import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class EnterpriseFiler {
    private static final Logger LOG = LoggerFactory.getLogger(EnterpriseFiler.class);

    private static final String COLUMN_CLASS_MAPPINGS = "ColumnClassMappings.json";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT;

    private static final String COL_SAVE_MODE = "save_mode";
    private static final String COL_ID = "id";

    private static final String UPSERT = "Upsert";
    private static final String DELETE = "Delete";

    //private static final String ZIP_ENTRY = "EnterpriseData.xml";

    //private static String keywordEscapeChar = null; //different DBs use different chars to escape keywords (" on pg, ` on mysql)

    private static Map<String, HikariDataSource> connectionPools = new ConcurrentHashMap<>();
    private static Map<String, String> escapeCharacters = new ConcurrentHashMap<>();

    public static void file(UUID batchId, String base64, String configName) throws Exception {

        byte[] bytes = Base64.getDecoder().decode(base64);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ZipInputStream zis = new ZipInputStream(bais);

        LOG.trace("Filing " + bytes.length + "b from batch " + batchId + " into " + configName);

        JsonNode columnClassMappings = null;

        JsonNode config = ConfigManager.getConfigurationAsJson(configName, "enterprise");
        String url = config.get("url").asText();
        Connection connection = openConnection(url, config);
        String keywordEscapeChar = getKeywordEscapeChar(url);

        try {
            List<DeleteWrapper> deletes = new ArrayList<>();

            while (true) {
                ZipEntry entry = zis.getNextEntry();
                if (entry == null) {
                    break;
                }

                String entryFileName = entry.getName();
                byte[] entryBytes = readZipEntry(zis);

                if (entryFileName.equals(COLUMN_CLASS_MAPPINGS)) {
                    String jsonStr = new String(entryBytes);
                    columnClassMappings = ObjectMapperPool.getInstance().readTree(jsonStr);

                } else {
                    fileCsvData(entryFileName, entryBytes, columnClassMappings, connection, keywordEscapeChar, deletes);
                }
            }

            //now files the deletes
            fileDeletes(deletes, connection);

        } catch (Exception ex) {
            //if we get an exception, write out the zip file, so we can investigate what was being filed
            writeZipFile(bytes);

            throw new TransformException("Exception filing to " + url, ex);

        } finally {
            if (zis != null) {
                zis.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }


    private static void writeZipFile(byte[] bytes) {
        File f = new File("EnterpriseFileError.zip");
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

        //LOG.trace("Got " + deletes.size() + " deletes");

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

    private static void fileCsvData(String entryFileName, byte[] csvBytes, JsonNode allColumnClassMappings, Connection connection, String keywordEscapeChar, List<DeleteWrapper> deletes) throws Exception {

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
        //List<CSVRecord> deletes = new ArrayList<>();

        Iterator<CSVRecord> csvIterator = csvParser.iterator();
        while (csvIterator.hasNext()) {
            CSVRecord csvRecord = csvIterator.next();
            String saveMode = csvRecord.get(COL_SAVE_MODE);

            if (saveMode.equalsIgnoreCase(DELETE)) {
                //we have to play deletes in reverse, so don't delete immediately. Cache for now.
                deletes.add(new DeleteWrapper(tableName, csvRecord, columns, columnClasses));
                //deletes.add(csvRecord);
            } else {
                upserts.add(csvRecord);
            }
        }

        //when doing a bulk, we can have 300,000+ practitioners, so do them in batches, so we're
        //not keeping huge DB transactions open
        List<CSVRecord> batch = new ArrayList<>();
        for (CSVRecord record: upserts) {
            batch.add(record);

            //in testing, batches of 20000 seemed best, although there wasn't much difference between batches of 5000 up to 100000
            if (batch.size() >= 20000) {
                fileUpserts(batch, columns, columnClasses, tableName, connection, keywordEscapeChar);
                batch = new ArrayList<>();
            }
        }
        if (!batch.isEmpty()) {
            fileUpserts(batch, columns, columnClasses, tableName, connection, keywordEscapeChar);
        }
        //fileUpserts(upserts, columns, columnClasses, tableName, connection);
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
                throw new TransformException("Failed to find class for column " + column + " in " + entryFileName);
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

        } else {
            throw new Exception("Unsupported value class " + fieldCls);
        }
    }

    private static void fileUpserts(List<CSVRecord> csvRecords, List<String> columns, HashMap<String, Class> columnClasses,
                                    String tableName, Connection connection, String keywordEscapeChar) throws Exception {

        if (csvRecords.isEmpty()) {
            return;
        }

        //first try treating all the objects as inserts as most transactions will be inserts
        try {
            fileInserts(csvRecords, columns, columnClasses, tableName, connection, keywordEscapeChar);
            //if we made it here, all the records were inserted ok
            return;

        } catch (SQLException ex) {
            //if we get a SQL Exception, then it's probably because one of the upserts should be an update
            connection.rollback();
        }

        //run all the CSV records through as updates
        int[] recordsUpdated = fileUpdates(csvRecords, columns, columnClasses, tableName, connection, keywordEscapeChar);

        //find all the csv records where the update didn't affect any rows and treat them as inserts
        //Note: we have some batches containing the SAME record more than once. So the first instance should
        //be treated as an insert, and the second as an update
        Set<String> trueInsertIds = new HashSet<>();
        List<CSVRecord> trueInserts = new ArrayList<>();
        List<CSVRecord> laterUpdates = new ArrayList<>();

        for (int i=0; i<recordsUpdated.length; i++) {
            if (recordsUpdated[i] == 0) {
                CSVRecord record = csvRecords.get(i);
                String id = record.get(COL_ID);
                if (!trueInsertIds.contains(id)) {
                    trueInserts.add(record);
                    trueInsertIds.add(id);

                } else {
                    laterUpdates.add(record);
                }
            }
        }

        //if the updates all went through ok, just return out
        if (trueInserts.isEmpty()
                && laterUpdates.isEmpty()) {
            return;
        }

        //file the true inserts
        try {
            fileInserts(trueInserts, columns, columnClasses, tableName, connection, keywordEscapeChar);

        } catch (SQLException ex) {
            StringBuffer s = new StringBuffer();
            s.append("Failed to insert into " + tableName + " record(s): ");
            for (CSVRecord record: trueInserts) {
                s.append("\n");
                for (String column: columns) {
                    s.append(record.get(column) + ", ");
                }
            }

            throw new Exception(s.toString(), ex);
        }

        //then do the updates to records we're also inserting
        recordsUpdated = fileUpdates(laterUpdates, columns, columnClasses, tableName, connection, keywordEscapeChar);

        for (int i=0; i<recordsUpdated.length; i++) {
            if (recordsUpdated[i] == 0) {
                CSVRecord record = laterUpdates.get(i);

                StringBuffer s = new StringBuffer();
                s.append("Failed to insert or update " + tableName + " record: ");
                for (String column: columns) {
                    s.append(record.get(column) + ", ");
                }

                throw new Exception(s.toString());
            }
        }
    }

    /*private static void fileUpserts(List<CSVRecord> csvRecords, List<String> columns, HashMap<String, Class> columnClasses,
                                    String tableName, Connection connection, String keywordEscapeChar) throws Exception {

        if (csvRecords.isEmpty()) {
            return;
        }

        //first try treating all the objects as inserts
        try {
            fileInserts(csvRecords, columns, columnClasses, tableName, connection, keywordEscapeChar);

        } catch (SQLException ex) {
            //if we get a SQL Exception, then it's probably because one of the objects is an update, so we try to
            //save each object separately first as an insert, then as an update
            connection.rollback();

            //if we have only one record, just try performing an update
            if (csvRecords.size() == 1) {
                CSVRecord singleRecord = csvRecords.get(0);
                List<CSVRecord> singleRecords = new ArrayList<>();
                singleRecords.add(singleRecord);

                try {
                    fileUpdates(singleRecords, columns, columnClasses, tableName, connection, keywordEscapeChar);

                } catch (SQLException ex3) {
                    StringBuffer s = new StringBuffer();
                    s.append("Failed to insert or update " + tableName + " record: ");
                    for (String column: columns) {
                        s.append(singleRecord.get(column) + ", ");
                    }

                    //we've got two exceptions in scope - the original insert exception and the update exception
                    //so we need to log both. Since only one can be wrapped in the exception we throw, we
                    //log the other one out here
                    LOG.error("", ex);

                    throw new Exception(s.toString(), ex3);
                }

            } else {
                //if we have multilple records, try inserting them one at a time
                for (CSVRecord csvRecord: csvRecords) {
                    List<CSVRecord> singleRecord = new ArrayList<>();
                    singleRecord.add(csvRecord);

                    fileUpserts(singleRecord, columns, columnClasses, tableName, connection, keywordEscapeChar);
                }
            }
        }
    }*/

    private static void fileInserts(List<CSVRecord> csvRecords, List<String> columns, HashMap<String, Class> columnClasses,
                                    String tableName, Connection connection, String keywordEscapeChar) throws Exception {

        if (csvRecords.isEmpty()) {
            return;
        }

        int tableColumns = columns.size()-1; //substract one because we don't save the save_mode
        String parameters = createParameters(tableColumns);

        StringBuilder sb = new StringBuilder();
        for (String column: columns) {
            if (!column.equals(COL_SAVE_MODE)) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(keywordEscapeChar + column + keywordEscapeChar);
            }
        }
        String columnStr = sb.toString();

        PreparedStatement insert = connection.prepareStatement("insert into " + tableName + " (" + columnStr + ") values (" + parameters + ")");

        //the version with the named columns doesn't seem to work on all versions of MySQL, so removing until can fix
        //but we need it for the episode_of_care table, which has additional columns
        /*PreparedStatement insert = null;
        if (!tableName.equalsIgnoreCase("episode_of_care")) {
            insert = connection.prepareStatement("insert into " + tableName + " values (" + parameters + ")");

        } else {

            StringBuilder sb = new StringBuilder();
            for (String column: columns) {
                if (!column.equals(COL_SAVE_MODE)) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(keywordEscapeChar + column + keywordEscapeChar);
                }
            }
            String columnStr = sb.toString();

            insert = connection.prepareStatement("insert into " + tableName + " (" + columnStr + ") values (" + parameters + ")");
        }*/


        for (CSVRecord csvRecord: csvRecords) {

            int index = 1;
            for (String column: columns) {
                if (!column.equals(COL_SAVE_MODE)) {
                    addToStatement(insert, csvRecord, column, columnClasses, index);
                    index ++;
                }
            }

            insert.addBatch();
        }

        insert.executeBatch();
        insert.close(); //was forgetting to do this
        connection.commit();
    }

    private static String createParameters(int num) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<num; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("?");
        }
        return sb.toString();
    }

    private static int[] fileUpdates(List<CSVRecord> csvRecords, List<String> columns, HashMap<String, Class> columnClasses,
                                    String tableName, Connection connection, String keywordEscapeChar) throws Exception {

        if (csvRecords.isEmpty()) {
            return new int[0];
        }

        //build the update statement
        StringBuilder sb = new StringBuilder();
        sb.append("update " + tableName + " set ");

        boolean addComma = false;
        for (String column: columns) {

            if (column.equals(COL_SAVE_MODE)
                    || column.equals(COL_ID)) {
                continue;
            }

            if (addComma) {
                sb.append(", ");
            }
            addComma = true;

            sb.append(keywordEscapeChar + column + keywordEscapeChar + " = ?");
        }
        sb.append(" where " + COL_ID + " = ?");

        PreparedStatement update = connection.prepareStatement(sb.toString());

        //populate the statement with the updates
        for (CSVRecord csvRecord: csvRecords) {

            //add all the updated columns
            int index = 1;
            for (String column: columns) {

                if (column.equals(COL_SAVE_MODE)
                        || column.equals(COL_ID)) {
                    continue;
                }

                addToStatement(update, csvRecord, column, columnClasses, index);
                index ++;
            }

            //then add the ID
            addToStatement(update, csvRecord, COL_ID, columnClasses, index);
            update.addBatch();
        }

        //execute the update, making sure the update count is what we ex[ect
        int[] updateCounts = update.executeBatch();
        update.close(); //was forgetting to do this

        if (updateCounts.length != csvRecords.size()) {
            throw new Exception("Batch results length " + updateCounts.length + " doesn't match number of batches " + csvRecords.size());
        }

        connection.commit();

        return updateCounts;
    }

    /*private static void fileUpdates(List<CSVRecord> csvRecords, List<String> columns, HashMap<String, Class> columnClasses,
                                    String tableName, Connection connection, String keywordEscapeChar) throws Exception {

        if (csvRecords.isEmpty()) {
            return;
        }

        //build the update statement
        StringBuilder sb = new StringBuilder();
        sb.append("update " + tableName + " set ");

        boolean addComma = false;
        for (String column: columns) {

            if (column.equals(COL_SAVE_MODE)
                    || column.equals(COL_ID)) {
                continue;
            }

            if (addComma) {
                sb.append(", ");
            }
            addComma = true;

            sb.append(keywordEscapeChar + column + keywordEscapeChar + " = ?");
        }
        sb.append(" where " + COL_ID + " = ?");

        PreparedStatement update = connection.prepareStatement(sb.toString());

        //populate the statement with the updates
        for (CSVRecord csvRecord: csvRecords) {

            //add all the updated columns
            int index = 1;
            for (String column: columns) {

                if (column.equals(COL_SAVE_MODE)
                        || column.equals(COL_ID)) {
                    continue;
                }

                addToStatement(update, csvRecord, column, columnClasses, index);
                index ++;
            }

            //then add the ID
            addToStatement(update, csvRecord, COL_ID, columnClasses, index);
            update.addBatch();
        }

        //execute the update, making sure the update count is what we ex[ect
        int[] updateCounts = update.executeBatch();
        update.close(); //was forgetting to do this

        if (updateCounts[0] != csvRecords.size()) {
            throw new SQLException("Failed to update the right number of rows. Expected " + csvRecords.size() + " Got " + updateCounts[0]);
        }

        connection.commit();
    }*/

    private static String getKeywordEscapeChar(String url) {
        return escapeCharacters.get(url);
    }

    private static Connection openConnection(String url, JsonNode config) throws Exception {

        HikariDataSource pool = connectionPools.get(url);
        if (pool == null) {
            synchronized (connectionPools) {
                pool = connectionPools.get(url);
                if (pool == null) {

                    String driverClass = config.get("driverClass").asText();
                    String username = config.get("username").asText();
                    String password = config.get("password").asText();

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
                }
            }
        }

        return pool.getConnection();
    }
    /*private static Connection openConnection(JsonNode config) throws Exception {

        String driverClass = config.get("driverClass").asText();
        String url = config.get("url").asText();
        String username = config.get("username").asText();
        String password = config.get("password").asText();

        keywordEscapeChar = config.get("keywordEscapeChar").asText();

        //force the driver to be loaded
        Class.forName(driverClass);

        Connection conn = DriverManager.getConnection(url, username, password);
        conn.setAutoCommit(false);

        return conn;
    }*/
}


class DeleteWrapper {
    private String tableName = null;
    private CSVRecord record = null;
    private List<String> columns = null;
    private HashMap<String, Class> columnClasses = null;

    public DeleteWrapper(String tableName, CSVRecord record, List<String> columns, HashMap<String, Class> columnClasses) {
        this.tableName = tableName;
        this.record = record;
        this.columns = columns;
        this.columnClasses = columnClasses;
    }

    public String getTableName() {
        return tableName;
    }

    public CSVRecord getRecord() {
        return record;
    }

    public List<String> getColumns() {
        return columns;
    }

    public HashMap<String, Class> getColumnClasses() {
        return columnClasses;
    }
}