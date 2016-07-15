package org.endeavourhealth.sftpreader;

import com.google.common.io.Resources;
import org.endeavourhealth.core.configuration.SftpReaderConfiguration;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpConnection;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpConnectionDetails;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpRemoteFile;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
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
        while (true)
        {
            int filesReceived = receiveAndProcessFiles();

            if (filesReceived == 0)
                break;
        }
    }

    private int receiveAndProcessFiles()
    {
        int fileAvailable = 0;

        try (SftpConnection sftpConnection = new SftpConnection(getSftpConnectionDetails()))
        {
            List<SftpRemoteFile> sftpRemoteFiles = sftpConnection.getFileList(configuration.getRemotePath());

            fileAvailable = sftpRemoteFiles.size();

            for (SftpRemoteFile sftpRemoteFile : sftpRemoteFiles)
            {
                LOG.info("Found " + sftpRemoteFile.getFilename());
            }
        }
        catch (Exception e)
        {

        }

        return fileAvailable;
    }

    private SftpConnectionDetails getSftpConnectionDetails()
    {
        return new SftpConnectionDetails()
                .setHostname(this.configuration.getHost())
                .setPort(this.configuration.getPort())
                .setUsername(this.configuration.getCredentials().getUsername())
                .setClientPrivateKeyFilePath(resolveFilePath(this.configuration.getCredentials().getClientPrivateKeyFilePath()))
                .setClientPrivateKeyPassword(this.configuration.getCredentials().getClientPrivateKeyPassword())
                .setHostPublicKeyFilePath(resolveFilePath(this.configuration.getCredentials().getHostPublicKeyFilePath()));
    }

    private String resolveFilePath(String filePath)
    {
        if (!Files.exists(Paths.get(filePath)))
            return Resources.getResource(filePath).getPath();

        return filePath;
    }

//    private void downloadFile()
//    {
//          String localPath = FilenameUtils.concat(configuration.getLocalPath(), file.getFilename());
//          Files.copy(inputStream, new File(localPath).toPath(), StandardCopyOption.REPLACE_EXISTING);
//    }
//
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
