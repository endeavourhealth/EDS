package org.endeavourhealth.hl7receiver;

import org.endeavourhealth.hl7receiver.logging.Logger;
import org.endeavourhealth.hl7receiver.model.db.DbConfiguration;
import org.endeavourhealth.hl7receiver.model.exceptions.ConfigurationException;
import org.endeavourhealth.utilities.configuration.LocalConfigurationException;
import org.endeavourhealth.utilities.configuration.LocalConfigurationLoader;
import org.endeavourhealth.utilities.configuration.model.DatabaseConnection;
import org.endeavourhealth.utilities.configuration.model.DatabaseType;
import org.endeavourhealth.utilities.configuration.model.LocalConfiguration;
import org.endeavourhealth.utilities.postgres.PgDataSource;
import org.endeavourhealth.utilities.streams.StreamExtension;

import javax.sql.DataSource;
import java.sql.SQLException;

public final class Configuration
{
    // class members //
    private static final Logger LOG = Logger.getLogger(Configuration.class);

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
    private DatabaseConnection hl7ReceiverDatabaseConnection;

    private Configuration() throws ConfigurationException
    {
        loadLocalConfiguration();
        configureLogger();
        loadDbConfiguration();
    }

    private void configureLogger() throws ConfigurationException {
        try {
            Logger.setDBLogger(new DataLayer(getDatabaseConnection()));
        } catch (Exception e) {
            throw new ConfigurationException("Error setting logger data source", e);
        }
    }

    private void loadLocalConfiguration() throws ConfigurationException {
        try {
            localConfiguration = LocalConfigurationLoader.loadLocalConfiguration();

            hl7ReceiverDatabaseConnection = localConfiguration
                    .getDatabaseConnections()
                    .getDatabaseConnection()
                    .stream()
                    .filter(t -> t.getDatabaseType().equals(DatabaseType.HL_7_RECEIVER.value()))
                    .collect(StreamExtension.singleOrNullCollector());

            if (hl7ReceiverDatabaseConnection == null)
                throw new ConfigurationException("Could not find database connection in local configuration file with DatabaseType of " + DatabaseType.HL_7_RECEIVER.value());

        } catch (LocalConfigurationException e) {
            throw new ConfigurationException(e);
        }
    }

    private void loadDbConfiguration() throws ConfigurationException {
        try {
            DataLayer dataLayer = new DataLayer(getDatabaseConnection());
            this.dbConfiguration = dataLayer.getConfiguration(getInstanceName());

            if (this.dbConfiguration.getDbInstance() == null)
                throw new ConfigurationException("No instance matching " + getInstanceName() + " found in DB configuration");
        } catch (Exception e) {
            throw new ConfigurationException("Error loading DB configuration, see inner exception", e);
        }
    }

    public DataSource getDatabaseConnection() throws SQLException
    {
        return PgDataSource.get(hl7ReceiverDatabaseConnection);
    }

    public String getInstanceName()
    {
        return localConfiguration.getInstanceName();
    }

    public DbConfiguration getDbConfiguration() {
        return dbConfiguration;
    }
}
