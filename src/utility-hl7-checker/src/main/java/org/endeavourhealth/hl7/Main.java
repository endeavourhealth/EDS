package org.endeavourhealth.hl7;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v23.segment.MRG;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import com.google.common.base.Strings;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.SlackHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static HikariDataSource connectionPool = null;
    private static HapiContext context;
    private static Parser parser;

    /**
     * utility to check the HL7 Receiver database and move any blocking messages to the Dead Letter Queue
     *
     * Parameters:
     * <db_connection_url> <driver_class> <db_username> <db_password>
     */
    public static void main(String[] args) throws Exception {

        ConfigManager.Initialize("Hl7Checker");

        if (args.length < 4) {
            LOG.error("Expecting four parameters:");
            LOG.error("<db_connection_url> <driver_class> <db_username> <db_password>");
            System.exit(0);
            return;
        }

        String url = args[0];
        String driverClass = args[1];
        String user = args[2];
        String pass = args[3];

        //optional fifth parameter puts it in read only mode
        boolean readOnly = false;
        if (args.length > 4) {
            readOnly = Boolean.parseBoolean(args[4]);
        }

        LOG.info("Starting HL7 Check on " + url);

        try {
            openConnectionPool(url, driverClass, user, pass);

            context = new DefaultHapiContext();
            context.setValidationContext(new NoValidation());

            parser = context.getGenericParser();

            String sql = "SELECT message_id, channel_id, inbound_message_type, inbound_payload, error_message, pid2 FROM log.message WHERE error_message is not null;";
            Connection connection = getConnection();
            ResultSet resultSet = executeQuery(connection, sql);

            try {
                while (resultSet.next()) {
                    int messageId = resultSet.getInt(1);
                    int channelId = resultSet.getInt(2);
                    String messageType = resultSet.getString(3);
                    String inboundPayload = resultSet.getString(4);
                    String errorMessage = resultSet.getString(5);
                    String localPatientId = resultSet.getString(6);

                    String ignoreReason = shouldIgnore(channelId, messageType, inboundPayload, errorMessage);
                    if (!Strings.isNullOrEmpty(ignoreReason)) {

                        if (readOnly) {
                            LOG.info("Would have moved message " + messageId + " to the DLQ but in read only mode");
                            continue;
                        }

                        //if we have a non-null reason, move to the DLQ
                        moveToDlq(messageId, ignoreReason);

                        //and notify Slack that we've done so
                        sendSlackMessage(channelId, messageId, ignoreReason, localPatientId);
                    }
                }

            } finally {
                resultSet.close();
                connection.close();
            }

        } catch (Exception ex) {
            LOG.error("", ex);
            //although the error may be related to Homerton, just send to one channel for simplicity
            SlackHelper.sendSlackMessage(SlackHelper.Channel.Hl7ReceiverAlertsBarts, "Exception in HL7 Checker", ex);
            System.exit(0);
        }

        LOG.info("Completed HL7 Check on " + url);
        System.exit(0);
    }

    private static void sendSlackMessage(int channelId, int messageId, String ignoreReason, String localPatientId) throws Exception {

        SlackHelper.Channel channel = null;
        if (channelId == 1) {
            channel = SlackHelper.Channel.Hl7ReceiverAlertsHomerton;

        } else if (channelId == 2) {
            channel = SlackHelper.Channel.Hl7ReceiverAlertsBarts;

        } else {
            throw new Exception("Unknown channel " + channelId);
        }

        SlackHelper.sendSlackMessage(channel, "HL7 Checker moved message ID " + messageId + " (PatientId=" + localPatientId + "):\r\n" + ignoreReason);
    }

    /*private static String findMessageSource(int channelId) throws Exception {

        String sql = "SELECT channel_name FROM configuration.channel WHERE channel_id = " + channelId + ";";
        Connection connection = getConnection();
        ResultSet rs = executeQuery(connection, sql);

        if (!rs.next()) {
            throw new Exception("Failed to find name for channel " + channelId);
        }

        String ret = rs.getString(1);

        rs.close();
        connection.close();

        return ret;
    }*/

    private static String shouldIgnore(int channelId, String messageType, String inboundPayload, String errorMessage) throws HL7Exception {
        LOG.info("Checking auto-DLQ rules");
        LOG.info("channelId:" + channelId);
        LOG.info("messageType:" + messageType);
        LOG.info("errorMessage:" + errorMessage);
        LOG.info("inboundPayload:" + inboundPayload);

        // *************************************************************************************************************************************************
        // Rules for Homerton
        // *************************************************************************************************************************************************
        if (channelId == 1
            && messageType.equals("ADT^A44")
            && errorMessage.equals("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[java.lang.NullPointerException]  episodeIdentifierValue")) {

            Message hapiMsg = parser.parse(inboundPayload);
            Terser terser = new Terser(hapiMsg);

            String mergeEpisodeId = terser.get("/PATIENT/MRG-5");
            LOG.info("mergeEpisodeId:" + mergeEpisodeId);

            // If merge episodeId is missing then move to DLQ
            if (Strings.isNullOrEmpty(mergeEpisodeId)) {
                return "Automatically moved A44 because of missing episode ID";
            }

            String mergeEpisodeIdSource = terser.get("/PATIENT/MRG-5-4");
            LOG.info("mergeEpisodeIdSource:" + mergeEpisodeIdSource);

            // If merge episodeId is from Newham then move to DLQ
            if (mergeEpisodeIdSource != null && mergeEpisodeIdSource.toUpperCase().startsWith("NEWHAM")) {
                return "Automatically moved A44 because of merging Newham episode";
            }
        }


        if (channelId == 1
                && messageType.equals("ADT^A44")
                && errorMessage.equals("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[java.lang.NullPointerException]  patientIdentifierValue")) {

            Message hapiMsg = parser.parse(inboundPayload);
            Terser terser = new Terser(hapiMsg);

            String mergePatientId = terser.get("/PATIENT/MRG-1");
            LOG.info("mergePatientId:" + mergePatientId);

            // If merge patientId is missing then move to DLQ
            if (Strings.isNullOrEmpty(mergePatientId)) {
                return "Automatically moved A44 because of missing MRG patientId ID";
            }
        }

        // Added 2017-10-24
        if (channelId == 1
                && (messageType.startsWith("ADT^"))
                && errorMessage.equals("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[java.lang.IllegalArgumentException]  episodeIdentifierValue")) {

            Message hapiMsg = parser.parse(inboundPayload);
            Terser terser = new Terser(hapiMsg);

            String episodeId = terser.get("/PV1-19");
            LOG.info("episodeId:" + episodeId);

            // If episode id / encounter id is missing then move to DLQ
            if (Strings.isNullOrEmpty(episodeId)) {
                return "Automatically moved ADT because of missing PV1:19";
            }
        }

        // Added 2017-11-08
        if (channelId == 1
                && messageType.equals("ADT^A34")
                && errorMessage.equals("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[org.endeavourhealth.hl7transform.common.TransformException]  MRG segment exists less than 1 time(s)")) {

            LOG.info("Looking for MRG segment");
            Message hapiMsg = parser.parse(inboundPayload);
            MRG mrg = (MRG) hapiMsg.get("MRG");

            // If MRG missing
            if (mrg == null || mrg.isEmpty()) {
                return "Automatically moved A34 because of missing MRG";
            } else {
                LOG.info("MRG segment found. isEmpty()=" + mrg.isEmpty());
            }
        }

        // Added 2017-11-10
        if (channelId == 1
                && messageType.equals("ADT^A35")
                && errorMessage.equals("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[org.endeavourhealth.hl7transform.common.TransformException]  MRG segment exists less than 1 time(s)")) {

            LOG.info("Looking for MRG segment");
            Message hapiMsg = parser.parse(inboundPayload);
            MRG mrg = (MRG) hapiMsg.get("MRG");

            // If MRG missing
            if (mrg == null || mrg.isEmpty()) {
                return "Automatically moved A35 because of missing MRG";
            } else {
                LOG.info("MRG segment found. isEmpty()=" + mrg.isEmpty());
            }
        }

        // *************************************************************************************************************************************************
        // Rules for Barts
        // *************************************************************************************************************************************************
        if (channelId == 2
            && messageType.equals("ADT^A31")
            && errorMessage.startsWith("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[org.endeavourhealth.hl7transform.common.TransformException]  Could not create organisation ")) {

            Message hapiMsg = parser.parse(inboundPayload);
            Terser terser = new Terser(hapiMsg);
            String gpPracticeId = terser.get("/PD1-3-3");
            LOG.info("Practice:" + gpPracticeId);

            // If practice id is missing or numeric then move to DLQ
            if (Strings.isNullOrEmpty(gpPracticeId)
                    || StringUtils.isNumeric(gpPracticeId)) {
                return "Automatically moved A31 because of invalid practice code";
            }
        }

        // Added 2017-11-07
        if (channelId == 2
                && messageType.startsWith("ADT^")
                && errorMessage.equals("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[java.lang.NullPointerException]  episodeIdentifierValue")) {

            Message hapiMsg = parser.parse(inboundPayload);
            Terser terser = new Terser(hapiMsg);

            String episodeId = terser.get("/PV1-19");
            LOG.info("episodeId:" + episodeId);

            // If episode id / encounter id is missing then move to DLQ
            if (Strings.isNullOrEmpty(episodeId)) {
                return "Automatically moved ADT because of missing PV1:19";
            }
        }

        // Added 2017-11-08
        if (channelId == 2
                && messageType.equals("ADT^A34")
                && errorMessage.equals("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[org.endeavourhealth.hl7transform.common.TransformException]  MRG segment exists less than 1 time(s)")) {

            LOG.info("Looking for MRG segment");
            Message hapiMsg = parser.parse(inboundPayload);
            MRG mrg = (MRG) hapiMsg.get("MRG");

            // If MRG missing
            if (mrg == null || mrg.isEmpty()) {
                return "Automatically moved A34 because of missing MRG";
            } else {
                LOG.info("MRG segment found. isEmpty()=" + mrg.isEmpty());
            }
        }

        // Added 2017-11-08
        if (channelId == 2
                && messageType.equals("ADT^A35")
                && errorMessage.equals("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[org.endeavourhealth.hl7transform.common.TransformException]  MRG segment exists less than 1 time(s)")) {

            LOG.info("Looking for MRG segment");
            Message hapiMsg = parser.parse(inboundPayload);
            MRG mrg = (MRG) hapiMsg.get("MRG");

            // If MRG missing
            if (mrg == null || mrg.isEmpty()) {
                return "Automatically moved A35 because of missing MRG";
            } else {
                LOG.info("MRG segment found. isEmpty()=" + mrg.isEmpty());
            }
        }

        //return null to indicate we don't ignore it
        return null;
    }

    /*
     *
     */
    private static void moveToDlq(int messageId, String reason) throws Exception {

        //although it looks like a select, it's just invoking a function which performs an update
        String sql = "SELECT helper.move_message_to_dead_letter(" + messageId + ", '" + reason + "');";
        executeUpdate(sql);

        sql = "UPDATE log.message"
                + " SET next_attempt_date = now() - interval '1 hour'"
                + " WHERE message_id = " + messageId + ";";
        executeUpdate(sql);

    }

    /*
     *
     */
    private static void openConnectionPool(String url, String driverClass, String username, String password) throws Exception {

        //force the driver to be loaded
        Class.forName(driverClass);

        HikariDataSource pool = new HikariDataSource();
        pool.setJdbcUrl(url);
        pool.setUsername(username);
        pool.setPassword(password);
        pool.setMaximumPoolSize(4);
        pool.setMinimumIdle(1);
        pool.setIdleTimeout(60000);
        pool.setPoolName("Hl7CheckerPool" + url);
        pool.setAutoCommit(false);

        connectionPool = pool;

        //test getting a connection
        Connection conn = pool.getConnection();
        conn.close();
    }

    private static Connection getConnection() throws Exception {
        return connectionPool.getConnection();
    }

    private static void executeUpdate(String sql) throws Exception {

        Connection connection = getConnection();

        try {
            Statement statement = connection.createStatement();
            statement.execute(sql);
            connection.commit();
        } finally {
            connection.close();
        }
    }

    private static ResultSet executeQuery(Connection connection, String sql) throws Exception {

        Statement statement = connection.createStatement();
        return statement.executeQuery(sql);
    }
}
