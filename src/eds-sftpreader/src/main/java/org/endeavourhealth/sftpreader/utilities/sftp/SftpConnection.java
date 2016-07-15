package org.endeavourhealth.sftpreader.utilities.sftp;

import com.jcraft.jsch.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class SftpConnection implements AutoCloseable
{
    private SftpConnectionDetails connectionDetails;
    private JSch jSch;
    private Session session;
    private ChannelSftp channel;
    private boolean connectionOpen = false;

    public SftpConnection(SftpConnectionDetails connectionDetails) throws JSchException, IOException
    {
        this.connectionDetails = connectionDetails;

        this.jSch = new JSch();

        jSch.addIdentity(this.connectionDetails.getClientPrivateKeyFilePath(), this.connectionDetails.getClientPrivateKeyPassword());
        jSch.setKnownHosts(new ByteArrayInputStream(this.connectionDetails.getKnownHostsString().getBytes()));

        this.session = jSch.getSession(connectionDetails.getUsername(), connectionDetails.getHostname(), connectionDetails.getPort());
        this.session.connect();

        this.channel = (ChannelSftp)session.openChannel("sftp");
        this.channel.connect();
    }

    public List<SftpRemoteFile> getFileList(String remotePath) throws SftpException
    {
        Vector<ChannelSftp.LsEntry> fileList = channel.ls(remotePath);

        return fileList
                .stream()
                .map(t -> new SftpRemoteFile(t.getFilename()))
                .collect(Collectors.toList());
    }

    public InputStream getFile(String remotePath) throws SftpException
    {
        return channel.get(remotePath);
    }

    @Override
    public void close()
    {
        if (channel != null && channel.isConnected())
            channel.disconnect();

        if (session != null && session.isConnected())
            session.disconnect();
    }
}
