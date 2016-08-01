package org.endeavourhealth.sftpreader;

import org.endeavourhealth.sftpreader.utilities.XmlSerializer;
import org.endeavourhealth.sftpreader.model.db.DbConfiguration;
import org.endeavourhealth.sftpreader.model.xml.DatabaseConnection;
import org.endeavourhealth.sftpreader.model.xml.SftpReaderConfiguration;
import org.endeavourhealth.sftpreader.utilities.postgres.PgStoredProcException;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;

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
    private DbConfiguration dbConfiguration;

    private Configuration() throws Exception
    {
        loadLocalConfiguration();
        loadDbConfiguration();
    }

    private void loadLocalConfiguration() throws Exception
    {
        LOG.info("Loading local configuration file from resource " + CONFIG_RESOURCE);
        localConfiguration = XmlSerializer.deserializeFromResource(SftpReaderConfiguration.class, CONFIG_RESOURCE, CONFIG_XSD);
    }

    private void loadDbConfiguration() throws PgStoredProcException, SQLException
    {
        DataLayer dataLayer = new DataLayer(getDatabaseConnection());
        this.dbConfiguration = dataLayer.getConfiguration(getInstanceId());
    }

    public DataSource getDatabaseConnection() throws SQLException
    {
        return getDataSource(localConfiguration.getDatabaseConnections().getSftpReader());
    }

    private static DataSource getDataSource(DatabaseConnection databaseConnection) throws SQLException
    {
        PGSimpleDataSource pgSimpleDataSource = new PGSimpleDataSource();
        pgSimpleDataSource.setServerName(databaseConnection.getHostname());
        pgSimpleDataSource.setPortNumber(databaseConnection.getPort().intValue());
        pgSimpleDataSource.setDatabaseName(databaseConnection.getDatabase());
        pgSimpleDataSource.setUser(databaseConnection.getUsername());
        pgSimpleDataSource.setPassword(databaseConnection.getPassword());
        return pgSimpleDataSource;
        //return DataSources.pooledDataSource(pgSimpleDataSource);
    }

    public String getInstanceId()
    {
        return localConfiguration.getInstanceId();
    }

    public SftpReaderConfiguration getLocalConfiguration()
    {
        return this.localConfiguration;
    }

    public DbConfiguration getDbConfiguration()
    {
        return this.dbConfiguration;
    }
}
