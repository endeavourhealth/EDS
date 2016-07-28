package org.endeavourhealth.sftpreader;

import com.jcraft.jsch.IO;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.endeavourhealth.sftpreader.model.db.DbConfigurationSftp;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpConnection;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpConnectionDetails;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpConnectionException;
import org.endeavourhealth.sftpreader.utilities.sftp.SftpRemoteFile;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

public class SftpHelper
{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SftpHelper.class);

    public static SftpConnection openSftpConnection(DbConfigurationSftp configurationSftp) throws SftpConnectionException, JSchException, IOException
    {
        SftpConnection sftpConnection = new SftpConnection(getSftpConnectionDetails(configurationSftp));

        String hostname = sftpConnection.getConnectionDetails().getHostname();
        String port = Integer.toString(sftpConnection.getConnectionDetails().getPort());
        String username = sftpConnection.getConnectionDetails().getUsername();

        LOG.info("Opening SFTP connection to " + hostname + " on port " + port + " with user " + username);

        sftpConnection.open();

        return sftpConnection;
    }

    public static List<SftpRemoteFile> getFileList(SftpConnection sftpConnection, String remotePath) throws SftpException
    {
        LOG.info("Get file list at " + remotePath);

        List<SftpRemoteFile> fileList = sftpConnection.getFileList(remotePath);

        LOG.info("Found " + Integer.toString(fileList.size()) + " files");

        return fileList;
    }

    public static void downloadFile(SftpConnection sftpConnection, String remoteFilePath, String localFilePath) throws SftpException, IOException
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

    public static void closeConnection(SftpConnection sftpConnection)
    {
        LOG.info("Closing SFTP connection");

        sftpConnection.close();
    }
}
