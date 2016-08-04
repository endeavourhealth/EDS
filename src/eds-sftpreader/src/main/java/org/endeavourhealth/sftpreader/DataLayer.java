package org.endeavourhealth.sftpreader;

import org.endeavourhealth.sftpreader.utilities.StreamExtension;
import org.endeavourhealth.sftpreader.model.db.*;
import org.endeavourhealth.sftpreader.utilities.postgres.PgResultSet;
import org.endeavourhealth.sftpreader.utilities.postgres.PgStoredProc;
import org.endeavourhealth.sftpreader.utilities.postgres.PgStoredProcException;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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
                        .setInterfaceTypeName(resultSet.getString("interface_type_name"))
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
                            .setPgpRecipientPrivateKeyPassword(resultSet.getString("pgp_recipient_private_key_password")))

                        .setDbConfigurationEds(new DbConfigurationEds()
                            .setEdsUrl(resultSet.getString("eds_url"))
                            .setEdsServiceIdentifier(resultSet.getString("eds_service_identifier"))
                            .setSoftwareName(resultSet.getString("software_name"))
                            .setSoftwareVersion(resultSet.getString("software_version"))
                            .setEnvelopeContentType(resultSet.getString("envelope_content_type"))));

        dbConfiguration.setDbConfigurationKvp(getConfigurationKvp(instanceId));
        dbConfiguration.setInterfaceFileTypes(getInterfaceFileTypes(instanceId));

        return dbConfiguration;
    }

    private List<DbConfigurationKvp> getConfigurationKvp(String instanceId) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("sftpreader.get_configuration_kvp")
                .addParameter("_instance_id", instanceId);

        return pgStoredProc.executeQuery((resultSet) -> new DbConfigurationKvp()
                        .setKey(resultSet.getString("key"))
                        .setValue(resultSet.getString("value")));
    }

    public List<String> getInterfaceFileTypes(String instanceId) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("sftpreader.get_interface_file_types")
                .addParameter("_instance_id", instanceId);

        return pgStoredProc.executeQuery(resultSet -> resultSet.getString("file_type_identifier"));
    }

    public AddFileResult addFile(String instanceId, SftpFile batchFile) throws PgStoredProcException
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

    public void setFileAsDownloaded(SftpFile batchFile) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("sftpreader.set_file_as_downloaded")
                .addParameter("_batch_file_id", batchFile.getBatchFileId())
                .addParameter("_local_size_bytes", batchFile.getLocalFileSizeBytes());

        pgStoredProc.execute();
    }

    public void setFileAsDecrypted(SftpFile batchFile) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("sftpreader.set_file_as_decrypted")
                .addParameter("_batch_file_id", batchFile.getBatchFileId())
                .addParameter("_decrypted_filename", batchFile.getDecryptedFilename())
                .addParameter("_decrypted_size_bytes", batchFile.getDecryptedFileSizeBytes());

        pgStoredProc.execute();
    }

    public void addUnknownFile(String instanceId, SftpFile batchFile) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("sftpreader.add_unknown_file")
                .addParameter("_instance_id", instanceId)
                .addParameter("_filename", batchFile.getFilename())
                .addParameter("_remote_size_bytes", batchFile.getRemoteFileSizeInBytes())
                .addParameter("_remote_created_date", batchFile.getRemoteLastModifiedDate());

        pgStoredProc.execute();
    }

    public List<Batch> getIncompleteBatches(String instanceId) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("sftpreader.get_incomplete_batches")
                .addParameter("_instance_id", instanceId);

        return populateBatch(pgStoredProc);
    }

    public Batch getLastCompleteBatch(String instanceId) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("sftpreader.get_last_complete_batch")
                .addParameter("_instance_id", instanceId);

        List<Batch> batches = populateBatch(pgStoredProc);

        if (batches.size() > 1)
            throw new PgStoredProcException("More than one last complete batch returned");

        if (batches.size() == 0)
            return null;

        return batches.get(0);
    }

    public List<Batch> getUnnotifiedBatches(String instanceId) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("sftpreader.get_unnotified_batches")
                .addParameter("_instance_id", instanceId);

        return populateBatch(pgStoredProc);
    }

    public List<UnknownFile> getUnknownFiles(String instanceId) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("sftpreader.get_unknown_files")
                .addParameter("_instance_id", instanceId);

        return pgStoredProc.executeQuery(resultSet -> new UnknownFile()
                .setUnknownFileId(resultSet.getInt("unknown_file_id"))
                .setFilename(resultSet.getString("filename"))
                .setInsertDate(PgResultSet.getLocalDateTime(resultSet, "insert_date"))
                .setRemoteCreatedDate(PgResultSet.getLocalDateTime(resultSet, "remote_created_date"))
                .setRemoteSizeBytes(resultSet.getLong("remote_size_bytes")));
    }

    private static List<Batch> populateBatch(PgStoredProc pgStoredProc) throws PgStoredProcException
    {
        List<Batch> batches = pgStoredProc.executeMultiQuery(resultSet ->
                new Batch()
                        .setBatchId(resultSet.getInt("batch_id"))
                        .setBatchIdentifier(resultSet.getString("batch_identifier"))
                        .setLocalRelativePath(resultSet.getString("local_relative_path"))
                        .setSequenceNumber(PgResultSet.getInteger(resultSet, "sequence_number")));

        List<BatchFile> batchFiles = pgStoredProc.nextMultiQuery(resultSet ->
                new BatchFile()
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

        batchFiles.forEach(t ->
                batches
                        .stream()
                        .filter(s -> s.getBatchId() == t.getBatchId())
                        .collect(StreamExtension.singleCollector())
                        .addBatchFile(t));

        return batches;
    }

    public void setBatchAsComplete(Batch batch, int sequenceNumber) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("sftpreader.set_batch_as_complete")
                .addParameter("_batch_id", batch.getBatchId())
                .addParameter("_sequence_number", Integer.toString(sequenceNumber));

        pgStoredProc.execute();
    }

    public void addBatchNotification(int batchId, String instanceId, EdsNotifier edsNotifier, boolean wasSuccess, String errorText) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("sftpreader.add_batch_notification")
                .addParameter("_batch_id", batchId)
                .addParameter("_instance_id", instanceId)
                .addParameter("_message_uuid", edsNotifier.getMessageId())
                .addParameter("_outbound_message", edsNotifier.getOutboundMessage())
                .addParameter("_inbound_message", edsNotifier.getInboundMessage())
                .addParameter("_was_success", wasSuccess)
                .addParameter("_error_text", errorText);

        pgStoredProc.execute();
    }
}
