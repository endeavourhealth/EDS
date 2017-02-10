package org.endeavourhealth.subscriber;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.endeavourhealth.core.cache.ObjectMapperPool;
import org.endeavourhealth.core.data.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
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

    private static String keywordEscapeChar = null; //different DBs use different chars to escape keywords (" on pg, ` on mysql)

    public static void file(String base64) throws Exception {

        byte[] bytes = Base64.getDecoder().decode(base64);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ZipInputStream zis = new ZipInputStream(bais);

        JsonNode columnClassMappings = null;

        Connection connection = openConnection();

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
                    fileCsvData(entryFileName, entryBytes, columnClassMappings, connection, deletes);
                }
            }

            //now files the deletes
            fileDeletes(deletes, connection);

        } finally {
            if (zis != null) {
                zis.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    private static void fileDeletes(List<DeleteWrapper> deletes, Connection connection) throws Exception {

        String currentTable = null;
        List<CSVRecord> currentRecords = null;
        List<String> currentColumns = null;
        HashMap<String, Class> currentColumnClasses = null;

        LOG.trace("Got " + deletes.size() + " deletes");

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

    private static void fileCsvData(String entryFileName, byte[] csvBytes, JsonNode allColumnClassMappings, Connection connection, List<DeleteWrapper> deletes) throws Exception {

        String tableName = Files.getNameWithoutExtension(entryFileName);

        try (
            ByteArrayInputStream bais = new ByteArrayInputStream(csvBytes);
            InputStreamReader isr = new InputStreamReader(bais);
            CSVParser csvParser = new CSVParser(isr, CSV_FORMAT.withHeader());
        ) {

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

            fileUpserts(upserts, columns, columnClasses, tableName, connection);
            //fileDeletes(deletes, columns, columnClasses, tableName, connection);
        }
    }

    private static void createHeaderColumnMap(Map<String, Integer> csvHeaderMap,
                                                String entryFileName,
                                                JsonNode allColumnClassMappings,
                                                List<String> columns,
                                                HashMap<String, Class> columnClasses) throws Exception {
        //get the column names, ordered
        String[] arr = new String[csvHeaderMap.size()];
        for (String column: csvHeaderMap.keySet()) {
            Integer index = csvHeaderMap.get(column);
            arr[index.intValue()] = column;
        }
        for (String column: arr) {
            columns.add(column);
        }

        //sort out column classes
        JsonNode columnClassMappings = allColumnClassMappings.get(entryFileName);

        for (String column: csvHeaderMap.keySet()) {
            String className = columnClassMappings.get(column).asText();
            Class cls = null;

            //getting the name of the primative types returns "int", but
            //this doesn't work in reverse, trying to get them using Class.forName("int")
            //so we have to manually check for these primative types
            if (className.equals("int")) {
                cls = Integer.TYPE;
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

        } else if (fieldCls == Long.class) {

            if (Strings.isNullOrEmpty(value)) {
                statement.setNull(index, Types.INTEGER);
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
                                    String tableName, Connection connection) throws Exception {

        if (csvRecords.isEmpty()) {
            return;
        }

        //first try treating all the objects as inserts
        try {
            fileInserts(csvRecords, columns, columnClasses, tableName, connection);

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
                    fileUpdates(singleRecords, columns, columnClasses, tableName, connection);

                } catch (SQLException ex3) {
                    String s = "Failed to insert or update " + tableName + " record: ";
                    for (String column: columns) {
                        s += singleRecord.get(column) + ", ";
                    }

                    //we've got two exceptions in scope - the original insert exception and the update exception
                    //so we need to log both. Since only one can be wrapped in the exception we throw, we
                    //log the other one out here
                    LOG.error("", ex);

                    throw new Exception(s, ex3);
                }

            } else {
                //if we have multilple records, try inserting them one at a time
                for (CSVRecord csvRecord: csvRecords) {
                    List<CSVRecord> singleRecord = new ArrayList<>();
                    singleRecord.add(csvRecord);

                    fileUpserts(singleRecord, columns, columnClasses, tableName, connection);
                }
            }
        }
    }

    private static void fileInserts(List<CSVRecord> csvRecords, List<String> columns, HashMap<String, Class> columnClasses,
                                    String tableName, Connection connection) throws Exception {

        if (csvRecords.isEmpty()) {
            return;
        }

        int tableColumns = columns.size()-1; //substract one because we don't save the save_mode
        String parameters = createParameters(tableColumns);

        PreparedStatement insert = connection.prepareStatement("insert into " + tableName + " values (" + parameters + ")");

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

    private static void fileUpdates(List<CSVRecord> csvRecords, List<String> columns, HashMap<String, Class> columnClasses,
                                    String tableName, Connection connection) throws Exception {

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
        if (updateCounts[0] != csvRecords.size()) {
            throw new SQLException("Failed to update the right number of rows. Expected " + csvRecords.size() + " Got " + updateCounts[0]);
        }

        connection.commit();
    }

    /*public static void file(String base64) throws Exception {

        byte[] bytes = Base64.getDecoder().decode(base64);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ZipInputStream zis = new ZipInputStream(bais);

        try {
            while (true) {
                ZipEntry entry = zis.getNextEntry();
                if (entry == null) {
                    break;
                }

                String fileName = entry.getName();
                if (!fileName.equalsIgnoreCase(ZIP_ENTRY)) {
                    throw new Exception("Unexpected file name in zip content " + fileName);
                }

                byte[] buffer = new byte[2048];

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BufferedOutputStream bos = new BufferedOutputStream(baos, buffer.length);

                int len = 0;
                while ((len = zis.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                }

                bos.flush();
                bos.close();

                byte[] xmlBytes = baos.toByteArray();
                String xmlStr = new String(xmlBytes);
                EnterpriseData data = EnterpriseSerializer.readFromXml(xmlStr);
                fileEnterpriseData(data);
            }
        } finally {
            zis.close();
        }
    }

    private static void fileEnterpriseData(EnterpriseData data) throws Exception {

        Connection connection = openConnection();

        try {
            file(data.getOrganization(), connection);
            file(data.getPractitioner(), connection);
            file(data.getSchedule(), connection);
            file(data.getPatient(), connection);
            file(data.getEpisodeOfCare(), connection);
            file(data.getAppointment(), connection);
            file(data.getEncounter(), connection);
            //file(data.getCondition(), connection);
            //file(data.getProcedure(), connection);
            file(data.getReferralRequest(), connection);
            file(data.getProcedureRequest(), connection);
            file(data.getObservation(), connection);
            file(data.getMedicationStatement(), connection);
            file(data.getMedicationOrder(), connection);
            //file(data.getImmunization(), connection);
            //file(data.getFamilyMemberHistory(), connection);
            file(data.getAllergyIntolerance(), connection);
            //file(data.getDiagnosticOrder(), connection);
            //file(data.getDiagnosticReport(), connection);

        } catch (SQLException ex) {

            //SQL exceptions have a weird way of storing multiple exceptions, which
            //normal exception logging doesn't display, so we need to manually log those
            //exceptions here, before throwing the exception as normal
            SQLException next = ex;
            while (next != null) {
                LOG.error("", next);
                next = next.getNextException();
            }

            throw ex;

        } finally {
            connection.close();
        }

    }

    private static <T extends BaseRecord> void file(List<T> objects, Connection connection) throws Exception {

        if (objects == null || objects.isEmpty()) {
            return;
        }

        //separate into three lists, for inserts, updates and deletes
        List<T> upserts = new ArrayList<>();
        List<T> deletes = new ArrayList<>();

        for (T record: objects) {
            if (record.getSaveMode() == SaveMode.UPSERT) {
                upserts.add(record);
            } else if (record.getSaveMode() == SaveMode.DELETE) {
                deletes.add(record);
            } else {
                LOG.error("Unexpected save mode " + record.getSaveMode());
            }
        }

        fileUpserts(upserts, connection);
        fileDeletes(deletes, connection);
    }

    private static <T extends BaseRecord> void fileUpserts(List<T> objects, Connection connection) throws Exception {

        if (objects == null || objects.isEmpty()) {
            return;
        }

        //first try treating all the objects as inserts
        try {
            fileInserts(objects, connection);

        } catch (SQLException ex) {
            //if we get a SQL Exception, then it's probably because one of the objects is an update, so we try to
            //save each object separately first as an insert, then as an update
            connection.rollback();

            for (T obj: objects) {
                List<T> singleObject = new ArrayList<>();
                singleObject.add(obj);

                try {
                    fileInserts(singleObject, connection);

                } catch (SQLException ex2) {
                    //if the insert of this single row failed, then save it as an update
                    connection.rollback();
                    fileUpdates(singleObject, connection);
                }
            }
        }
    }

    private static Object getClassAnnotationValue(Class classType, Class annotationType, String attributeName) throws Exception {
        Annotation annotation = classType.getAnnotation(annotationType);
        return annotation.annotationType().getMethod(attributeName).invoke(annotation);
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

    private static void addToStatement(PreparedStatement statement, Field field, Object record, int index) throws Exception {

        Object value = field.get(record);
        Class fieldCls = field.getType();

        if (fieldCls == String.class) {

            if (value == null) {
                statement.setNull(index, Types.VARCHAR);
            } else {
                statement.setString(index, (String)value);
            }

        } else if (fieldCls == XMLGregorianCalendar.class) {
            if (value == null) {
                statement.setNull(index, Types.DATE);
            } else {
                XMLGregorianCalendar g = (XMLGregorianCalendar)value;
                long millis = g.toGregorianCalendar().getTimeInMillis();
                statement.setTimestamp(index, new Timestamp(millis));
            }

        } else if (fieldCls == BigDecimal.class) {
            if (value == null) {
                statement.setNull(index, Types.DECIMAL);
            } else {
                statement.setBigDecimal(index, (BigDecimal)value);
            }

        } else if (fieldCls == Integer.class) {
            if (value == null) {
                statement.setNull(index, Types.INTEGER);
            } else {
                statement.setInt(index, ((Integer)value).intValue());
            }

        } else if (fieldCls == Integer.TYPE) {
            if (value == null) {
                statement.setNull(index, Types.INTEGER);
            } else {
                statement.setInt(index, (int)value);
            }

        } else if (fieldCls == Long.class) {
            if (value == null) {
                statement.setNull(index, Types.INTEGER);
            } else {
                statement.setLong(index, ((Long)value).longValue());
            }

        } else if (fieldCls == Boolean.class) {
            if (value == null) {
                statement.setNull(index, Types.BOOLEAN);
            } else {
                statement.setBoolean(index, ((Boolean)value).booleanValue());
            }

        } else if (fieldCls == Boolean.TYPE) {
            if (value == null) {
                statement.setNull(index, Types.BOOLEAN);
            } else {
                statement.setBoolean(index, ((Boolean)value).booleanValue());
            }

        } else {
            throw new Exception("Unsupported value class " + fieldCls);
        }
    }

    private static <T extends BaseRecord> String getTableName(T record) throws Exception {
        Class cls = record.getClass();
        return (String)getClassAnnotationValue(cls, XmlType.class, "name");
    }

    private static <T extends BaseRecord> List<Field> getFields(T record) throws Exception {
        Class cls = record.getClass();
        String[] xmlSchemaProperties = (String[])getClassAnnotationValue(cls, XmlType.class, "propOrder");

        List<Field> fields = new ArrayList<>();
        for (int i=0; i<xmlSchemaProperties.length; i++) {
            String fieldName = xmlSchemaProperties[i];

            Field field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);
            fields.add(field);
        }

        return fields;
    }

    private static <T extends BaseRecord> void fileInserts(List<T> objects, Connection connection) throws Exception {

        if (objects == null || objects.isEmpty()) {
            return;
        }

        T first = objects.get(0);

        String tableName = getTableName(first);
        List<Field> fields = getFields(first);
        int tableColumns = fields.size()+1; //the ID is in the base class, so just add one
        String parameters = createParameters(tableColumns);

        PreparedStatement insert = connection.prepareStatement("insert into " + tableName + " values (" + parameters + ")");

        for (T record: objects) {

            //ID is always first and always a UUID
            insert.setInt(1, record.getId());

            for (int i=0; i<fields.size(); i++) {
                Field field = fields.get(i);
                addToStatement(insert, field, record, i+2);
            }

            insert.addBatch();
        }

        insert.executeBatch();

        connection.commit();
    }

    private static <T extends BaseRecord> void fileUpdates(List<T> objects, Connection connection) throws Exception {

        if (objects == null || objects.isEmpty()) {
            return;
        }

        T first = objects.get(0);

        String tableName = getTableName(first);
        List<Field> fields = getFields(first);

        StringBuilder sb = new StringBuilder();
        sb.append("update " + tableName + " set ");
        boolean addComma = false;
        for (Field field: fields) {

            String columnName = field.getName();

            //if there's an annotation, this will give the proper column name, if it differs from the field ane
            XmlElement annotation = field.getAnnotation(XmlElement.class);
            if (annotation != null) {
                columnName = annotation.name();
            }

            if (addComma) {
                sb.append(", ");
            }
            addComma = true;

            //the Appointment table has a column called "left" which is a keyword, so we may as well escape
            //all column names to prevent this error
            //mySQL uses different sscape chars, so move into config
            sb.append(keywordEscapeChar + columnName + keywordEscapeChar + " = ?");
            //sb.append("\"" + columnName + "\" = ?");
            //sb.append(columnName + " = ?");
        }
        sb.append(" where id = ?");

        PreparedStatement update = connection.prepareStatement(sb.toString());

        for (T record: objects) {

            for (int i=0; i<fields.size(); i++) {
                Field field = fields.get(i);
                addToStatement(update, field, record, i+1);
            }

            //the ID is the last parameter
            update.setInt(fields.size()+1, record.getId());

            update.addBatch();
        }

        update.executeBatch();

        connection.commit();
    }

    private static <T extends BaseRecord> void fileDeletes(List<T> objects, Connection connection) throws Exception {

        if (objects == null || objects.isEmpty()) {
            return;
        }

        T first = objects.get(0);

        String tableName = getTableName(first);

        PreparedStatement insert = connection.prepareStatement("delete from " + tableName + " where id = ?");

        for (T record: objects) {

            insert.setInt(1, record.getId());
            insert.addBatch();
        }

        insert.executeBatch();

        connection.commit();
    }*/

    private static Connection openConnection() throws Exception {

        JsonNode config = ConfigManager.getConfigurationAsJson("patient_database", "enterprise");

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
    }
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