package org.endeavourhealth.sftpreader;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.endeavourhealth.sftpreader.implementations.ImplementationActivator;
import org.endeavourhealth.sftpreader.implementations.SftpBatchSequencer;
import org.endeavourhealth.sftpreader.implementations.SftpBatchValidator;
import org.endeavourhealth.sftpreader.implementations.SftpFilenameParser;
import org.endeavourhealth.sftpreader.model.db.AddFileResult;
import org.endeavourhealth.sftpreader.model.db.DbConfiguration;
import org.endeavourhealth.sftpreader.model.db.Batch;
import org.endeavourhealth.sftpreader.model.db.DbConfigurationSftp;
import org.endeavourhealth.sftpreader.model.exceptions.SftpFilenameParseException;
import org.endeavourhealth.sftpreader.model.exceptions.SftpValidationException;
import org.endeavourhealth.sftpreader.utilities.PgpUtil;
import org.endeavourhealth.sftpreader.utilities.StreamExtension;
import org.endeavourhealth.sftpreader.utilities.postgres.PgStoredProcException;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpConnection;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpConnectionDetails;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpConnectionException;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpRemoteFile;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
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

            LOG.info(">>>Notifying onward actors");
            notifyOnwardPipeline();

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
            // open connection
            sftpConnection = openSftpConnection(dbConfiguration.getDbConfigurationSftp());

            // get file list
            String remotePath = dbConfiguration.getDbConfigurationSftp().getRemotePath();

            List<SftpRemoteFile> sftpRemoteFiles = getFileList(sftpConnection, remotePath);

            // process batch
            for (SftpRemoteFile sftpRemoteFile : sftpRemoteFiles)
            {
                LOG.info("Found file " + sftpRemoteFile.getFilename());

                SftpFile batchFile = instantiateSftpBatchFile(sftpRemoteFile);

                if (!batchFile.isFilenameValid())
                {
                    LOG.info("Invalid filename or batch file type identifier " + batchFile.getFilename() + ", skipping");
                    db.addUnknownFile(dbConfiguration.getInstanceId(), batchFile);
                    continue;
                }

                AddFileResult addFileResult = db.addFile(configuration.getInstanceId(), batchFile);

                if (addFileResult.isFileAlreadyProcessed())
                {
                    LOG.info("Skipping file as already processed " + batchFile.getFilename());
                    continue;
                }

                batchFile.setBatchFileId(addFileResult.getBatchFileId());

                createBatchDirectory(batchFile);

                downloadFile(sftpConnection, batchFile);

                if (batchFile.doesFileNeedDecrypting())
                    decryptFile(batchFile);

                LOG.info("End processing file " + batchFile.getFilename());
            }
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

        LOG.info("Opening SFTP connection to " + hostname + " on port " + port + " with user " + username);

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
        LOG.info("Get file list at " + remotePath);

        List<SftpRemoteFile> fileList = sftpConnection.getFileList(remotePath);

        LOG.info("Found " + Integer.toString(fileList.size()) + " files");

        return fileList;
    }

    private void downloadFile(SftpConnection sftpConnection, SftpFile batchFile) throws Exception
    {
        downloadFile(sftpConnection, batchFile.getRemoteFilePath(), batchFile.getLocalFilePath());

        batchFile.setLocalFileSizeBytes(getFileSizeBytes(batchFile.getLocalFilePath()));

        db.setFileAsDownloaded(batchFile);
    }

    private static void downloadFile(SftpConnection sftpConnection, String remoteFilePath, String localFilePath) throws SftpException, IOException
    {
        LOG.info("Downloading file");

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
        SftpFilenameParser emisSftpFilenameParser = ImplementationActivator.createFilenameParser(sftpRemoteFile.getFilename(),
                        dbConfiguration.getPgpFileExtensionFilter(),
                        dbConfiguration.getInterfaceFileTypes());

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

        LOG.info("Decrypting file " + localFilePath + " to " + decryptedLocalFilePath);

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
        List<Batch> incompleteBatches = db.getIncompleteBatches(dbConfiguration.getInstanceId());
        Batch lastCompleteBatch = db.getLastCompleteBatch(dbConfiguration.getInstanceId());

        SftpBatchValidator sftpBatchValidator = ImplementationActivator.createSftpBatchValidator();
        sftpBatchValidator.validateBatches(incompleteBatches, lastCompleteBatch, dbConfiguration);

        SftpBatchSequencer sftpBatchSequencer = ImplementationActivator.createSftpBatchSequencer();
        Map<Batch, Integer> batchSequence = sftpBatchSequencer.determineBatchSequenceNumbers(incompleteBatches, lastCompleteBatch);

        Map<Batch, Integer> sortedBatchSequence = StreamExtension.sortByValue(batchSequence);

        for (Batch batch : sortedBatchSequence.keySet())
            db.completeBatch(batch, sortedBatchSequence.get(batch));
    }

    private void notifyOnwardPipeline() throws PgStoredProcException
    {
        List<Batch> unnotifiedBatches = db.getUnnotifiedBatches(dbConfiguration.getInstanceId());

        unnotifiedBatches = unnotifiedBatches
                .stream()
                .sorted(Comparator.comparing(t -> t.getSequenceNumber()))
                .collect(Collectors.toList());

        for (Batch unnotifiedBatch : unnotifiedBatches)
            notify(unnotifiedBatch);
    }

    private void notify(Batch unnotifiedBatch)
    {
        // do notifiy
    }
}
