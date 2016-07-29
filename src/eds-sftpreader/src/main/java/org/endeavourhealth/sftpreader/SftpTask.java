package org.endeavourhealth.sftpreader;

import com.jcraft.jsch.SftpException;
import org.endeavourhealth.sftpreader.batchFileImplementations.BatchFile;
import org.endeavourhealth.sftpreader.batchFileImplementations.BatchFileFactory;
import org.endeavourhealth.sftpreader.model.db.AddFileResult;
import org.endeavourhealth.sftpreader.model.db.DbConfiguration;
import org.endeavourhealth.sftpreader.utilities.pgp.PgpUtil;
import org.endeavourhealth.sftpreader.utilities.postgres.PgStoredProcException;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpConnection;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpRemoteFile;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
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
            validateBatches();
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

                BatchFile batchFile = instantiateBatchFile(sftpRemoteFile);

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

    private BatchFile instantiateBatchFile(SftpRemoteFile sftpRemoteFile)
    {
        return BatchFileFactory.create(sftpRemoteFile,
                dbConfiguration.getLocalRootPath(),
                dbConfiguration.getPgpFileExtensionFilter(),
                dbConfiguration.getInterfaceFileTypes());
    }

    private void createBatchDirectory(BatchFile batchFile) throws IOException
    {
        File localPath = new File(batchFile.getLocalPath());

        if (!localPath.exists())
            if (!localPath.mkdirs())
                throw new IOException("Could not create path " + localPath);
    }

    private void downloadFile(SftpConnection sftpConnection, BatchFile batchFile) throws Exception
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

    private void decryptFile(BatchFile batchFile) throws Exception
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

    private void validateBatches() throws PgStoredProcException
    {
        db.getIncompleteBatches(dbConfiguration.getInstanceId());
    }

    private void notifyOnwardPipeline()
    {
        try
        {

        }
        catch (Exception e)
        {
            LOG.info("Exception while notifying onward pipeline", e);
        }
    }
}
