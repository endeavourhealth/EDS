package org.endeavourhealth.sftpreader;

import com.jcraft.jsch.SftpException;
import org.endeavourhealth.sftpreader.implementations.ImplementationActivator;
import org.endeavourhealth.sftpreader.implementations.SftpBatchSequencer;
import org.endeavourhealth.sftpreader.implementations.SftpBatchValidator;
import org.endeavourhealth.sftpreader.implementations.SftpFilenameParser;
import org.endeavourhealth.sftpreader.model.db.AddFileResult;
import org.endeavourhealth.sftpreader.model.db.DbConfiguration;
import org.endeavourhealth.sftpreader.model.db.Batch;
import org.endeavourhealth.sftpreader.model.exceptions.SftpFilenameParseException;
import org.endeavourhealth.sftpreader.model.exceptions.SftpValidationException;
import org.endeavourhealth.sftpreader.utilities.PgpUtil;
import org.endeavourhealth.sftpreader.utilities.StreamExtension;
import org.endeavourhealth.sftpreader.utilities.postgres.PgStoredProcException;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpConnection;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpRemoteFile;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

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
            initialise();
            downloadAndProcessFiles();
            validateAndSequenceBatches();
            notifyOnwardPipeline();
        }
        catch (Exception e)
        {
            LOG.error("Fatal exception in SftpTask.run", e);
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
            sftpConnection = SftpHelper.openSftpConnection(dbConfiguration.getDbConfigurationSftp());

            // get file list
            String remotePath = dbConfiguration.getDbConfigurationSftp().getRemotePath();

            List<SftpRemoteFile> sftpRemoteFiles = SftpHelper.getFileList(sftpConnection, remotePath);

            // process batch
            for (SftpRemoteFile sftpRemoteFile : sftpRemoteFiles)
            {
                LOG.info("Start processing file " + sftpRemoteFile.getFilename());

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
            SftpHelper.closeConnection(sftpConnection);
        }
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

    private void downloadFile(SftpConnection sftpConnection, SftpFile batchFile) throws Exception
    {
        SftpHelper.downloadFile(sftpConnection, batchFile.getRemoteFilePath(), batchFile.getLocalFilePath());

        batchFile.setLocalFileSizeBytes(getFileSizeBytes(batchFile.getLocalFilePath()));

        db.setFileAsDownloaded(batchFile);
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

    private void notifyOnwardPipeline()
    {
        try
        {
            //List<Batch> unnotifiedBatches = db.getUnnotifiedBatches(dbConfiguration.getInstanceId());

        }
        catch (Exception e)
        {
            LOG.info("Exception while notifying onward pipeline", e);
        }
    }
}
