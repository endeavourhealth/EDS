package org.endeavourhealth.sftpreader.model.db;

import java.util.List;

public class DbConfiguration
{
    private String instanceId;
    private String instanceDescription;
    private String interfaceTypeName;
    private int pollFrequencySeconds;
    private String localRootPath;
    private DbConfigurationSftp dbConfigurationSftp;
    private DbConfigurationPgp dbConfigurationPgp;
    private DbConfigurationEds dbConfigurationEds;
    private List<DbConfigurationKvp> dbConfigurationKvp;
    private List<String> interfaceFileTypes;

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

    public String getInterfaceTypeName()
    {
        return interfaceTypeName;
    }

    public DbConfiguration setInterfaceTypeName(String interfaceTypeName)
    {
        this.interfaceTypeName = interfaceTypeName;
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

    public List<String> getInterfaceFileTypes()
    {
        return this.interfaceFileTypes;
    }

    public DbConfiguration setInterfaceFileTypes(List<String> interfaceFileTypes)
    {
        this.interfaceFileTypes = interfaceFileTypes;
        return this;
    }

    public DbConfigurationEds getDbConfigurationEds()
    {
        return this.dbConfigurationEds;
    }

    public DbConfiguration setDbConfigurationEds(DbConfigurationEds dbConfigurationEds)
    {
        this.dbConfigurationEds = dbConfigurationEds;
        return this;
    }

    public List<DbConfigurationKvp> getDbConfigurationKvp()
    {
        return this.dbConfigurationKvp;
    }

    public DbConfiguration setDbConfigurationKvp(List<DbConfigurationKvp> dbConfigurationKvp)
    {
        this.dbConfigurationKvp = dbConfigurationKvp;
        return this;
    }
}
