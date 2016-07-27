package org.endeavourhealth.sftpreader;

import com.google.common.io.Resources;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.openpgp.PGPException;
import org.endeavourhealth.sftpreader.model.db.DbConfiguration;
import org.endeavourhealth.sftpreader.model.db.DbConfigurationSftp;
import org.endeavourhealth.sftpreader.utilities.pgp.PgpUtil;
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
import java.nio.file.Paths;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SftpTask extends TimerTask
{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SftpTask.class);

    private Configuration configuration = null;
    private DbConfiguration dbConfiguration = null;
    private DataLayer dataLayer = null;

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
            receiveAndProcessFiles();
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
        this.dataLayer = new DataLayer(configuration.getDatabaseConnection());
    }

    private void receiveAndProcessFiles()
    {
        SftpConnection sftpConnection = null;

        try
        {
            String destinationPath = dbConfiguration.getLocalRootPath();
            String sessionPath = createSessionDirectory(dbConfiguration.getLocalRootPath());

            sftpConnection = openSftpConnection();

            for (SftpRemoteFile sftpRemoteFile : getFileList(sftpConnection))
            {
                LOG.info(">Start Processing file " + sftpRemoteFile.getFilename());

                String remoteFilePath = sftpRemoteFile.getFullPath();
                String localFilePath = FilenameUtils.concat(sessionPath, sftpRemoteFile.getFilename());

                downloadFile(sftpConnection, remoteFilePath, localFilePath);

                if (doesFileNeedDecrypting(localFilePath))
                {
                    String decryptedLocalFilePath = getDecryptedFilePath(localFilePath);

                    decryptFile(localFilePath, decryptedLocalFilePath);

                    moveFileToDestination(decryptedLocalFilePath, destinationPath);
                }
                else
                {
                    copyFileToDestination(localFilePath, destinationPath);
                }

                recordFileInDatabase(sftpRemoteFile.getFilename(), destinationPath, 0);

                //deleteRemoteFile(sftpConnection, remoteFilePath);



                LOG.info(">End Processing file " + sftpRemoteFile.getFilename());
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

    private void recordFileInDatabase(String filename, String filePath, long fileSize) throws PgStoredProcException
    {
        String instanceId = configuration.getInstanceId();
        String fileSetIdentifier = "aaaa";

        this.dataLayer.addFile(instanceId, fileSetIdentifier, filename, filePath, fileSize);
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



    private boolean doesFileNeedDecrypting(String localFilePath)
    {
        if (dbConfiguration.getDbConfigurationPgp() != null)
            if (localFilePath.endsWith(dbConfiguration.getDbConfigurationPgp().getPgpFileExtensionFilter()))
                return true;

        return false;
    }

    private String getDecryptedFilePath(String localFilePath)
    {
        return StringUtils.removeEnd(localFilePath, dbConfiguration.getDbConfigurationPgp().getPgpFileExtensionFilter());
    }

    private void copyFileToDestination(String sourceFilePath, String destinationDirectory) throws IOException
    {
        String destinationFilePath = FilenameUtils.concat(destinationDirectory, sourceFilePath);

        LOG.info("Copy file " + sourceFilePath + " to " + destinationFilePath);

        File sourceFile = new File(sourceFilePath);
        File destinationFile = new File(destinationFilePath);

        Files.copy(sourceFile.toPath(), destinationFile.toPath());
    }

    private void moveFileToDestination(String sourceFilePath, String destinationDirectory) throws IOException
    {
        String destinationFilePath = FilenameUtils.concat(destinationDirectory, FilenameUtils.getName(sourceFilePath));

        LOG.info("Move file " + sourceFilePath + " to " + destinationFilePath);

        File sourceFile = new File(sourceFilePath);
        File destinationFile = new File(destinationFilePath);

        Files.move(sourceFile.toPath(), destinationFile.toPath());
    }

    private void deleteRemoteFile(SftpConnection sftpConnection, String remoteFilePath) throws SftpException
    {
        LOG.info("Deleting remote file " + remoteFilePath);

        sftpConnection.deleteFile(remoteFilePath);
    }

    private void decryptFile(String localFilePath, String decryptedLocalFilePath) throws PGPException, SignatureException, NoSuchProviderException, IOException
    {
        LOG.info("Decrypting file " + localFilePath);

        String senderPublicKey = dbConfiguration.getDbConfigurationPgp().getPgpSenderPublicKey();
        String recipientPrivateKey = dbConfiguration.getDbConfigurationPgp().getPgpRecipientPrivateKey();
        String recipientPrivateKeyPassword = dbConfiguration.getDbConfigurationPgp().getPgpRecipientPrivateKeyPassword();

        PgpUtil.decryptAndVerify(localFilePath, senderPublicKey, recipientPrivateKey, recipientPrivateKeyPassword, decryptedLocalFilePath);
    }

    private String createSessionDirectory(String localPath) throws IOException
    {
        String dateTime = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MMM-dd HH.mm.ss.SSS 'UTC'"));

        String sessionDirectoryName = FilenameUtils.concat(localPath, dateTime);

        LOG.info("Creating session directory " + sessionDirectoryName);

        File sessionDirectory = new File(sessionDirectoryName);

        if (sessionDirectory.exists())
            throw new IOException("Session directory already exists");

        if (!sessionDirectory.mkdir())
            throw new IOException("Could not create session directory");

        return sessionDirectory.getAbsolutePath();
    }

    private void downloadFile(SftpConnection sftpConnection, String remoteFilePath, String localFilePath) throws SftpException, IOException
    {
        LOG.info("Downloading file");

        File temporaryDownloadFile = new File(localFilePath + ".download");

        if (temporaryDownloadFile.exists())
            throw new IOException("Temporary download file " + temporaryDownloadFile + " already exists");

        InputStream inputStream = sftpConnection.getFile(remoteFilePath);

        Files.copy(inputStream, temporaryDownloadFile.toPath());

        if (!temporaryDownloadFile.renameTo(new File(localFilePath)))
            throw new IOException("Could not temporary download file to " + localFilePath);
    }

    private SftpConnection openSftpConnection() throws SftpConnectionException, JSchException, IOException
    {
        SftpConnection sftpConnection = new SftpConnection(getSftpConnectionDetails());

        String hostname = sftpConnection.getConnectionDetails().getHostname();
        String port = Integer.toString(sftpConnection.getConnectionDetails().getPort());
        String username = sftpConnection.getConnectionDetails().getUsername();

        LOG.info("Opening SFTP connection to " + hostname + " on port " + port + " with user " + username);

        sftpConnection.open();

        return sftpConnection;
    }

    private void closeConnection(SftpConnection sftpConnection)
    {
        LOG.info("Closing SFTP connection");

        sftpConnection.close();
    }

    private List<SftpRemoteFile> getFileList(SftpConnection sftpConnection) throws SftpException
    {
        String remotePath = dbConfiguration.getDbConfigurationSftp().getRemotePath();

        LOG.info("Get file list at " + remotePath);

        List<SftpRemoteFile> fileList = sftpConnection.getFileList(remotePath);

        LOG.info("Found " + Integer.toString(fileList.size()) + " files");

        return fileList;
    }

    private SftpConnectionDetails getSftpConnectionDetails()
    {
        DbConfigurationSftp configurationSftp = dbConfiguration.getDbConfigurationSftp();

        return new SftpConnectionDetails()
                .setHostname(configurationSftp.getHostname())
                .setPort(configurationSftp.getPort())
                .setUsername(configurationSftp.getUsername())
                .setClientPrivateKey(configurationSftp.getClientPrivateKey())
                .setClientPrivateKeyPassword(configurationSftp.getClientPrivateKeyPassword())
                .setHostPublicKey(configurationSftp.getHostPublicKey());
    }

    private static String resolveFilePath(String filePath)
    {
        if (!Files.exists(Paths.get(filePath)))
            return Resources.getResource(filePath).getPath();

        return filePath;
    }
}
