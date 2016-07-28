package org.endeavourhealth.sftpreader.model.db;

public class DbConfiguration
{
    private String instanceId;
    private String instanceDescription;
    private int batchTypeId;
    private String batchTypeDescription;
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

    public String getInstanceDescription()
    {
        return instanceDescription;
    }

    public DbConfiguration setInstanceDescription(String instanceDescription)
    {
        this.instanceDescription = instanceDescription;
        return this;
    }

    public int getBatchTypeId()
    {
        return batchTypeId;
    }

    public DbConfiguration setBatchTypeId(int batchTypeId)
    {
        this.batchTypeId = batchTypeId;
        return this;
    }

    public String getBatchTypeDescription()
    {
        return batchTypeDescription;
    }

    public DbConfiguration setBatchTypeDescription(String batchTypeDescription)
    {
        this.batchTypeDescription = batchTypeDescription;
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

    public String getPgpFileExtensionFilter()
    {
        if (this.getDbConfigurationPgp() == null)
            return null;

        return this.getDbConfigurationPgp().getPgpFileExtensionFilter();
    }
}
