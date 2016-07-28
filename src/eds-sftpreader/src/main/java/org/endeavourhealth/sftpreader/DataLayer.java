package org.endeavourhealth.sftpreader;

import org.endeavourhealth.sftpreader.batchFileImplementations.BatchFile;
import org.endeavourhealth.sftpreader.model.db.DbConfiguration;
import org.endeavourhealth.sftpreader.model.db.DbConfigurationPgp;
import org.endeavourhealth.sftpreader.model.db.DbConfigurationSftp;
import org.endeavourhealth.sftpreader.utilities.postgres.PgStoredProc;
import org.endeavourhealth.sftpreader.utilities.postgres.PgStoredProcException;

import javax.sql.DataSource;

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
                .setName("sftpreader.get_configuration")
                .addParameter("_instance_id", instanceId);

        return pgStoredProc.executeSingleRow((resultSet) ->
                new DbConfiguration()

                        .setInstanceId(resultSet.getString("instance_id"))
                        .setInstanceDescription(resultSet.getString("instance_description"))
                        .setBatchTypeId(resultSet.getInt("batch_type_id"))
                        .setBatchTypeDescription(resultSet.getString("batch_type_description"))
                        .setPollFrequencySeconds(resultSet.getInt("poll_frequency_seconds"))
                        .setLocalRootPath(resultSet.getString("local_root_path"))

                        .setDbConfigurationSftp(new DbConfigurationSftp()
                            .setHostname(resultSet.getString("hostname"))
                            .setPort(resultSet.getInt("port"))
                            .setRemotePath(resultSet.getString("remote_path"))
                            .setUsername(resultSet.getString("username"))
                            .setClientPrivateKey(resultSet.getString("client_private_key"))
                            .setClientPrivateKeyPassword(resultSet.getString("client_private_key_password"))
                            .setHostPublicKey(resultSet.getString("host_public_key")))

                        .setDbConfigurationPgp(new DbConfigurationPgp()
                            .setPgpFileExtensionFilter(resultSet.getString("pgp_file_extension_filter"))
                            .setPgpSenderPublicKey(resultSet.getString("pgp_sender_public_key"))
                            .setPgpRecipientPublicKey(resultSet.getString("pgp_recipient_public_key"))
                            .setPgpRecipientPrivateKey(resultSet.getString("pgp_recipient_private_key"))
                            .setPgpRecipientPrivateKeyPassword(resultSet.getString("pgp_recipient_private_key_password"))));
    }

    public int addFile(String instanceId, BatchFile batchFile) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("sftpreader.add_file")
                .addParameter("_instance_id", instanceId)
                .addParameter("_remote_batch_identifier", batchFile.getRemoteBatchIdentifier())
                .addParameter("_remote_file_type_identifier", batchFile.getRemoteFileTypeIdentifier())
                .addParameter("_filename", batchFile.getFilename())
                .addParameter("_remote_file_size_bytes", batchFile.getRemoteFileSizeInBytes())
                .addParameter("_remote_created_date", batchFile.getRemoteLastModifiedDate());

        return pgStoredProc.executeSingleRow((resultSet) -> resultSet.getInt("add_file"));
        // create executeScalar function instead of above syntax
    }
}
