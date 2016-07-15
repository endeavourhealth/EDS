package org.endeavourhealth.sftpreader.utilities.sftp;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class SftpConnectionDetails
{
    private String hostname;
    private int port;
    private String username;
    private String clientPrivateKeyFilePath;
    private String clientPrivateKeyPassword;
    private String hostPublicKeyFilePath;

    public String getHostname()
    {
        return hostname;
    }

    public SftpConnectionDetails setHostname(String hostname)
    {
        this.hostname = hostname;
        return this;
    }

    public int getPort()
    {
        return port;
    }

    public SftpConnectionDetails setPort(int port)
    {
        this.port = port;
        return this;
    }

    public String getUsername()
    {
        return username;
    }

    public SftpConnectionDetails setUsername(String username)
    {
        this.username = username;
        return this;
    }

    public String getClientPrivateKeyFilePath()
    {
        return clientPrivateKeyFilePath;
    }

    public SftpConnectionDetails setClientPrivateKeyFilePath(String clientPrivateKeyFilePath)
    {
        this.clientPrivateKeyFilePath = clientPrivateKeyFilePath;
        return this;
    }

    public String getClientPrivateKeyPassword()
    {
        return clientPrivateKeyPassword;
    }

    public SftpConnectionDetails setClientPrivateKeyPassword(String clientPrivateKeyPassword)
    {
        this.clientPrivateKeyPassword = clientPrivateKeyPassword;
        return this;
    }

    public String getHostPublicKeyFilePath()
    {
        return hostPublicKeyFilePath;
    }

    public SftpConnectionDetails setHostPublicKeyFilePath(String hostPublicKeyFilePath)
    {
        this.hostPublicKeyFilePath = hostPublicKeyFilePath;
        return this;
    }

    public String getKnownHostsString() throws IOException
    {
        String hostPublicKey = FileUtils.readFileToString(new File(this.hostPublicKeyFilePath));
        return this.getHostname() + " " + hostPublicKey;
    }
}
