package org.endeavourhealth.querytool;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.FileHelper;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.ScheduledTaskAuditDalI;
import org.endeavourhealth.core.database.dal.audit.SimplePropertyDalI;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final String PROPERTY_LAST_RUN = "LastRun";
    private static final String KEYWORD_LAST_RUN = "<last_run>";

    /**
     * utility to execute a stored procedure and email the results out, used for
     * internal schedule tasks (e.g. checking Emis missing codes or getting DDS stats)

     * Usage
     * =================================================================================
     * <query config name> - config name that defines the SP to run and where to send the results
     */
    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            LOG.error("Parameter required: <query config name>");
            return;
        }

        String queryName = args[0];
        ConfigManager.initialize("query-tool", queryName);

        try {
            Date dtRun = new Date();
            Date dtLastRun = findDateTimeLastRun();
            LOG.debug("Last run " + dtLastRun);

            runQuery(queryName, dtLastRun);
            saveDateTimeLastRun(dtRun);
            auditSuccess(queryName, args);

        } catch (Throwable t) {

            LOG.error("Error with scheduled query " + queryName, t);
            SlackHelper.sendSlackMessage(SlackHelper.Channel.QueryTool, "Error running Scheduled Query Tool query [" + queryName + "]", t);
            auditFailure(queryName, args, t);
        }
    }

    private static Date findDateTimeLastRun() throws Exception {
        SimplePropertyDalI dal = DalProvider.factorySimplePropertyDal();
        return dal.getPropertyDate(PROPERTY_LAST_RUN);
    }

    private static void saveDateTimeLastRun(Date dtRun) throws Exception {
        SimplePropertyDalI dal = DalProvider.factorySimplePropertyDal();
        dal.savePropertyDate(PROPERTY_LAST_RUN, dtRun);
    }

    private static void auditSuccess(String queryName, String[] args) throws Exception {
        ScheduledTaskAuditDalI dal = DalProvider.factoryScheduledTaskAuditDal();
        dal.auditTaskSuccess(queryName, args);
    }

    private static void auditFailure(String queryName, String[] args, Throwable t) throws Exception {
        ScheduledTaskAuditDalI dal = DalProvider.factoryScheduledTaskAuditDal();
        dal.auditTaskFailure(queryName, args, t);
    }

    private static void runQuery(String queryName, Date dtLastRun) throws Exception {
        String queryJson = ConfigManager.getConfiguration(queryName);
        if (Strings.isNullOrEmpty(queryJson)) {
            throw new Exception("Missing config JSON for query");
        }
        QueryDefinition query = ObjectMapperPool.getInstance().readValue(queryJson, QueryDefinition.class);

        Connection connection = getConnection(query);
        try {
            List<File> files = executeQueries(connection, query, dtLastRun);

            sendEmail(query, files);

            //tidy up
            deleteFiles(files);

        } finally {
            connection.close();
        }
    }


    private static void sendEmail(QueryDefinition query, List<File> files) throws Exception {

        LOG.debug("Sending email");

        String subject = query.getEmailSubject();
        if (Strings.isNullOrEmpty(subject)) {
            throw new Exception("No subject found for email");
        }
        String bodyText = query.getEmailBody();
        if (Strings.isNullOrEmpty(bodyText)) {
            throw new Exception("No body found for email");
        }
        List<String> recipients = query.getEmailRecipients();
        if (recipients == null || recipients.isEmpty()) {
            throw new Exception("No recipients found for email");
        }
        //apparently the parsing expects commas
        //String recipientStr = String.join("; ", recipients);
        String recipientStr = String.join(",", recipients);

        JsonNode json = ConfigManager.getConfigurationAsJson("email");
        if (json == null) {
            throw new Exception("No config record found for email");
        }
        String fromAddress = json.get("sendFromAddress").asText();
        String smtpHost = json.get("smtpHost").asText();
        int smtpPort = json.get("smtpPort").asInt();
        String password = json.get("password").asText();
        boolean debug = json.has("debug") && json.get("debug").asBoolean();

        // Creates a Session with the following properties.
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.host", smtpHost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "" + smtpPort);
        props.put("mail.debug", "" + debug);
        props.put("mail.smtp.socketFactory.port", "" + smtpPort);
        props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");

        Authenticator auth = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromAddress, password);
			}
		};
		Session session = Session.getInstance(props, auth);
        //Session session = Session.getDefaultInstance(props);

        // Create an Internet mail msg.
        MimeMessage msg = new MimeMessage(session);
        msg.setSender(new InternetAddress(fromAddress));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientStr));
        msg.setSubject(subject);
        msg.setContent(msg, "text/plain");
        msg.setSentDate(new Date());

        // Create Multipart E-Mail.
        Multipart multipart = new MimeMultipart();

        // Set the email msg text.
        MimeBodyPart messagePart = new MimeBodyPart();
        if (!Strings.isNullOrEmpty(bodyText)) {
            messagePart.setText(bodyText);
        }
        multipart.addBodyPart(messagePart);

        // Set the email attachment file
        for (File f: files) {
            FileDataSource fileDataSource = new FileDataSource(f);

            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.setDataHandler(new DataHandler(fileDataSource));
            attachmentPart.setFileName(fileDataSource.getName());

            multipart.addBodyPart(attachmentPart);
        }

        msg.setContent(multipart);

        // to authenticate to the mail server.
        Transport.send(msg, fromAddress, password);
        LOG.debug("Email sent");
    }


    private static void deleteFiles(List<File> files) {
        for (File f: files) {
            f.delete();
        }
    }

    private static List<File> executeQueries(Connection connection, QueryDefinition queryDefinition, Date dtLastRun) throws Exception {

        List<Query> queries = queryDefinition.getQueries();
        if (queries == null || queries.isEmpty()) {
            throw new Exception("Null or empty query list");
        }

        List<File> ret = new ArrayList<>();

        for (Query query: queries) {

            //simple validation to stop stupid mistakes
            String storedProc = query.getStoredProcedure();
            if (Strings.isNullOrEmpty(storedProc)) {
                throw new Exception("Null or empty stored procedure name");
            }

            String lower = storedProc.toLowerCase();
            if (lower.contains("update")
                    || lower.contains("delete")
                    || lower.contains("truncate")
                    || lower.contains("insert")) {
                throw new Exception("Query not permitted [" + storedProc + "]");
            }

            String outputFileName = query.getOutputFileName();
            if (Strings.isNullOrEmpty(outputFileName)) {
                throw new Exception("Null or empty output file name element");
            }

            try {
                File tempDir = FileHelper.getTempDir();
                File dstFile = new File(tempDir, outputFileName);
                String recordSeparator = "\r\n"; //need to explicitly set this since we'll be running in Ubuntu
                boolean appendToFile = false;

                LOG.debug("Running " + storedProc);
                long msStart = System.currentTimeMillis();

                CallableStatement statement = null;

                //substitute the date last run if the keyword is present
                if (storedProc.contains(KEYWORD_LAST_RUN)) {
                    storedProc = storedProc.replace(KEYWORD_LAST_RUN, "?");

                    statement = connection.prepareCall(storedProc);
                    if (dtLastRun == null) {
                        statement.setNull(1, Types.TIMESTAMP);
                    } else {
                        statement.setTimestamp(1, new java.sql.Timestamp(dtLastRun.getTime()));
                    }

                } else {
                    statement = connection.prepareCall(storedProc);
                }

                statement.execute();
                while (true) {
                    ResultSet rs = statement.getResultSet();
                    ResultSetMetaData rsmd = rs.getMetaData();

                    /*int cols = rsmd.getColumnCount();
                    String[] headers = new String[cols];
                    for (int i=0; i<cols; i++) {
                        String colName = rsmd.getColumnName(i);
                        headers[i] = colName;
                    }*/

                    FileOutputStream fos = new FileOutputStream(dstFile, appendToFile);
                    OutputStreamWriter osw = new OutputStreamWriter(fos);
                    BufferedWriter bufferedWriter = new BufferedWriter(osw);

                    //if the first result set, just flip to say we're not
                    if (!appendToFile) {
                        appendToFile = true;
                    } else {
                        //if a subsequent result set, just add some newlines
                        bufferedWriter.write(recordSeparator);
                        bufferedWriter.write(recordSeparator);
                    }

                    CSVFormat format = CSVFormat.DEFAULT
                            .withHeader(rsmd)
                            .withQuoteMode(QuoteMode.MINIMAL)
                            .withRecordSeparator(recordSeparator);
                    CSVPrinter printer = new CSVPrinter(bufferedWriter, format);

                    //CSV printer supports writing an entire result set
                    printer.printRecords(rs);

                    printer.close();

                    if (!statement.getMoreResults()) {
                        break;
                    }
                }

                statement.close();

                ret.add(dstFile);

                long msEnd = System.currentTimeMillis();
                LOG.debug("Ran " + storedProc + " in " + (msEnd - msStart) + "ms to " + dstFile);

            } catch (Exception ex) {
                throw new Exception("Exception with query " + storedProc, ex);
            }
        }

        return ret;
    }

    private static Connection getConnection(QueryDefinition query) throws Exception {

        String configName = query.getDatabaseConfigName();
        String instanceName = query.getDatabaseInstanceName(); //this can be null for single-instance DBs (e.g. audit)
        if (Strings.isNullOrEmpty(configName)) {
            throw new Exception("No database config name specified");
        }

        for (ConnectionManager.Db db: ConnectionManager.Db.values()) {
            if (db.getConfigName().equals(configName)) {
                LOG.debug("Opening connection to " + db + " (instance " + instanceName + ")");
                return ConnectionManager.getConnectionNonPooled(db, instanceName);
            }
        }

        //if we get here, we failed to find a match
        throw new Exception("Failed to find database enum value for [" + configName + "]");
    }

}
