package org.endeavourhealth.hl7receiver;

import org.endeavourhealth.core.postgres.PgStoredProc;
import org.endeavourhealth.core.postgres.PgStoredProcException;
import org.endeavourhealth.hl7receiver.model.db.*;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DataLayer {

    private DataSource dataSource;

    public DataLayer(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public DbConfiguration getConfiguration(String hostname) throws PgStoredProcException {

        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("configuration.get_configuration")
                .addParameter("_hostname", hostname);

        DbConfiguration dbConfiguration = pgStoredProc.executeMultiQuerySingleRow((resultSet) ->
                new DbConfiguration()
                        .setInstanceId(resultSet.getInt("instance_id")));

        List<DbChannel> dbChannels = pgStoredProc.executeMultiQuery((resultSet) ->
                new DbChannel()
                        .setChannelId(resultSet.getInt("channel_id"))
                        .setChannelName(resultSet.getString("channel_name"))
                        .setPortNumber(resultSet.getInt("port_number"))
                        .setActive(resultSet.getBoolean("is_active"))
                        .setUseTls(resultSet.getBoolean("use_tls"))
                        .setSendingApplication(resultSet.getString("sending_application"))
                        .setSendingFacility(resultSet.getString("sending_facility"))
                        .setReceivingApplication(resultSet.getString("receiving_application"))
                        .setReceivingFacility(resultSet.getString("receiving_facility"))
                        .setNotes(resultSet.getString("notes")));

        List<DbChannelMessageType> dbChannelMessageTypes = pgStoredProc.executeMultiQuery((resultSet) ->
                new DbChannelMessageType()
                        .setChannelId(resultSet.getInt("channel_id"))
                        .setMessageType(resultSet.getString("message_type"))
                        .setAllowed(resultSet.getBoolean("is_allowed")));

        for (DbChannel dbChannel : dbChannels) {
            dbChannel.setDbChannelMessageTypes(
                    dbChannelMessageTypes
                            .stream()
                            .filter(t -> t.getChannelId() == dbChannel.getChannelId())
                            .collect(Collectors.toList()));
        }

        return dbConfiguration
                .setDbChannels(dbChannels);
    }

    public int openConnection(int instanceId, int channelId, int localPort, String remoteHost, int remotePort) throws PgStoredProcException {

        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("log.open_connection")
                .addParameter("_instance_id", instanceId)
                .addParameter("_channel_id", channelId)
                .addParameter("_local_port", localPort)
                .addParameter("_remote_host", remoteHost)
                .addParameter("_remote_port", remotePort);

        return pgStoredProc.executeSingleRow((resultSet) -> resultSet.getInt("connection_id"));
    }

    public void closeConnection(int connectionId) throws PgStoredProcException {

        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("log.close_connection")
                .addParameter("_connection_id", connectionId);

        pgStoredProc.execute();
    }

    public int logMessage(
            int channelId,
            int connectionId,
            String messageControlId,
            String inboundMessageType,
            String inboundPayload,
            String outboundMessageType,
            String outboundPayload) throws PgStoredProcException {

        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("log.log_message")
                .addParameter("_channel_id", channelId)
                .addParameter("_connection_id", connectionId)
                .addParameter("_message_control_id", messageControlId)
                .addParameter("_inbound_message_type", inboundMessageType)
                .addParameter("_inbound_payload", inboundPayload)
                .addParameter("_outbound_message_type", outboundMessageType)
                .addParameter("_outbound_payload", outboundPayload);

        return pgStoredProc.executeSingleRow((resultSet) -> resultSet.getInt("log_message"));
    }

    public DbErrorIdentifier logError(String exception, String method, String message) throws PgStoredProcException {

        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("log.log_error")
                .addParameter("_exception", exception)
                .addParameter("_method", method)
                .addParameter("_message", message);

        return pgStoredProc.executeSingleRow((resultSet) ->
                new DbErrorIdentifier()
                        .setErrorId(resultSet.getInt("error_id"))
                        .setErrorUuid(UUID.fromString(resultSet.getString("error_uuid"))));
    }

    public int logDeadLetter(
            Integer instanceId,
            Integer channelId,
            Integer connectionId,
            String localHost,
            Integer localPort,
            String remoteHost,
            Integer remotePort,
            String sendingApplication,
            String sendingFacility,
            String receivingApplication,
            String receivingFacility,
            String messageControlId,
            String inboundMessageType,
            String inboundPayload,
            String outboundMessageType,
            String outboundPayload) throws PgStoredProcException {

        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("log.log_dead_letter")
                .addParameter("_instance_id", instanceId)
                .addParameter("_channel_id", channelId)
                .addParameter("_connection_id", connectionId)
                .addParameter("_local_host", localHost)
                .addParameter("_local_port", localPort)
                .addParameter("_remote_host", remoteHost)
                .addParameter("_remote_port", remotePort)
                .addParameter("_sending_application", sendingApplication)
                .addParameter("_sending_facility", sendingFacility)
                .addParameter("_receiving_application", receivingApplication)
                .addParameter("_receiving_facility", receivingFacility)
                .addParameter("_message_control_id", messageControlId)
                .addParameter("_inbound_message_type", inboundMessageType)
                .addParameter("_inbound_payload", inboundPayload)
                .addParameter("_outbound_message_type", outboundMessageType)
                .addParameter("_outbound_payload", outboundPayload);

        return pgStoredProc.executeSingleRow((resultSet) -> resultSet.getInt("log_dead_letter"));
    }
}
