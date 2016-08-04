package org.endeavourhealth.sftpreader;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.sftpreader.implementations.*;
import org.endeavourhealth.sftpreader.model.db.*;
import org.endeavourhealth.sftpreader.model.exceptions.SftpFilenameParseException;
import org.endeavourhealth.sftpreader.model.exceptions.SftpReaderException;
import org.endeavourhealth.sftpreader.model.exceptions.SftpValidationException;
import org.endeavourhealth.sftpreader.utilities.PgpUtil;
import org.endeavourhealth.sftpreader.utilities.StreamExtension;
import org.endeavourhealth.sftpreader.utilities.postgres.PgStoredProcException;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpConnection;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpConnectionDetails;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpConnectionException;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpRemoteFile;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class SftpTask extends TimerTask
{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SftpTask.class);

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
        try
        {
            LOG.info(">>>Starting scheduled SftpTask run, initialising");
            initialise();

            LOG.info(">>>Downloading and decrypting files");
            downloadAndProcessFiles();

            LOG.info(">>>Validating and sequencing batches");
            validateAndSequenceBatches();

            LOG.info(">>>Notifying EDS");
            notifyEds();

            LOG.info(">>>Completed SftpTask run");
        }
        catch (Exception e)
        {
            LOG.error(">>>Fatal exception in SftpTask run, terminating this run", e);
        }
    }

    private void initialise() throws Exception
    {
        this.dbConfiguration = configuration.getDbConfiguration();
        this.db = new DataLayer(configuration.getDatabaseConnection());
    }

    private void downloadAndProcessFiles()
    {
        SftpConnection sftpConnection = null;

        try
        {
            sftpConnection = openSftpConnection(dbConfiguration.getDbConfigurationSftp());

            String remotePath = dbConfiguration.getDbConfigurationSftp().getRemotePath();

            List<SftpRemoteFile> sftpRemoteFiles = getFileList(sftpConnection, remotePath);

            LOG.info( " Found " + Integer.toString(sftpRemoteFiles.size()) + " files");

            for (SftpRemoteFile sftpRemoteFile : sftpRemoteFiles)
            {
                LOG.info("  Processing remote file: " + sftpRemoteFile.getFilename());

                SftpFile batchFile = instantiateSftpBatchFile(sftpRemoteFile);

                if (!batchFile.isFilenameValid())
                {
                    LOG.info("   Invalid filename, skipping: " + batchFile.getFilename());
                    db.addUnknownFile(dbConfiguration.getInstanceId(), batchFile);
                    continue;
                }

                AddFileResult addFileResult = db.addFile(configuration.getInstanceId(), batchFile);

                if (addFileResult.isFileAlreadyProcessed())
                {
                    LOG.info("   Already processed, skipping: " + batchFile.getFilename());
                    continue;
                }

                batchFile.setBatchFileId(addFileResult.getBatchFileId());

                createBatchDirectory(batchFile);

                downloadFile(sftpConnection, batchFile);

                if (batchFile.doesFileNeedDecrypting())
                    decryptFile(batchFile);
            }

            LOG.info(" Completed processing " + Integer.toString(sftpRemoteFiles.size()) + " files");
        }
        catch (Exception e)
        {
            LOG.error("Exception occurred while processing files", e);
        }
        finally
        {
            closeConnection(sftpConnection);
        }
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

    private static void closeConnection(SftpConnection sftpConnection)
    {
        LOG.info("Closing SFTP connection");

        sftpConnection.close();
    }

    private static List<SftpRemoteFile> getFileList(SftpConnection sftpConnection, String remotePath) throws SftpException
    {
        LOG.info( " Get remote file list at: " + remotePath);

        return sftpConnection.getFileList(remotePath);
    }

    private void downloadFile(SftpConnection sftpConnection, SftpFile batchFile) throws Exception
    {
        downloadFile(sftpConnection, batchFile.getRemoteFilePath(), batchFile.getLocalFilePath());

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

        if (!temporaryDownloadFile.renameTo(new File(localFilePath)))
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
        String senderPublicKey = dbConfiguration.getDbConfigurationPgp().getPgpSenderPublicKey();
        String recipientPrivateKey = dbConfiguration.getDbConfigurationPgp().getPgpRecipientPrivateKey();
        String recipientPrivateKeyPassword = dbConfiguration.getDbConfigurationPgp().getPgpRecipientPrivateKeyPassword();

        LOG.info("   Decrypting file to: " + decryptedLocalFilePath);

        PgpUtil.decryptAndVerify(localFilePath, senderPublicKey, recipientPrivateKey, recipientPrivateKeyPassword, decryptedLocalFilePath);

        batchFile.setDecryptedFileSizeBytes(getFileSizeBytes(batchFile.getDecryptedLocalFilePath()));

        db.setFileAsDecrypted(batchFile);
    }

    private static long getFileSizeBytes(String filePath)
    {
        File file = new File(filePath);
        return file.length();
    }

    private void validateAndSequenceBatches() throws PgStoredProcException, SftpValidationException, SftpFilenameParseException
    {
        try
        {
            List<UnknownFile> unknownFiles = getUnknownFiles();

            if (unknownFiles.size() > 0)
                return;

            List<Batch> incompleteBatches = getIncompleteBatches();

            if (incompleteBatches.size() == 0)
                return;

            Batch lastCompleteBatch = db.getLastCompleteBatch(dbConfiguration.getInstanceId());

            validateBatches(incompleteBatches, lastCompleteBatch);

            sequenceBatches(incompleteBatches, lastCompleteBatch);
        }
        catch (Exception e)
        {
            LOG.error("Error occurred during validation and sequencing", e);
        }
    }

    private List<UnknownFile> getUnknownFiles() throws PgStoredProcException
    {
        LOG.info(" Checking for unknown files");

        List<UnknownFile> unknownFiles = db.getUnknownFiles(dbConfiguration.getInstanceId());

        LOG.info(" There are " + Integer.toString(unknownFiles.size()) + " unknown files");

        return unknownFiles;
    }

    private List<Batch> getIncompleteBatches() throws PgStoredProcException
    {
        LOG.info(" Getting batches ready for validation and sequencing");

        List<Batch> incompleteBatches = db.getIncompleteBatches(dbConfiguration.getInstanceId());

        LOG.info(" There are " + Integer.toString(incompleteBatches.size()) + " batches ready for validation and sequencing");

        return incompleteBatches;
    }

    private void validateBatches(List<Batch> incompleteBatches, Batch lastCompleteBatch) throws SftpValidationException
    {
        String batchIdentifiers = StringUtils.join(incompleteBatches
                .stream()
                .map(t -> t.getBatchIdentifier())
                .collect(Collectors.toList()), ", ");

        LOG.info(" Validating batches: " + batchIdentifiers);

        SftpBatchValidator sftpBatchValidator = ImplementationActivator.createSftpBatchValidator();
        sftpBatchValidator.validateBatches(incompleteBatches, lastCompleteBatch, dbConfiguration);

        LOG.info(" Completed batch validation");
    }

    private void sequenceBatches(List<Batch> incompleteBatches, Batch lastCompleteBatch) throws SftpValidationException, SftpFilenameParseException, PgStoredProcException
    {
        LOG.info(" Sequencing batches");

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
            LOG.info("  Batch " + batch.getBatchIdentifier() + " sequenced as " + sortedBatchSequence.get(batch).toString());
            db.setBatchAsComplete(batch, sortedBatchSequence.get(batch));
        }

        LOG.info(" Completed batch sequencing");
    }

    private static int getNextSequenceNumber(Batch lastCompleteBatch)
    {
        if (lastCompleteBatch == null)
            return 1;

        return lastCompleteBatch.getSequenceNumber() + 1;
    }

    private void notifyEds() throws PgStoredProcException, SftpReaderException
    {
        try
        {
            LOG.info(" Getting complete batches for notification");

            List<Batch> unnotifiedBatches = db.getUnnotifiedBatches(dbConfiguration.getInstanceId());

            LOG.info(" There are " + Integer.toString(unnotifiedBatches.size()) + " complete batches for notification");

            unnotifiedBatches = unnotifiedBatches
                    .stream()
                    .sorted(Comparator.comparing(t -> t.getSequenceNumber()))
                    .collect(Collectors.toList());

            for (Batch unnotifiedBatch : unnotifiedBatches)
            {
                LOG.info(" Notifying EDS for batch: " + unnotifiedBatch.getBatchIdentifier());
                notify(unnotifiedBatch);
            }
        }
        catch (Exception e)
        {
            LOG.error(" Error occurred notifying EDS", e);
        }
    }

    private void notify(Batch unnotifiedBatch) throws SftpReaderException, PgStoredProcException
    {
        SftpNotificationCreator sftpNotificationCreator = ImplementationActivator.createSftpNotificationCreator();

        String message = sftpNotificationCreator.createNotificationMessage(dbConfiguration, unnotifiedBatch);

        EdsNotifier edsNotifier = new EdsNotifier(dbConfiguration.getDbConfigurationEds(), message);

        boolean wasError = false;
        String errorMessage = null;
        Exception exception = null;

        try
        {
            edsNotifier.notifyEds();

        }
        catch (Exception e)
        {
            wasError = true;
            errorMessage = e.getMessage();
            exception = e;

            LOG.error("Error notifying EDS for batch " + unnotifiedBatch.getBatchIdentifier(), e);
        }

        db.addBatchNotification(unnotifiedBatch.getBatchId(), dbConfiguration.getInstanceId(), edsNotifier, (!wasError), errorMessage);

        if (exception != null)
            throw new SftpReaderException("Error notifying EDS for batch " + unnotifiedBatch.getBatchIdentifier(), exception);
    }
}
