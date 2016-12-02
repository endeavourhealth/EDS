package org.endeavourhealth.hl7receiver;

import org.endeavourhealth.hl7receiver.model.xml.Hl7ReceiverConfiguration;
import org.endeavourhealth.utilities.postgres.PgStoredProcException;
import org.endeavourhealth.utilities.xml.XmlSerializer;
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
    private Hl7ReceiverConfiguration localConfiguration;
    //private DbConfiguration dbConfiguration;

    private Configuration() throws Exception
    {
        loadLocalConfiguration();
        loadDbConfiguration();
    }

    private void loadLocalConfiguration() throws Exception
    {
        String path = System.getProperty("sftpreader.configurationFile");

        if (path != null)
        {
            LOG.info("Loading local configuration file from path " + path);
            localConfiguration = XmlSerializer.deserializeFromFile(Hl7ReceiverConfiguration.class, path, CONFIG_XSD);
        }
        else
        {
            LOG.info("Loading local configuration file from resource " + CONFIG_RESOURCE);
            localConfiguration = XmlSerializer.deserializeFromResource(Hl7ReceiverConfiguration.class, CONFIG_RESOURCE, CONFIG_XSD);
        }
    }

    private void loadDbConfiguration() throws PgStoredProcException, SQLException
    {
//        DataLayer dataLayer = new DataLayer(getDatabaseConnection());
//        this.dbConfiguration = dataLayer.getConfiguration(getInstanceId());
    }

    public DataSource getDatabaseConnection() throws SQLException
    {
        return null; //getDataSource(localConfiguration.getDatabaseConnections().getSftpReader());
    }

//    private static DataSource getDataSource(DatabaseConnection databaseConnection) throws SQLException
//    {
//        PGSimpleDataSource pgSimpleDataSource = new PGSimpleDataSource();
//        pgSimpleDataSource.setServerName(databaseConnection.getHostname());
//        pgSimpleDataSource.setPortNumber(databaseConnection.getPort().intValue());
//        pgSimpleDataSource.setDatabaseName(databaseConnection.getDatabase());
//        pgSimpleDataSource.setUser(databaseConnection.getUsername());
//        pgSimpleDataSource.setPassword(databaseConnection.getPassword());
//        return pgSimpleDataSource;
//        //return DataSources.pooledDataSource(pgSimpleDataSource);
//    }
//
//    public String getInstanceId()
//    {
//        return localConfiguration.getInstanceId();
//    }
//
//    public SftpReaderConfiguration getLocalConfiguration()
//    {
//        return this.localConfiguration;
//    }
//
//    public DbConfiguration getDbConfiguration()
//    {
//        return this.dbConfiguration;
//    }
}
