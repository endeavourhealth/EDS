package org.endeavourhealth.sftpreader;

import org.endeavourhealth.core.configuration.SftpReaderConfiguration;
import org.endeavourhealth.core.engineConfiguration.EngineConfigurationSerializer;
import org.endeavourhealth.core.utility.XmlSerializer;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public final class Configuration
{
    // class members //
    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);
    private static final String CONFIG_XSD = "SftpReaderConfiguration.xsd";
    private static final String CONFIG_RESOURCE = "SftpReaderConfiguration.xml";

    private static Configuration instance = null;

    public static Configuration getInstance() throws Exception
    {
        if (instance == null)
            instance = new Configuration();

        return instance;
    }

    // instance members //
    private SftpReaderConfiguration localConfiguration;

    private Configuration() throws Exception
    {
        loadLocalConfiguration();
    }

    private void loadLocalConfiguration() throws Exception
    {
        LOG.info("Loading local configuration file from resource " + CONFIG_RESOURCE);
        localConfiguration = XmlSerializer.deserializeFromResource(SftpReaderConfiguration.class, CONFIG_RESOURCE, CONFIG_XSD);
    }

    public DataSource getDataSource()
    {
        PGSimpleDataSource pgSimpleDataSource = new PGSimpleDataSource();
        pgSimpleDataSource.setServerName(localConfiguration.getPostgresConnetion().getHostname());
        pgSimpleDataSource.setPortNumber(localConfiguration.getPostgresConnetion().getPort().intValue());
        pgSimpleDataSource.setDatabaseName(localConfiguration.getPostgresConnetion().getDatabase());
        pgSimpleDataSource.setUser(localConfiguration.getPostgresConnetion().getUsername());
        pgSimpleDataSource.setPassword(localConfiguration.getPostgresConnetion().getPassword());
        return pgSimpleDataSource;
    }

    public String getInstanceId()
    {
        return localConfiguration.getInstanceId();
    }

    public SftpReaderConfiguration getLocalConfiguration()
    {
        return localConfiguration;
    }

    public void initialiseEngineConfiguration(String[] args) throws Exception
    {
        EngineConfigurationSerializer.loadConfigFromArgIfPossible(args, 1);
    }
}
