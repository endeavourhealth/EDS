package org.endeavourhealth.sftpreader.utilities.sftp;

import com.jcraft.jsch.*;
import org.apache.commons.lang3.Validate;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class SftpConnection
{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SftpConnection.class);

    private SftpConnectionDetails connectionDetails;
    private JSch jSch;
    private Session session;
    private ChannelSftp channel;

    public SftpConnection(SftpConnectionDetails connectionDetails)
    {
        Validate.notEmpty(connectionDetails.getHostname(), "hostname is empty");
        Validate.notEmpty(connectionDetails.getUsername(), "username is empty");
        Validate.isTrue(connectionDetails.getPort() > 0, "port must be positive");

        this.connectionDetails = connectionDetails;
    }

    public void open() throws JSchException, IOException, SftpConnectionException
    {
        this.jSch = new JSch();

        jSch.addIdentity("client-private-key", this.connectionDetails.getClientPrivateKey().getBytes(), null, this.connectionDetails.getClientPrivateKeyPassword().getBytes());
        jSch.setKnownHosts(new ByteArrayInputStream(this.connectionDetails.getKnownHostsString().getBytes()));

        this.session = jSch.getSession(connectionDetails.getUsername(), connectionDetails.getHostname(), connectionDetails.getPort());

        this.session.connect();

        this.channel = (ChannelSftp)session.openChannel("sftp");
        this.channel.connect();
    }

    @SuppressWarnings("unchecked")
    public List<SftpRemoteFile> getFileList(String remotePath) throws SftpException
    {
        Vector<ChannelSftp.LsEntry> fileList = channel.ls(remotePath);

        return fileList
                .stream()
                .filter(t -> !t.getAttrs().isDir())
                .map(t ->
                        new SftpRemoteFile(t.getFilename(),
                                remotePath,
                                t.getAttrs().getSize(),
                                LocalDateTime.ofInstant(new Date((long)t.getAttrs().getMTime() * 1000L).toInstant(), ZoneId.systemDefault())
                        )
                )
                .collect(Collectors.toList());
    }

    public InputStream getFile(String remotePath) throws SftpException
    {
        return channel.get(remotePath);
    }

    public void deleteFile(String remotePath) throws SftpException
    {
        channel.rm(remotePath);
    }

    public void cd(String remotePath) throws SftpException {
        channel.cd(remotePath);
    }

    public void put(String localPath, String destinationPath) throws SftpException {
        channel.put(localPath, destinationPath);
    }

    public void mkDir(String path) throws SftpException {
        channel.mkdir(path);
    }

    public void close()
    {
        if (channel != null && channel.isConnected())
            channel.disconnect();

        if (session != null && session.isConnected())
            session.disconnect();
    }

    public SftpConnectionDetails getConnectionDetails()
    {
        return connectionDetails;
    }
}
