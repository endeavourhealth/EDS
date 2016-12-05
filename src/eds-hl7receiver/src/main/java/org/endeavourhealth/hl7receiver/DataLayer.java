package org.endeavourhealth.hl7receiver;

import org.endeavourhealth.hl7receiver.model.db.Channel;
import org.endeavourhealth.hl7receiver.model.db.DbConfiguration;
import org.endeavourhealth.hl7receiver.model.db.Instance;
import org.endeavourhealth.utilities.postgres.PgStoredProc;
import org.endeavourhealth.utilities.postgres.PgStoredProcException;

import javax.sql.DataSource;
import java.util.List;

public class DataLayer
{
    private DataSource dataSource;

    public DataLayer(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public DbConfiguration getConfiguration(String instanceId) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("configuration.get_configuration")
                .addParameter("_instance_id", instanceId);

        Instance instance = pgStoredProc.executeMultiQuerySingleRow((resultSet) ->
                new Instance()
                        .setInstanceId(resultSet.getString("instance_id"))
                        .setInstanceDescription(resultSet.getString("description")));

        List<Channel> channels = pgStoredProc.executeMultiQuery((resultSet) ->
                new Channel()
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
                .setInstance(instance)
                .setChannels(channels);
    }
}
