package org.endeavourhealth.sftpreader.utilities.sftp;

import java.io.IOException;

public class SftpConnectionDetails
{
    private String hostname;
    private int port;
    private String username;
    private String clientPrivateKey;
    private String clientPrivateKeyPassword;
    private String hostPublicKey;

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

    public String getClientPrivateKey()
    {
        return clientPrivateKey;
    }

    public SftpConnectionDetails setClientPrivateKey(String clientPrivateKey)
    {
        this.clientPrivateKey = clientPrivateKey;
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

    public String getHostPublicKey()
    {
        return hostPublicKey;
    }

    public SftpConnectionDetails setHostPublicKey(String hostPublicKey)
    {
        this.hostPublicKey = hostPublicKey;
        return this;
    }

    public String getKnownHostsString() throws IOException
    {
        return this.getHostname() + " " + hostPublicKey + "\n";
    }
}
