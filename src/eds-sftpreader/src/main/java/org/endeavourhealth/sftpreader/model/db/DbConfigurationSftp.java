package org.endeavourhealth.sftpreader.model.db;

public class DbConfigurationSftp
{
    private String hostname;
    private int port;
    private String remotePath;
    private String username;
    private String clientPrivateKey;
    private String clientPrivateKeyPassword;
    private String hostPublicKey;

    public String getHostname()
    {
        return hostname;
    }

    public DbConfigurationSftp setHostname(String hostname)
    {
        this.hostname = hostname;
        return this;
    }

    public int getPort()
    {
        return port;
    }

    public DbConfigurationSftp setPort(int port)
    {
        this.port = port;
        return this;
    }

    public String getRemotePath()
    {
        return remotePath;
    }

    public DbConfigurationSftp setRemotePath(String remotePath)
    {
        this.remotePath = remotePath;
        return this;
    }

    public String getUsername()
    {
        return username;
    }

    public DbConfigurationSftp setUsername(String username)
    {
        this.username = username;
        return this;
    }

    public String getClientPrivateKey()
    {
        return clientPrivateKey;
    }

    public DbConfigurationSftp setClientPrivateKey(String clientPrivateKey)
    {
        this.clientPrivateKey = clientPrivateKey;
        return this;
    }

    public String getClientPrivateKeyPassword()
    {
        return clientPrivateKeyPassword;
    }

    public DbConfigurationSftp setClientPrivateKeyPassword(String clientPrivateKeyPassword)
    {
        this.clientPrivateKeyPassword = clientPrivateKeyPassword;
        return this;
    }

    public String getHostPublicKey()
    {
        return hostPublicKey;
    }

    public DbConfigurationSftp setHostPublicKey(String hostPublicKey)
    {
        this.hostPublicKey = hostPublicKey;
        return this;
    }
}
