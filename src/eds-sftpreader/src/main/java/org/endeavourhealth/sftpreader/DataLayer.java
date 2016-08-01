package org.endeavourhealth.sftpreader;

import org.endeavourhealth.sftpreader.utilities.StreamExtension;
import org.endeavourhealth.sftpreader.batchFileImplementations.BatchFile;
import org.endeavourhealth.sftpreader.model.db.*;
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

    public DbConfiguration getConfiguration(String instanceId) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("sftpreader.get_configuration")
                .addParameter("_instance_id", instanceId);

        DbConfiguration dbConfiguration = pgStoredProc.executeSingleRow((resultSet) ->
                new DbConfiguration()

                        .setInstanceId(resultSet.getString("instance_id"))
                        .setInstanceDescription(resultSet.getString("instance_description"))
                        .setInterfaceTypeId(resultSet.getInt("interface_type_id"))
                        .setInterfaceTypeDescription(resultSet.getString("interface_type_description"))
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

        dbConfiguration.setInterfaceFileTypes(getInterfaceFileTypes(instanceId));

        return dbConfiguration;
    }

    public List<String> getInterfaceFileTypes(String instanceId) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("sftpreader.get_interface_file_types")
                .addParameter("_instance_id", instanceId);

        return pgStoredProc.executeQuery(resultSet -> resultSet.getString("file_type_identifier"));
    }

    public AddFileResult addFile(String instanceId, BatchFile batchFile) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("sftpreader.add_file")
                .addParameter("_instance_id", instanceId)
                .addParameter("_batch_identifier", batchFile.getBatchIdentifier())
                .addParameter("_file_type_identifier", batchFile.getFileTypeIdentifier())
                .addParameter("_filename", batchFile.getFilename())
                .addParameter("_local_relative_path", batchFile.getLocalRelativePath())
                .addParameter("_remote_size_bytes", batchFile.getRemoteFileSizeInBytes())
                .addParameter("_remote_created_date", batchFile.getRemoteLastModifiedDate())
                .addParameter("_requires_decryption", batchFile.doesFileNeedDecrypting());

        return pgStoredProc.executeSingleRow((resultSet) ->
                new AddFileResult()
                    .setFileAlreadyProcessed(resultSet.getBoolean("file_already_processed"))
                    .setBatchFileId(resultSet.getInt("batch_file_id")));
    }

    public void setFileAsDownloaded(BatchFile batchFile) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("sftpreader.set_file_as_downloaded")
                .addParameter("_batch_file_id", batchFile.getBatchFileId())
                .addParameter("_local_size_bytes", batchFile.getLocalFileSizeBytes());

        pgStoredProc.execute();
    }

    public void setFileAsDecrypted(BatchFile batchFile) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("sftpreader.set_file_as_decrypted")
                .addParameter("_batch_file_id", batchFile.getBatchFileId())
                .addParameter("_decrypted_filename", batchFile.getDecryptedFilename())
                .addParameter("_decrypted_size_bytes", batchFile.getDecryptedFileSizeBytes());

        pgStoredProc.execute();
    }

    public void addUnknownFile(String instanceId, BatchFile batchFile) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("sftpreader.add_unknown_file")
                .addParameter("_instance_id", instanceId)
                .addParameter("_filename", batchFile.getFilename())
                .addParameter("_remote_size_bytes", batchFile.getRemoteFileSizeInBytes())
                .addParameter("_remote_created_date", batchFile.getRemoteLastModifiedDate());

        pgStoredProc.execute();
    }

    public List<IncompleteBatch> getIncompleteBatches(String instanceId) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("sftpreader.get_incomplete_batches")
                .addParameter("_instance_id", instanceId);

        List<IncompleteBatch> incompleteBatches = pgStoredProc.executeMultiQuery(resultSet ->
                new IncompleteBatch()
                        .setBatchId(resultSet.getInt("batch_id"))
                        .setBatchIdentifier(resultSet.getString("batch_identifier"))
                        .setLocalRelativePath(resultSet.getString("local_relative_path")));

        List<IncompleteBatchFile> incompleteBatchFiles = pgStoredProc.nextMultiQuery(resultSet ->
                new IncompleteBatchFile()
                        .setBatchId(resultSet.getInt("batch_id"))
                        .setBatchFileId(resultSet.getInt("batch_file_id"))
                        .setFileTypeIdentifier(resultSet.getString("file_type_identifier"))
                        .setFilename(resultSet.getString("filename"))
                        .setRemoteSizeBytes(resultSet.getLong("remote_size_bytes"))
                        .setDownloaded(resultSet.getBoolean("is_downloaded"))
                        .setLocalSizeBytes(resultSet.getLong("local_size_bytes"))
                        .setRequiresDecryption(resultSet.getBoolean("requires_decryption"))
                        .setDecrypted(resultSet.getBoolean("is_decrypted"))
                        .setDecryptedFilename(resultSet.getString("decrypted_filename"))
                        .setDecryptedSizeBytes(resultSet.getLong("decrypted_size_bytes")));

        incompleteBatchFiles.forEach(t ->
                incompleteBatches
                        .stream()
                        .filter(s -> s.getBatchId() == t.getBatchId())
                        .collect(StreamExtension.singleCollector())
                        .addIncompleteBatchFile(t));

        return incompleteBatches;
    }
}
