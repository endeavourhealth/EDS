package org.endeavourhealth.subscriber;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.core.data.config.ConfigManager;
import org.endeavourhealth.core.xml.EnterpriseSerializer;
import org.endeavourhealth.core.xml.enterprise.BaseRecord;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class EnterpriseFiler {
    private static final Logger LOG = LoggerFactory.getLogger(EnterpriseFiler.class);

    private static final String ZIP_ENTRY = "EnterpriseData.xml";

    public static void file(String base64) throws Exception {

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
            file(data.getCondition(), connection);
            file(data.getProcedure(), connection);
            file(data.getReferralRequest(), connection);
            file(data.getProcedureRequest(), connection);
            file(data.getObservation(), connection);
            file(data.getMedicationStatement(), connection);
            file(data.getMedicationOrder(), connection);
            file(data.getImmunization(), connection);
            file(data.getFamilyMemberHistory(), connection);
            file(data.getAllergyIntolerance(), connection);
            file(data.getDiagnosticOrder(), connection);
            file(data.getDiagnosticReport(), connection);

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

            /*//UUIDs are also represented as Strings in the objects, so we need a way to know which fields should be treated
            //as Strings and which as UUIDs. Not ideal, but works for the schema.
            String fieldName = field.getName();
            if (fieldName.equals("id")
                    || fieldName.endsWith("Id")) {

                if (value == null) {
                    statement.setNull(index, Types.OTHER);
                } else {
                    statement.setObject(index, UUID.fromString((String)value));
                }

            } else {

                if (value == null) {
                    statement.setNull(index, Types.VARCHAR);
                } else {
                    statement.setString(index, (String)value);
                }
            }*/

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
            sb.append("\"" + columnName + "\" = ?");
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
    }

    private static Connection openConnection() throws Exception {

        //force the driver to be loaded
        Class.forName("org.postgresql.Driver");

        JsonNode config = ConfigManager.getConfigurationAsJson("postgres", "enterprise");
        String url = config.get("url").asText();
        String username = config.get("username").asText();
        String password = config.get("password").asText();

        Connection conn = DriverManager.getConnection(url, username, password);
        conn.setAutoCommit(false);

        return conn;
    }



    /*private static Connection openConnection() throws Exception {
        SubscriberConfiguration config = ConfigurationProvider.getInstance().getConfiguration();
        PostgreSQLConnection dbConfig = config.getPostgreSQLConnection();

        Class.forName("org.postgresql.Driver"); //load the driver

        String url = "jdbc:postgresql://" + dbConfig.getHostname() + ":" + dbConfig.getPort() + "/" + dbConfig.getDatabase();
        String user = dbConfig.getUsername();
        String pass = dbConfig.getPassword();

        Connection conn = DriverManager.getConnection(url, user, pass);
        conn.setAutoCommit(false);

        if (dbConfig.getSchema() != null) {
            conn.setSchema(dbConfig.getSchema());
        }

        return conn;
    }*/

}
