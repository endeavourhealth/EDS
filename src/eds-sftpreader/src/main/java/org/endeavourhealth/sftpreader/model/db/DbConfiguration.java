package org.endeavourhealth.sftpreader.model.db;

public class DbConfiguration
{
    private String instanceId;
    private int pollFrequencySeconds;
    private String localRootPath;
    private DbConfigurationSftp dbConfigurationSftp;
    private DbConfigurationPgp dbConfigurationPgp;

    public String getInstanceId()
    {
        return instanceId;
    }

    public DbConfiguration setInstanceId(String instanceId)
    {
        this.instanceId = instanceId;
        return this;
    }

    public int getPollFrequencySeconds()
    {
        return pollFrequencySeconds;
    }

    public DbConfiguration setPollFrequencySeconds(int pollFrequencySeconds)
    {
        this.pollFrequencySeconds = pollFrequencySeconds;
        return this;
    }

    public String getLocalRootPath()
    {
        return localRootPath;
    }

    public DbConfiguration setLocalRootPath(String localRootPath)
    {
        this.localRootPath = localRootPath;
        return this;
    }

    public DbConfigurationPgp getDbConfigurationPgp() { return this.dbConfigurationPgp; }

    public DbConfiguration setDbConfigurationPgp(DbConfigurationPgp dbConfigurationPgp)
    {
        this.dbConfigurationPgp = dbConfigurationPgp;
        return this;
    }

    public DbConfigurationSftp getDbConfigurationSftp() { return this.dbConfigurationSftp; }

    public DbConfiguration setDbConfigurationSftp(DbConfigurationSftp dbConfigurationSftp)
    {
        this.dbConfigurationSftp = dbConfigurationSftp;
        return this;
    }
}
