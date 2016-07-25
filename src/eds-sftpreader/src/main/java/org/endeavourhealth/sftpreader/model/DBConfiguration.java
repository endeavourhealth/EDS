package org.endeavourhealth.sftpreader.model;

public class DBConfiguration
{
    private String instanceId;
    private String hostname;
    private int port;
    private String remotePath;
    private String username;
    private String clientPrivateKey;
    private String clientPrivateKeyPassword;
    private String hostPublicKey;

    public String getInstanceId()
    {
        return instanceId;
    }

    public DBConfiguration setInstanceId(String instanceId)
    {
        this.instanceId = instanceId;
        return this;
    }

    public String getHostname()
    {
        return hostname;
    }

    public DBConfiguration setHostname(String hostname)
    {
        this.hostname = hostname;
        return this;
    }

    public int getPort()
    {
        return port;
    }

    public DBConfiguration setPort(int port)
    {
        this.port = port;
        return this;
    }

    public String getRemotePath()
    {
        return remotePath;
    }

    public DBConfiguration setRemotePath(String remotePath)
    {
        this.remotePath = remotePath;
        return this;
    }

    public String getUsername()
    {
        return username;
    }

    public DBConfiguration setUsername(String username)
    {
        this.username = username;
        return this;
    }

    public String getClientPrivateKey()
    {
        return clientPrivateKey;
    }

    public DBConfiguration setClientPrivateKey(String clientPrivateKey)
    {
        this.clientPrivateKey = clientPrivateKey;
        return this;
    }

    public String getClientPrivateKeyPassword()
    {
        return clientPrivateKeyPassword;
    }

    public DBConfiguration setClientPrivateKeyPassword(String clientPrivateKeyPassword)
    {
        this.clientPrivateKeyPassword = clientPrivateKeyPassword;
        return this;
    }

    public String getHostPublicKey()
    {
        return hostPublicKey;
    }

    public DBConfiguration setHostPublicKey(String hostPublicKey)
    {
        this.hostPublicKey = hostPublicKey;
        return this;
    }
}
