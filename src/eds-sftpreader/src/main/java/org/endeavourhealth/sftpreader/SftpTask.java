package org.endeavourhealth.sftpreader;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.apache.commons.io.FilenameUtils;
import org.endeavourhealth.core.configuration.SftpReaderConfiguration;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpConnection;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpConnectionDetails;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpConnectionException;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpRemoteFile;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.TimerTask;

public class SftpTask extends TimerTask
{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SftpTask.class);

    private SftpReaderConfiguration configuration;

    public SftpTask(SftpReaderConfiguration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public void run()
    {
        receiveAndProcessFiles();
    }

    private void receiveAndProcessFiles()
    {
        SftpConnection sftpConnection = createConnection();

        try
        {
            openConnection(sftpConnection);

            for (SftpRemoteFile sftpRemoteFile : getFileList(sftpConnection))
            {
                processFile(sftpConnection, sftpRemoteFile);
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

    private void processFile(SftpConnection sftpConnection, SftpRemoteFile sftpRemoteFile) throws SftpException, IOException
    {
        LOG.info("Processing file " + sftpRemoteFile.getFilename());

        LOG.info(" Downloading file");

        InputStream inputStream = sftpConnection.getFile(sftpRemoteFile.getFullPath());

        String localPath = FilenameUtils.concat(configuration.getLocalPath(), sftpRemoteFile.getFilename());
        Files.copy(inputStream, new File(localPath).toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private SftpConnection createConnection()
    {
        return new SftpConnection(getSftpConnectionDetails());
    }

    private void openConnection(SftpConnection sftpConnection) throws SftpConnectionException, JSchException, IOException
    {
        String hostname = sftpConnection.getConnectionDetails().getHostname();
        String port = Integer.toString(sftpConnection.getConnectionDetails().getPort());
        String username = sftpConnection.getConnectionDetails().getUsername();

        LOG.info("Opening SFTP connection to " + hostname + " on port " + port + " with user " + username);

        sftpConnection.open();
    }

    private void closeConnection(SftpConnection sftpConnection)
    {
        LOG.info("Closing SFTP connection");

        sftpConnection.close();
    }

    private List<SftpRemoteFile> getFileList(SftpConnection sftpConnection) throws SftpException
    {
        LOG.info("Get file list at " + configuration.getRemotePath());

        List<SftpRemoteFile> fileList = sftpConnection.getFileList(configuration.getRemotePath());

        LOG.info("Found " + Integer.toString(fileList.size()) + " files");

        return fileList;
    }

    private SftpConnectionDetails getSftpConnectionDetails()
    {
        return new SftpConnectionDetails()
                .setHostname(this.configuration.getHost())
                .setPort(this.configuration.getPort())
                .setUsername(this.configuration.getCredentials().getUsername())
                .setClientPrivateKeyFilePath(this.configuration.getCredentials().getClientPrivateKeyFilePath())
                .setClientPrivateKeyPassword(this.configuration.getCredentials().getClientPrivateKeyPassword())
                .setHostPublicKeyFilePath(this.configuration.getCredentials().getHostPublicKeyFilePath());
    }


//    private void decryptFile(String inputFilePath, String outputFilePath) throws Exception
//    {
//        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//
//        String pgpSecretKey = Resources.getResource("test-pgp-endeavour-private.asc").getPath();
//        String pgpPublicKey = Resources.getResource("test-pgp-emis-public.asc").getPath();
//
//        PgpUtil.decryptAndVerify(inputFilePath, pgpPublicKey, pgpSecretKey, "password", outputFilePath);
//    }
}
