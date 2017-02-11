package org.endeavourhealth.sftpreader;

import org.endeavourhealth.core.postgres.PgResultSet;
import org.endeavourhealth.core.postgres.PgStoredProc;
import org.endeavourhealth.core.postgres.PgStoredProcException;
import org.endeavourhealth.core.postgres.logdigest.IDBDigestLogger;
import org.endeavourhealth.core.utility.StreamExtension;
import org.endeavourhealth.sftpreader.model.db.*;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;

public class DataLayer implements IDBDigestLogger
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

        DbConfiguration dbConfiguration = pgStoredProc.executeSingleOrEmptyRow((resultSet) ->
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
                            .setEnvelopeContentType(resultSet.getString("envelope_content_type"))
                            .setUseKeycloak(resultSet.getBoolean("use_keycloak"))
                            .setKeycloakTokenUri(resultSet.getString("keycloak_token_uri"))
                            .setKeycloakRealm(resultSet.getString("keycloak_realm"))
                            .setKeycloakUsername(resultSet.getString("keycloak_username"))
                            .setKeycloakPassword(resultSet.getString("keycloak_password"))
                            .setKeycloakClientId(resultSet.getString("keycloak_clientid"))));

        if (dbConfiguration != null) {
            dbConfiguration.setDbConfigurationKvp(getConfigurationKvp(instanceId));
            dbConfiguration.setInterfaceFileTypes(getInterfaceFileTypes(instanceId));
        }

        return dbConfiguration;
    }

    private List<DbConfigurationKvp> getConfigurationKvp(String instanceId) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("configuration.get_configuration_kvp")
                .addParameter("_instance_id", instanceId);

        return pgStoredProc.executeQuery((resultSet) -> new DbConfigurationKvp()
                        .setKey(resultSet.getString("key"))
                        .setValue(resultSet.getString("value")));
    }

    public List<String> getInterfaceFileTypes(String instanceId) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("configuration.get_interface_file_types")
                .addParameter("_instance_id", instanceId);

        return pgStoredProc.executeQuery(resultSet -> resultSet.getString("file_type_identifier"));
    }

    public void addEmisOrganisationMap(EmisOrganisationMap mapping) throws PgStoredProcException {

        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("configuration.add_emis_organisation_map")
                .addParameter("_guid", mapping.getGuid())
                .addParameter("_name", mapping.getName())
                .addParameter("_ods_code", mapping.getOdsCode());

        pgStoredProc.execute();
    }

    public EmisOrganisationMap getEmisOrganisationMap(String guid) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("configuration.get_emis_organisation_map")
                .addParameter("_guid", guid);

        List<EmisOrganisationMap> mappings = pgStoredProc.executeQuery(resultSet -> new EmisOrganisationMap()
                .setGuid(resultSet.getString("guid"))
                .setName(resultSet.getString("name"))
                .setOdsCode(resultSet.getString("ods_code")));

        if (mappings.isEmpty())
            return null;

        return mappings.get(0);
    }

    public AddFileResult addFile(String instanceId, SftpFile batchFile) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("log.add_file")
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
                .setName("log.set_file_as_downloaded")
                .addParameter("_batch_file_id", batchFile.getBatchFileId())
                .addParameter("_local_size_bytes", batchFile.getLocalFileSizeBytes());

        pgStoredProc.execute();
    }

    public void setFileAsDecrypted(SftpFile batchFile) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("log.set_file_as_decrypted")
                .addParameter("_batch_file_id", batchFile.getBatchFileId())
                .addParameter("_decrypted_filename", batchFile.getDecryptedFilename())
                .addParameter("_decrypted_size_bytes", batchFile.getDecryptedFileSizeBytes());

        pgStoredProc.execute();
    }

    public void addUnknownFile(String instanceId, SftpFile batchFile) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("log.add_unknown_file")
                .addParameter("_instance_id", instanceId)
                .addParameter("_filename", batchFile.getFilename())
                .addParameter("_remote_size_bytes", batchFile.getRemoteFileSizeInBytes())
                .addParameter("_remote_created_date", batchFile.getRemoteLastModifiedDate());

        pgStoredProc.execute();
    }

    public List<Batch> getIncompleteBatches(String instanceId) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("log.get_incomplete_batches")
                .addParameter("_instance_id", instanceId);

        return populateBatches(pgStoredProc);
    }

    public Batch getLastCompleteBatch(String instanceId) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("log.get_last_complete_batch")
                .addParameter("_instance_id", instanceId);

        List<Batch> batches = populateBatches(pgStoredProc);

        if (batches.size() > 1)
            throw new PgStoredProcException("More than one last complete batch returned");

        if (batches.size() == 0)
            return null;

        return batches.get(0);
    }

    public List<BatchSplit> getUnnotifiedBatchSplits(String instanceId) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("log.get_unnotified_batch_splits")
                .addParameter("_instance_id", instanceId);

        return populateBatchSplits(pgStoredProc);
    }

    private static List<BatchSplit> populateBatchSplits(PgStoredProc pgStoredProc) throws PgStoredProcException
    {
        List<BatchSplit> batchSplits = pgStoredProc.executeMultiQuery(resultSet ->
                new BatchSplit()
                    .setBatchSplitId(resultSet.getInt("batch_split_id"))
                    .setBatchId(resultSet.getInt("batch_id"))
                    .setLocalRelativePath(resultSet.getString("local_relative_path"))
                    .setOrganisationId(resultSet.getString("organisation_id")));

        List<Batch> batches = populateBatches(pgStoredProc);

        for (Batch batch: batches) {
            for (BatchSplit batchSplit: batchSplits) {
                if (batchSplit.getBatchId() == batch.getBatchId()) {
                    batchSplit.setBatch(batch);
                }
            }
        }

        return batchSplits;
    }

    public List<UnknownFile> getUnknownFiles(String instanceId) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("log.get_unknown_files")
                .addParameter("_instance_id", instanceId);

        return pgStoredProc.executeQuery(resultSet -> new UnknownFile()
                .setUnknownFileId(resultSet.getInt("unknown_file_id"))
                .setFilename(resultSet.getString("filename"))
                .setInsertDate(PgResultSet.getLocalDateTime(resultSet, "insert_date"))
                .setRemoteCreatedDate(PgResultSet.getLocalDateTime(resultSet, "remote_created_date"))
                .setRemoteSizeBytes(resultSet.getLong("remote_size_bytes")));
    }

    private static List<Batch> populateBatches(PgStoredProc pgStoredProc) throws PgStoredProcException
    {
        List<Batch> batches = pgStoredProc.executeMultiQuery(resultSet ->
                new Batch()
                        .setBatchId(resultSet.getInt("batch_id"))
                        .setBatchIdentifier(resultSet.getString("batch_identifier"))
                        .setLocalRelativePath(resultSet.getString("local_relative_path"))
                        .setSequenceNumber(PgResultSet.getInteger(resultSet, "sequence_number")));

        List<BatchFile> batchFiles = pgStoredProc.executeMultiQuery(resultSet ->
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
                .setName("log.set_batch_as_complete")
                .addParameter("_batch_id", batch.getBatchId())
                .addParameter("_sequence_number", Integer.toString(sequenceNumber));

        pgStoredProc.execute();
    }

    public void addBatchNotification(int batchId, int batchSplitId, String instanceId, UUID messageId, String outboundMessage, String inboundMessage, boolean wasSuccess, String errorText) throws PgStoredProcException
    {
        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("log.add_batch_notification")
                .addParameter("_batch_id", batchId)
                .addParameter("_batch_split_id", batchSplitId)
                .addParameter("_instance_id", instanceId)
                .addParameter("_message_uuid", messageId)
                .addParameter("_outbound_message", outboundMessage)
                .addParameter("_inbound_message", inboundMessage)
                .addParameter("_was_success", wasSuccess)
                .addParameter("_error_text", errorText);

        pgStoredProc.execute();
    }

    public void addBatchSplit(BatchSplit batchSplit, String instanceId) throws PgStoredProcException {

        int batchId = batchSplit.getBatchId();
        String localRelativePath = batchSplit.getLocalRelativePath();
        String organisationId = batchSplit.getOrganisationId();

        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("log.add_batch_split")
                .addParameter("_batch_id", batchId)
                .addParameter("_instance_id", instanceId)
                .addParameter("_local_relative_path", localRelativePath)
                .addParameter("_organisation_id", organisationId);

        pgStoredProc.execute();
    }

    public void deleteBatchSplits(Batch batch) throws PgStoredProcException {

        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("log.delete_batch_splits")
                .addParameter("_batch_id", batch.getBatchId());

        pgStoredProc.execute();
    }

    public void logErrorDigest(String logClass, String logMethod, String logMessage, String exception) throws PgStoredProcException {

        PgStoredProc pgStoredProc = new PgStoredProc(dataSource)
                .setName("log.log_error_digest")
                .addParameter("_log_class", logClass)
                .addParameter("_log_method", logMethod)
                .addParameter("_log_message", logMessage)
                .addParameter("_exception", exception);

        pgStoredProc.execute();
    }
}
