package org.endeavourhealth.sftpreader;

import org.endeavourhealth.sftpreader.model.DBConfiguration;
import org.endeavourhealth.sftpreader.utilities.postgres.PgStoredProc;
import org.endeavourhealth.sftpreader.utilities.postgres.PgStoredProcException;

import javax.sql.DataSource;
import java.util.List;

public class DataLayer
{
    private DataSource dataSource;

    public DataLayer(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public List<DBConfiguration> getDBConfiguration(String instanceId) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("sftpreader.get_configuration")
                .addParameter("_instance_id", instanceId);

        return pgStoredProc.executeQuery((resultSet) ->
                new DBConfiguration()
                        .setInstanceId(resultSet.getString("instance_id"))
                        .setHostname(resultSet.getString("hostname"))
                        .setPort(resultSet.getInt("port"))
                        .setRemotePath(resultSet.getString("remote_path"))
                        .setUsername(resultSet.getString("username"))
                        .setClientPrivateKey(resultSet.getString("client_private_key"))
                        .setClientPrivateKeyPassword(resultSet.getString("client_private_key_password"))
                        .setHostPublicKey(resultSet.getString("host_public_key")));
    }
}
