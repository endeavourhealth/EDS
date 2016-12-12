package org.endeavourhealth.hl7receiver;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7receiver.model.db.DbConfiguration;
import org.endeavourhealth.hl7receiver.model.exceptions.ConfigurationException;
import org.endeavourhealth.hl7receiver.model.exceptions.LogbackConfigurationException;
import org.endeavourhealth.utilities.configuration.LocalConfigurationException;
import org.endeavourhealth.utilities.configuration.LocalConfigurationLoader;
import org.endeavourhealth.utilities.configuration.model.DatabaseConnection;
import org.endeavourhealth.utilities.configuration.model.DatabaseType;
import org.endeavourhealth.utilities.configuration.model.LocalConfiguration;
import org.endeavourhealth.utilities.postgres.PgDataSource;
import org.endeavourhealth.utilities.streams.StreamExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;

public final class Configuration
{
    // class members //
    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);
    private static final String LOGBACK_ENVIRONMENT_VARIABLE = "LOGBACK_CONFIG_FILE";

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
        validateLogbackConfiguration();
        loadLocalConfiguration();
        loadDbConfiguration();
    }

    public void validateLogbackConfiguration() throws ConfigurationException {
        String logbackConfiguration = System.getenv(LOGBACK_ENVIRONMENT_VARIABLE);

        if (StringUtils.isBlank(logbackConfiguration))
            throw new LogbackConfigurationException("FATAL ERROR: Please set environment variable " + LOGBACK_ENVIRONMENT_VARIABLE);

        File file = new File(logbackConfiguration);

        if ((!file.exists()) || (!file.isFile()))
            throw new LogbackConfigurationException("FATAL ERROR: Could not find " + LOGBACK_ENVIRONMENT_VARIABLE);

        LOG.info("Using base logback config file at " + logbackConfiguration);
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
