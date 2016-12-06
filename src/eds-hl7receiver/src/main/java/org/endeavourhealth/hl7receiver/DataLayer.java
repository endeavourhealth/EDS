package org.endeavourhealth.hl7receiver;

import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.endeavourhealth.hl7receiver.model.db.DbConfiguration;
import org.endeavourhealth.hl7receiver.model.db.DbInstance;
import org.endeavourhealth.utilities.postgres.PgStoredProc;
import org.endeavourhealth.utilities.postgres.PgStoredProcException;

import javax.sql.DataSource;
import java.util.List;

public class DataLayer {

    private DataSource dataSource;

    public DataLayer(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public DbConfiguration getConfiguration(String instanceName) throws PgStoredProcException {

        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("configuration.get_configuration")
                .addParameter("_instance_name", instanceName);

        DbInstance dbInstance = pgStoredProc.executeMultiQuerySingleRow((resultSet) ->
                new DbInstance()
                        .setInstanceId(resultSet.getString("instance_id"))
                        .setInstanceName(resultSet.getString("instance_name"))
                        .setInstanceDescription(resultSet.getString("description")));

        List<DbChannel> dbChannels = pgStoredProc.executeMultiQuery((resultSet) ->
                new DbChannel()
                        .setChannelId(resultSet.getInt("channel_id"))
                        .setChannelName(resultSet.getString("channel_name"))
                        .setPortNumber(resultSet.getInt("port_number"))
                        .setUseTls(resultSet.getBoolean("use_tls"))
                        .setRemoteApplication(resultSet.getString("remote_application"))
                        .setRemoteFacility(resultSet.getString("remote_facility"))
                        .setLocalApplication(resultSet.getString("local_application"))
                        .setLocalFacility(resultSet.getString("local_facility"))
                        .setUseAcks(resultSet.getBoolean("use_acks"))
                        .setNotes(resultSet.getString("notes")));

        return new DbConfiguration()
                .setDbInstance(dbInstance)
                .setDbChannels(dbChannels);
    }

    public int openConnection(String instanceName, String channelName, int localPort, String remoteHost, int remotePort) throws PgStoredProcException {

        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("log.open_connection")
                .addParameter("_instance_name", instanceName)
                .addParameter("_channel_name", channelName)
                .addParameter("_local_port", localPort)
                .addParameter("_remote_host", remoteHost)
                .addParameter("_remote_port", remotePort);

        return pgStoredProc.executeSingleRow((resultSet) -> resultSet.getInt("open_connection"));
    }

    public void closeConnection(int connectionId) throws PgStoredProcException {

        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("log.close_connection")
                .addParameter("_connection_id", connectionId);

        pgStoredProc.execute();
    }

    public int logMessage(int channelId, int connectionId, String inboundPayload) throws PgStoredProcException {

        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("log.log_message")
                .addParameter("_channel_id", channelId)
                .addParameter("_connection_id", connectionId)
                .addParameter("_inbound_payload", inboundPayload);

        return pgStoredProc.executeSingleRow((resultSet) -> resultSet.getInt("log_message"));
    }
}
