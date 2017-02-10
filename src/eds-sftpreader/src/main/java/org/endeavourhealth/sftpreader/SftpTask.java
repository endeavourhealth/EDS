package org.endeavourhealth.sftpreader;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.Header;
import org.endeavourhealth.core.eds.EdsSender;
import org.endeavourhealth.core.keycloak.KeycloakClient;
import org.endeavourhealth.core.postgres.PgStoredProcException;
import org.endeavourhealth.core.utility.StreamExtension;
import org.endeavourhealth.sftpreader.implementations.*;
import org.endeavourhealth.sftpreader.model.db.*;
import org.endeavourhealth.sftpreader.model.exceptions.SftpFilenameParseException;
import org.endeavourhealth.sftpreader.model.exceptions.SftpReaderException;
import org.endeavourhealth.sftpreader.model.exceptions.SftpValidationException;
import org.endeavourhealth.sftpreader.utilities.PgpUtil;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpConnection;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpConnectionDetails;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpConnectionException;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpRemoteFile;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class SftpTask extends TimerTask
{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SftpTask.class);
    private static final DateTimeFormatter DATE_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm");

    private Configuration configuration = null;
    private DbConfiguration dbConfiguration = null;
    private DataLayer db = null;

    public SftpTask(Configuration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public void run()
    {
        LocalDateTime startTime = LocalDateTime.now();

        try
        {
            LOG.trace(">>>Starting scheduled SftpTask run, initialising");
            initialise();

            LOG.trace(">>>Downloading and decrypting files");

            if (!downloadAndProcessFiles())
                throw new SftpReaderException("Exception occurred downloading and processing files - halting to prevent incorrect ordering of batches.");

            LOG.trace(">>>Validating and sequencing batches");
            if (!validateAndSequenceBatches())
                throw new SftpReaderException("Exception occurred validating and sequencing batches - halting to prevent incorrect ordering of batches.");

            LOG.trace(">>>Notifying EDS");
            notifyEds();

            LOG.trace(">>>Completed SftpTask run");
        }
        catch (Exception e)
        {
            LOG.error(">>>Fatal exception in SftpTask run, terminating this run", e);
        }

        LOG.trace(">>>Next run scheduled for " + calculateNextRunTime(startTime).format(DATE_DISPLAY_FORMAT));
        LOG.trace("--------------------------------------------------");
    }

    private void initialise() throws Exception
    {
        this.dbConfiguration = configuration.getDbConfiguration();
        this.db = new DataLayer(configuration.getDatabaseConnection());
    }

    private boolean downloadAndProcessFiles()
    {
        SftpConnection sftpConnection = null;

        try
        {
            sftpConnection = openSftpConnection(dbConfiguration.getDbConfigurationSftp());

            String remotePath = dbConfiguration.getDbConfigurationSftp().getRemotePath();

            sftpConnection.cd(remotePath);
            List<SftpRemoteFile> sftpRemoteFiles = getFileList(sftpConnection, "\\");

            int countAlreadyProcessed = 0;

            LOG.trace("Found {} files in {}", new Integer(sftpRemoteFiles.size()), remotePath);

            for (SftpRemoteFile sftpRemoteFile : sftpRemoteFiles)
            {
                SftpFile batchFile = instantiateSftpBatchFile(sftpRemoteFile);

                if (!batchFile.isFilenameValid())
                {
                    LOG.error("   Invalid filename, skipping: " + batchFile.getFilename());
                    db.addUnknownFile(dbConfiguration.getInstanceId(), batchFile);
                    continue;
                }

                AddFileResult addFileResult = db.addFile(configuration.getInstanceName(), batchFile);

                if (addFileResult.isFileAlreadyProcessed())
                {
                    countAlreadyProcessed ++;
                    continue;
                }

                batchFile.setBatchFileId(addFileResult.getBatchFileId());

                createBatchDirectory(batchFile);

                downloadFile(sftpConnection, batchFile);

                if (batchFile.doesFileNeedDecrypting())
                    decryptFile(batchFile);
            }

            if (countAlreadyProcessed > 0)
                LOG.trace("Skipped {} files as already processed them", new Integer(countAlreadyProcessed));

            LOG.info("Completed processing {} files", Integer.toString(sftpRemoteFiles.size()));

            return true;
        }
        catch (Exception e)
        {
            LOG.error("Exception occurred while processing files - cannot continue or may process batches out of order", e);
        }
        finally
        {
            closeConnection(sftpConnection);
        }

        return false;
    }

    private static SftpConnection openSftpConnection(DbConfigurationSftp configurationSftp) throws SftpConnectionException, JSchException, IOException
    {
        SftpConnection sftpConnection = new SftpConnection(getSftpConnectionDetails(configurationSftp));

        String hostname = sftpConnection.getConnectionDetails().getHostname();
        String port = Integer.toString(sftpConnection.getConnectionDetails().getPort());
        String username = sftpConnection.getConnectionDetails().getUsername();

        LOG.info(" Opening SFTP connection to " + hostname + " on port " + port + " with user " + username);

        sftpConnection.open();

        return sftpConnection;
    }

    private static SftpConnectionDetails getSftpConnectionDetails(DbConfigurationSftp configurationSftp)
    {
        return new SftpConnectionDetails()
                .setHostname(configurationSftp.getHostname())
                .setPort(configurationSftp.getPort())
                .setUsername(configurationSftp.getUsername())
                .setClientPrivateKey(configurationSftp.getClientPrivateKey())
                .setClientPrivateKeyPassword(configurationSftp.getClientPrivateKeyPassword())
                .setHostPublicKey(configurationSftp.getHostPublicKey());
    }

    private static void closeConnection(SftpConnection sftpConnection) {

        if (sftpConnection != null)
            sftpConnection.close();
    }

    private static List<SftpRemoteFile> getFileList(SftpConnection sftpConnection, String remotePath) throws SftpException
    {
        return sftpConnection.getFileList(remotePath);
    }

    private void downloadFile(SftpConnection sftpConnection, SftpFile batchFile) throws Exception
    {
        downloadFile(sftpConnection, batchFile.getFilename(), batchFile.getLocalFilePath());

        batchFile.setLocalFileSizeBytes(getFileSizeBytes(batchFile.getLocalFilePath()));

        db.setFileAsDownloaded(batchFile);
    }

    private static void downloadFile(SftpConnection sftpConnection, String remoteFilePath, String localFilePath) throws SftpException, IOException
    {
        LOG.info("   Downloading file to: " + localFilePath);

        File temporaryDownloadFile = new File(localFilePath + ".download");

        if (temporaryDownloadFile.exists())
            if (!temporaryDownloadFile.delete())
                throw new IOException("Could not delete existing temporary download file " + temporaryDownloadFile);

        InputStream inputStream = sftpConnection.getFile(remoteFilePath);

        Files.copy(inputStream, temporaryDownloadFile.toPath());

        //if we previously failure during decryption, the renamed file will already exist, so delete it
        File destination = new File(localFilePath);
        if (destination.exists()) {
            destination.delete();
        }

        if (!temporaryDownloadFile.renameTo(destination))
            throw new IOException("Could not temporary download file to " + localFilePath);
    }

    private SftpFile instantiateSftpBatchFile(SftpRemoteFile sftpRemoteFile)
    {
        SftpFilenameParser emisSftpFilenameParser = ImplementationActivator.createFilenameParser(sftpRemoteFile.getFilename(), dbConfiguration);

        return new SftpFile(sftpRemoteFile,
                emisSftpFilenameParser,
                dbConfiguration.getLocalRootPath());
    }

    private void createBatchDirectory(SftpFile batchFile) throws IOException
    {
        File localPath = new File(batchFile.getLocalPath());

        if (!localPath.exists())
            if (!localPath.mkdirs())
                throw new IOException("Could not create path " + localPath);
    }

    private void deleteRemoteFile(SftpConnection sftpConnection, String remoteFilePath) throws SftpException
    {
        LOG.info("Deleting remote file " + remoteFilePath);

        sftpConnection.deleteFile(remoteFilePath);
    }

    private void decryptFile(SftpFile batchFile) throws Exception
    {
        String localFilePath = batchFile.getLocalFilePath();
        String decryptedLocalFilePath = batchFile.getDecryptedLocalFilePath();
        String privateKey = dbConfiguration.getDbConfigurationPgp().getPgpRecipientPrivateKey();
        String privateKeyPassword = dbConfiguration.getDbConfigurationPgp().getPgpRecipientPrivateKeyPassword();
        //String publicKey = dbConfiguration.getDbConfigurationPgp().getPgpRecipientPublicKey();
        String publicKey = dbConfiguration.getDbConfigurationPgp().getPgpSenderPublicKey();

        LOG.info("   Decrypting file to: " + decryptedLocalFilePath);

        PgpUtil.decryptAndVerify(localFilePath, privateKey, privateKeyPassword, decryptedLocalFilePath, publicKey);

        batchFile.setDecryptedFileSizeBytes(getFileSizeBytes(batchFile.getDecryptedLocalFilePath()));

        db.setFileAsDecrypted(batchFile);
    }

    private static long getFileSizeBytes(String filePath)
    {
        File file = new File(filePath);
        return file.length();
    }

    private boolean validateAndSequenceBatches()
    {
        try
        {
            List<UnknownFile> unknownFiles = getUnknownFiles();

            if (unknownFiles.size() > 0)
                throw new SftpValidationException("There are " + Integer.toString(unknownFiles.size()) + " unknown files present.");

            List<Batch> incompleteBatches = getIncompleteBatches();

            if (incompleteBatches.size() > 0) {

                Batch lastCompleteBatch = db.getLastCompleteBatch(dbConfiguration.getInstanceId());

                validateBatches(incompleteBatches, lastCompleteBatch);
                splitBatches(incompleteBatches);
                sequenceBatches(incompleteBatches, lastCompleteBatch);
            }

            return true;
        }
        catch (Exception e)
        {
            LOG.error("Error occurred during validation and sequencing", e);
        }

        return false;
    }

    private List<UnknownFile> getUnknownFiles() throws PgStoredProcException
    {
        return db.getUnknownFiles(dbConfiguration.getInstanceId());
    }

    private List<Batch> getIncompleteBatches() throws PgStoredProcException
    {
        LOG.trace(" Getting batches ready for validation and sequencing");

        List<Batch> incompleteBatches = db.getIncompleteBatches(dbConfiguration.getInstanceId());

        LOG.trace(" There are {} batches ready for validation and sequencing", Integer.toString(incompleteBatches.size()));

        return incompleteBatches;
    }

    private void validateBatches(List<Batch> incompleteBatches, Batch lastCompleteBatch) throws SftpValidationException
    {
        String batchIdentifiers = StringUtils.join(incompleteBatches
                .stream()
                .map(t -> t.getBatchIdentifier())
                .collect(Collectors.toList()), ", ");

        LOG.trace(" Validating batches: " + batchIdentifiers);

        SftpBatchValidator sftpBatchValidator = ImplementationActivator.createSftpBatchValidator();
        sftpBatchValidator.validateBatches(incompleteBatches, lastCompleteBatch, dbConfiguration);

        LOG.trace(" Completed batch validation");
    }

    private void sequenceBatches(List<Batch> incompleteBatches, Batch lastCompleteBatch) throws SftpValidationException, SftpFilenameParseException, PgStoredProcException
    {
        LOG.trace(" Sequencing batches");

        int nextSequenceNumber = getNextSequenceNumber(lastCompleteBatch);

        SftpBatchSequencer sftpBatchSequencer = ImplementationActivator.createSftpBatchSequencer();
        Map<Batch, Integer> batchSequence = sftpBatchSequencer.determineBatchSequenceNumbers(incompleteBatches, nextSequenceNumber, lastCompleteBatch);

        Map<Batch, Integer> sortedBatchSequence = StreamExtension.sortByValue(batchSequence);

        if (!new HashSet(incompleteBatches).equals(sortedBatchSequence.keySet()))
            throw new SftpValidationException("Batch sequence does not contain all unsequenced batches");

        for (Batch batch : sortedBatchSequence.keySet())
            if (sortedBatchSequence.get(batch).intValue() != nextSequenceNumber++)
                throw new SftpValidationException("Unexpected batch sequence number");

        for (Batch batch : sortedBatchSequence.keySet())
        {
            LOG.debug("  Batch " + batch.getBatchIdentifier() + " sequenced as " + sortedBatchSequence.get(batch).toString());
            db.setBatchAsComplete(batch, sortedBatchSequence.get(batch));
        }

        LOG.trace(" Completed batch sequencing");
    }

    private void splitBatches(List<Batch> batches) throws Exception {
        LOG.trace("Splitting batches");

        for (Batch batch: batches) {

            //delete any pre-existing splits for this batch
            db.deleteBatchSplits(batch);

            SftpBatchSplitter sftpBatchSplitter = ImplementationActivator.createSftpBatchSplitter();
            List<BatchSplit> splitBatches = sftpBatchSplitter.splitBatch(batch, db, dbConfiguration);

            for (BatchSplit splitBatch: splitBatches) {
                db.addBatchSplit(splitBatch, dbConfiguration.getInstanceId());
            }
        }

        LOG.trace("Completed splitting");
    }

    private static int getNextSequenceNumber(Batch lastCompleteBatch)
    {
        if (lastCompleteBatch == null)
            return 1;

        return lastCompleteBatch.getSequenceNumber() + 1;
    }

    private void notifyEds() throws PgStoredProcException, SftpReaderException {

        List<BatchSplit> unnotifiedBatchSplits = db.getUnnotifiedBatchSplits(dbConfiguration.getInstanceId());
        LOG.debug("There are {} complete split batches for notification", unnotifiedBatchSplits.size());

        if (unnotifiedBatchSplits.isEmpty()) {
            return;
        }

        if (dbConfiguration.getDbConfigurationEds().isUseKeycloak())
        {
            LOG.trace("Initialising keycloak at: {}", dbConfiguration.getDbConfigurationEds().getKeycloakTokenUri());

            KeycloakClient.init(dbConfiguration.getDbConfigurationEds().getKeycloakTokenUri(),
                    dbConfiguration.getDbConfigurationEds().getKeycloakRealm(),
                    dbConfiguration.getDbConfigurationEds().getKeycloakUsername(),
                    dbConfiguration.getDbConfigurationEds().getKeycloakPassword(),
                    dbConfiguration.getDbConfigurationEds().getKeycloakClientId());

            try {
                Header response = KeycloakClient.instance().getAuthorizationHeader();

                LOG.trace("Keycloak authorization header is {}: {}", response.getName(), response.getValue());
            } catch (IOException e) {
                throw new SftpReaderException("Error initialising keycloak", e);
            }
        }
        else
        {
            LOG.trace("Keycloak is not enabled");
        }

        //sort the batch splits by sequence ID
        unnotifiedBatchSplits = unnotifiedBatchSplits
                .stream()
                .sorted(Comparator.comparing(t -> t.getBatch().getSequenceNumber()))
                .collect(Collectors.toList());

        //hash the split batches by organisation ID and keep an ordered list of the organisations,
        //so we notify the earliest received organisations first
        HashMap<String, List<BatchSplit>> hmByOrg = new HashMap<>();
        List<String> organisationIds = new ArrayList<>();

        for (BatchSplit batchSplit: unnotifiedBatchSplits) {

            List<BatchSplit> list = hmByOrg.get(batchSplit.getOrganisationId());
            if (list == null) {
                list = new ArrayList<>();
                hmByOrg.put(batchSplit.getOrganisationId(), list);
                organisationIds.add(batchSplit.getOrganisationId());
            }
            list.add(batchSplit);
        }

        //then attempt to notify EDS for each organisation
        int countSuccess = 0;
        int countFail = 0;

        for (String organisationId: organisationIds) {
            List<BatchSplit> batchSplits = hmByOrg.get(organisationId);

            try
            {
                for (BatchSplit batchSplit: batchSplits) {

                    LOG.trace("Notifying EDS for batch split: {}", batchSplit.getBatchSplitId());
                    notify(batchSplit);
                    countSuccess ++;
                }
            }
            catch (Exception e) {
                countFail ++;
                LOG.error("Error occurred notifying EDS for batch split", e);
            }
        }

        LOG.info("Notified EDS successfully {} times and failed {}", countSuccess, countFail);
    }


    private void notify(BatchSplit unnotifiedBatchSplit) throws SftpReaderException, PgStoredProcException, IOException
    {
        SftpNotificationCreator sftpNotificationCreator = ImplementationActivator.createSftpNotificationCreator();

        String messagePayload = sftpNotificationCreator.createNotificationMessage(dbConfiguration, unnotifiedBatchSplit);

        UUID messageId = UUID.randomUUID();
        String organisationId = unnotifiedBatchSplit.getOrganisationId();
        String envelopeContentType = dbConfiguration.getDbConfigurationEds().getEnvelopeContentType();
        String softwareVersion = dbConfiguration.getDbConfigurationEds().getSoftwareVersion();
        String outboundMessage = EdsSender.buildEnvelope(messageId, organisationId, envelopeContentType, softwareVersion, messagePayload);

        try {
            String edsUrl = dbConfiguration.getDbConfigurationEds().getEdsUrl();
            boolean useKeycloak = dbConfiguration.getDbConfigurationEds().isUseKeycloak();

            String inboundMessage = EdsSender.notifyEds(edsUrl, useKeycloak, outboundMessage);

            db.addBatchNotification(unnotifiedBatchSplit.getBatchId(),
                    unnotifiedBatchSplit.getBatchSplitId(),
                    dbConfiguration.getInstanceId(),
                    messageId,
                    outboundMessage,
                    inboundMessage,
                    true,
                    null);
        }
        catch (Exception e)
        {
            String inboundMessage = e.getMessage();

            db.addBatchNotification(unnotifiedBatchSplit.getBatchId(),
                    unnotifiedBatchSplit.getBatchSplitId(),
                    dbConfiguration.getInstanceId(),
                    messageId,
                    outboundMessage,
                    inboundMessage,
                    false,
                    e.getClass().getName() + " | " + e.getMessage());

            throw new SftpReaderException("Error notifying EDS for batch split " + unnotifiedBatchSplit.getBatchSplitId(), e);
        }
    }

    private LocalDateTime calculateNextRunTime(LocalDateTime thisRunStartTime) {
        Validate.notNull(thisRunStartTime);

        return thisRunStartTime.plusSeconds(configuration.getDbConfiguration().getPollFrequencySeconds());
    }
}
