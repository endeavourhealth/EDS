package org.endeavourhealth.hl7;

import com.google.common.base.Strings;
import com.zaxxer.hikari.HikariDataSource;
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

    /**
     * utility to check the HL7 Receiver database and move any blocking messages to the Dead Letter Queue
     *
     * Parameters:
     * <db_connection_url> <driver_class> <db_username> <db_password>
     */
    public static void main(String[] args) throws Exception {

        ConfigManager.Initialize("Hl7Checker");

        if (args.length != 4) {
            LOG.error("Expecting four parameters:");
            LOG.error("<db_connection_url> <driver_class> <db_username> <db_password>");
            System.exit(0);
            return;
        }

        String url = args[0];
        String driverClass = args[1];
        String user = args[2];
        String pass = args[3];

        LOG.info("Starting HL7 Check on " + url);

        try {
            openConnectionPool(url, driverClass, user, pass);

            String sql = "SELECT message_id, channel_id, inbound_message_type, inbound_payload, error_message FROM log.message WHERE error_message is not null;";
            Connection connection = getConnection();
            ResultSet resultSet = executeQuery(connection, sql);

            try {
                while (resultSet.next()) {
                    int messageId = resultSet.getInt(1);
                    int channelId = resultSet.getInt(2);
                    String messageType = resultSet.getString(3);
                    String inboundPayload = resultSet.getString(4);
                    String errorMessage = resultSet.getString(5);

                    String ignoreReason = shouldIgnore(channelId, messageType, inboundPayload, errorMessage);
                    if (!Strings.isNullOrEmpty(ignoreReason)) {
                        //if we have a non-null reason, move to the DLQ
                        moveToDlq(messageId, ignoreReason);

                        //and notify Slack that we've done so
                        sendSlackMessage(channelId, messageId, ignoreReason);
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

    private static void sendSlackMessage(int channelId, int messageId, String ignoreReason) throws Exception {

        SlackHelper.Channel channel = null;
        if (channelId == 1) {
            channel = SlackHelper.Channel.Hl7ReceiverAlertsHomerton;

        } else if (channelId == 2) {
            channel = SlackHelper.Channel.Hl7ReceiverAlertsBarts;

        } else {
            throw new Exception("Unknown channel " + channelId);
        }

        SlackHelper.sendSlackMessage(channel, "HL7 Checker moved message ID " + messageId + ":\r\n" + ignoreReason);
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

    private static String shouldIgnore(int channelId, String messageType, String inboundPayload, String errorMessage) {

        if (channelId == 1
            && messageType.equals("ADT^A44")
            && errorMessage.equals("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[java.lang.NullPointerException]  episodeIdentifierValue")) {

            //Peter, add extra validation here if required

            return "Automatically moved A44 because of missing episode ID";
        }

        if (channelId == 2
            && messageType.equals("ADT^A31")
            && errorMessage.startsWith("[org.endeavourhealth.hl7receiver.model.exceptions.HL7MessageProcessorException]  Transform failure\r\n[org.endeavourhealth.hl7transform.common.TransformException]  Could not create organisation ")) {

            //Peter, add extra validation here if required

            return "Automatically moved A31 because of invalid practice code";
        }

        //return null to indicate we don't ignore it
        return null;
    }

    private static void moveToDlq(int messageId, String reason) throws Exception {

        //not sure if this should be treated as a select or update, since it actually does perform updates. Probably need to test???
        Connection connection = getConnection();
        String sql = "SELECT * FROM helper.move_message_to_dead_letter(" + messageId + ", '" + reason + "');";
        executeQuery(connection, sql);
        connection.close();

        sql = "UPDATE log.message"
                + " SELECT next_attempt_date = now() - interval '1 hour'"
                + " WHERE message_id = " + messageId + ";";
        executeUpdate(sql);

    }

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
