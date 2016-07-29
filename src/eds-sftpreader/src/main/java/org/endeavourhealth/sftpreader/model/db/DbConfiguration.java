package org.endeavourhealth.sftpreader.model.db;

import java.util.List;

public class DbConfiguration
{
    private String instanceId;
    private String instanceDescription;
    private int interfaceTypeId;
    private String interfaceTypeDescription;
    private int pollFrequencySeconds;
    private String localRootPath;
    private DbConfigurationSftp dbConfigurationSftp;
    private DbConfigurationPgp dbConfigurationPgp;
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

    public int getInterfaceTypeId()
    {
        return interfaceTypeId;
    }

    public DbConfiguration setInterfaceTypeId(int interfaceTypeId)
    {
        this.interfaceTypeId = interfaceTypeId;
        return this;
    }

    public String getInterfaceTypeDescription()
    {
        return interfaceTypeDescription;
    }

    public DbConfiguration setInterfaceTypeDescription(String interfaceTypeDescription)
    {
        this.interfaceTypeDescription = interfaceTypeDescription;
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
}
