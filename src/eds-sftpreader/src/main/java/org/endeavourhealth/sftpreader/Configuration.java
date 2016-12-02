package org.endeavourhealth.sftpreader;

import org.endeavourhealth.utilities.xml.XmlSerializer;
import org.endeavourhealth.sftpreader.model.db.DbConfiguration;
import org.endeavourhealth.sftpreader.model.xml.DatabaseConnection;
import org.endeavourhealth.sftpreader.model.xml.SftpReaderConfiguration;
import org.endeavourhealth.utilities.postgres.PgDataSource;
import org.endeavourhealth.utilities.postgres.PgStoredProcException;
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
        String path = System.getProperty("sftpreader.configurationFile");

        if (path != null)
        {
            LOG.info("Loading local configuration file from path " + path);
            localConfiguration = XmlSerializer.deserializeFromFile(SftpReaderConfiguration.class, path, CONFIG_XSD);
        }
        else
        {
            LOG.info("Loading local configuration file from resource " + CONFIG_RESOURCE);
            localConfiguration = XmlSerializer.deserializeFromResource(SftpReaderConfiguration.class, CONFIG_RESOURCE, CONFIG_XSD);
        }
    }

    private void loadDbConfiguration() throws PgStoredProcException, SQLException
    {
        DataLayer dataLayer = new DataLayer(getDatabaseConnection());
        this.dbConfiguration = dataLayer.getConfiguration(getInstanceId());
    }

    public DataSource getDatabaseConnection() throws SQLException
    {
        DatabaseConnection dataSource = localConfiguration.getDatabaseConnections().getSftpReader();

        return PgDataSource.get(
                dataSource.getHostname(),
                dataSource.getPort().intValue(),
                dataSource.getDatabase(),
                dataSource.getUsername(),
                dataSource.getPassword());
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
