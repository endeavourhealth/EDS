package org.endeavourhealth.subscriber;

import org.endeavourhealth.core.xml.EnterpriseSerializer;
import org.endeavourhealth.core.xml.enterprise.*;
import org.endeavourhealth.subscriber.configuration.ConfigurationProvider;
import org.endeavourhealth.subscriber.configuration.models.PostgreSQLConnection;
import org.endeavourhealth.subscriber.configuration.models.SubscriberConfiguration;
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
import java.util.UUID;
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
                EnterpriseData data = EnterpriseSerializer.readFromXml(new String(xmlBytes));
                fileEnterpriseData(data);
            }
        } finally {
            zis.close();
        }
    }

    private static void fileEnterpriseData(EnterpriseData data) throws Exception {

        Connection connection = openConnection();

        try {
            file(data.getOrganisation(), connection);
            file(data.getPractitioner(), connection);
            file(data.getSchedule(), connection);
            file(data.getPatient(), connection);
            file(data.getAppointment(), connection);
            file(data.getEncounter(), connection);
            file(data.getCondition(), connection);
            file(data.getProcedure(), connection);
            file(data.getReferralRequest(), connection);
            file(data.getProcedureRequest(), connection);
            file(data.getObservation(), connection);
            file(data.getMedicationStatement(), connection);
            file(data.getMedicationOrder(), connection);
            file(data.getImmunisation(), connection);
            file(data.getFamilyMemberHistory(), connection);
            file(data.getAllergyIntolerance(), connection);
            file(data.getDiagnosticOrder(), connection);

        } catch (SQLException ex) {

            while (ex != null) {
                LOG.error("", ex);
                ex = ex.getNextException();
            }
            LOG.error("----------------------------------------------");

        } finally {
            connection.close();
        }

    }

    private static <T extends BaseRecord> void file(List<T> objects, Connection connection) throws Exception {

        if (objects == null || objects.isEmpty()) {
            return;
        }

        //separate into three lists, for inserts, updates and deletes
        List<T> inserts = new ArrayList<>();
        List<T> updates = new ArrayList<>();
        List<T> deletes = new ArrayList<>();

        for (T record: objects) {
            if (record.getSaveMode() == SaveMode.INSERT) {
                inserts.add(record);
            } else if (record.getSaveMode() == SaveMode.UPDATE) {
                updates.add(record);
            } else if (record.getSaveMode() == SaveMode.DELETE) {
                deletes.add(record);
            } else {
                LOG.error("Unexpected save mode " + record.getSaveMode());
            }
        }

        fileInserts(inserts, connection);
        fileUpdates(updates, connection);
        fileDeletes(deletes, connection);
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

            //UUIDs are also represented as Strings in the objects, so we need a way to know which fields should be treated
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

        } else if (fieldCls == Long.class) {
            if (value == null) {
                statement.setNull(index, Types.INTEGER);
            } else {
                statement.setLong(index, ((Long)value).longValue());
            }

        } else if (fieldCls == Gender.class) {
            if (value == null) {
                statement.setNull(index, Types.VARCHAR);
            } else {
                statement.setString(index, ((Gender)value).value());
            }

        } else if (fieldCls == DatePrecision.class) {
            if (value == null) {
                statement.setNull(index, Types.VARCHAR);
            } else {
                statement.setString(index, ((DatePrecision)value).value());
            }

        } else if (fieldCls == ProcedureRequestStatus.class) {
            if (value == null) {
                statement.setNull(index, Types.VARCHAR);
            } else {
                statement.setString(index, ((ProcedureRequestStatus)value).value());
            }

        } else if (fieldCls == MedicationStatementStatus.class) {
            if (value == null) {
                statement.setNull(index, Types.VARCHAR);
            } else {
                statement.setString(index, ((MedicationStatementStatus)value).value());
            }

        } else if (fieldCls == AppointmentStatus.class) {
            if (value == null) {
                statement.setNull(index, Types.VARCHAR);
            } else {
                statement.setString(index, ((AppointmentStatus)value).value());
            }

        } else if (fieldCls == MedicationStatementAuthorisationType.class) {
            if (value == null) {
                statement.setNull(index, Types.VARCHAR);
            } else {
                statement.setString(index, ((MedicationStatementAuthorisationType)value).value());
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
            insert.setObject(1, UUID.fromString(record.getId()));

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

            sb.append(columnName + " = ?");
        }
        sb.append(" where id = ?");

        PreparedStatement insert = connection.prepareStatement(sb.toString());

        for (T record: objects) {

            for (int i=0; i<fields.size(); i++) {
                Field field = fields.get(i);
                addToStatement(insert, field, record, i+1);
            }

            //the ID is the last parameter
            insert.setObject(fields.size()+1, UUID.fromString(record.getId()));

            insert.addBatch();
        }

        insert.executeBatch();

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

            insert.setObject(1, UUID.fromString(record.getId()));
            insert.addBatch();
        }

        insert.executeBatch();

        connection.commit();
    }

    private static Connection openConnection() throws Exception {
        SubscriberConfiguration config = ConfigurationProvider.getInstance().getConfiguration();
        PostgreSQLConnection dbConfig = config.getPostgreSQLConnection();

        Class.forName("org.postgresql.Driver"); //load the driver

        String url = "jdbc:postgresql://" + dbConfig.getHostname() + ":" + dbConfig.getPort() + "/" + dbConfig.getDatabase();
        String user = dbConfig.getUsername();
        String pass = dbConfig.getPassword();

        Connection conn = DriverManager.getConnection(url, user, pass);
        conn.setAutoCommit(false);
        return conn;
    }

}
