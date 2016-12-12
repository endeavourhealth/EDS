package org.endeavourhealth.sftpreader;

import org.endeavourhealth.sftpreader.model.db.DbConfiguration;
import org.endeavourhealth.utilities.configuration.LocalConfigurationException;
import org.endeavourhealth.utilities.configuration.LocalConfigurationLoader;
import org.endeavourhealth.utilities.configuration.model.DatabaseConnection;
import org.endeavourhealth.utilities.configuration.model.DatabaseType;
import org.endeavourhealth.utilities.configuration.model.LocalConfiguration;
import org.endeavourhealth.utilities.postgres.PgDataSource;
import org.endeavourhealth.utilities.postgres.PgStoredProcException;
import org.endeavourhealth.utilities.streams.StreamExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;

public final class Configuration
{
    // class members //
    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

    private static Configuration instance = null;

    public static Configuration getInstance() throws Exception
    {
        if (instance == null)
            instance = new Configuration();

        return instance;
    }

    // instance members //
    private LocalConfiguration localConfiguration;
    private DbConfiguration dbConfiguration;
    private DatabaseConnection sftpReaderDatabaseConnection;

    private Configuration() throws Exception
    {
        loadLocalConfiguration();
        loadDbConfiguration();
    }

    private void loadLocalConfiguration() throws LocalConfigurationException {
        localConfiguration = LocalConfigurationLoader.loadLocalConfiguration();

        sftpReaderDatabaseConnection = localConfiguration
                .getDatabaseConnections()
                .getDatabaseConnection()
                .stream()
                .filter(t -> t.getDatabaseType().equals(DatabaseType.SFTPREADER.value()))
                .collect(StreamExtension.singleOrNullCollector());

        if (sftpReaderDatabaseConnection == null)
            throw new LocalConfigurationException("Could not find database connection in local configuration file with DatabaseType of " + DatabaseType.HL_7_RECEIVER.value());
    }


    private void loadDbConfiguration() throws PgStoredProcException, SQLException
    {
        DataLayer dataLayer = new DataLayer(getDatabaseConnection());
        this.dbConfiguration = dataLayer.getConfiguration(getInstanceName());
    }

    public DataSource getDatabaseConnection() throws SQLException
    {
        return PgDataSource.get(sftpReaderDatabaseConnection);
    }

    public String getInstanceName()
    {
        return localConfiguration.getInstanceName();
    }

    public LocalConfiguration getLocalConfiguration()
    {
        return this.localConfiguration;
    }

    public DbConfiguration getDbConfiguration()
    {
        return this.dbConfiguration;
    }
}
