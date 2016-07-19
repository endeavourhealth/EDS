package org.endeavourhealth.sftpreader.utilities.sftp;

import com.google.common.base.Preconditions;
import com.jcraft.jsch.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        Preconditions.checkArgument(!StringUtils.isEmpty(connectionDetails.getHostname()), "hostname is empty");
        Preconditions.checkArgument(!StringUtils.isEmpty(connectionDetails.getUsername()), "username is empty");
        Preconditions.checkArgument(connectionDetails.getPort() > 0, "port must be positive");

        this.connectionDetails = connectionDetails;
    }

    public void open() throws JSchException, IOException, SftpConnectionException
    {
        this.jSch = new JSch();

        jSch.addIdentity(this.connectionDetails.getClientPrivateKeyFilePath(), this.connectionDetails.getClientPrivateKeyPassword());
        jSch.setKnownHosts(new ByteArrayInputStream(this.connectionDetails.getKnownHostsString().getBytes()));

        this.session = jSch.getSession(connectionDetails.getUsername(), connectionDetails.getHostname(), connectionDetails.getPort());
        this.session.connect();

        this.channel = (ChannelSftp)session.openChannel("sftp");
        this.channel.connect();

        LOG.info("Connection opened");
    }

    public List<SftpRemoteFile> getFileList(String remotePath) throws SftpException
    {
        Vector<ChannelSftp.LsEntry> fileList = channel.ls(remotePath);

        return fileList
                .stream()
                .filter(t -> !FilenameUtils.getName(t.getFilename()).equals("."))
                .filter(t -> !FilenameUtils.getName(t.getFilename()).equals(".."))
                .map(t -> new SftpRemoteFile(t.getFilename(), remotePath))
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
