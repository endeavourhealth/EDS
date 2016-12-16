package org.endeavourhealth.hl7receiver;

import org.endeavourhealth.core.data.config.ConfigManager;
import org.endeavourhealth.core.data.config.ConfigManagerException;
import org.endeavourhealth.hl7receiver.logging.Logger;
import org.endeavourhealth.hl7receiver.model.db.DbConfiguration;
import org.endeavourhealth.hl7receiver.model.exceptions.ConfigurationException;
import org.endeavourhealth.utilities.postgres.PgDataSource;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.sql.SQLException;

public final class Configuration
{
    // class members //
    private static final Logger LOG = Logger.getLogger(Configuration.class);
    private static final String PROGRAM_CONFIG_MANAGER_NAME = "hl7receiver";

    private static Configuration instance = null;

    public static Configuration getInstance() throws Exception
    {
        if (instance == null)
            instance = new Configuration();

        return instance;
    }

    // instance members //
    private DbConfiguration dbConfiguration;
    private String machineName;
    private String postgresUrl;
    private String postgresUsername;
    private String postgresPassword;

    private Configuration() throws ConfigurationException
    {
        initialiseMachineName();
        initialiseConfigManager();
        configureLogger();
        loadDbConfiguration();
    }

    private void initialiseMachineName() throws ConfigurationException {
        try {
            machineName = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            throw new ConfigurationException("Error getting machine name");
        }
    }

    private void initialiseConfigManager() throws ConfigurationException {
        try {
            ConfigManager.Initialize(PROGRAM_CONFIG_MANAGER_NAME);

            postgresUrl = ConfigManager.getConfiguration("postgres-url");
            postgresUsername = ConfigManager.getConfiguration("postgres-username");
            postgresPassword = ConfigManager.getConfiguration("postgres-username");

        } catch (ConfigManagerException e) {
            throw new ConfigurationException("Error loading ConfigManager configuration", e);
        }
    }

    private void configureLogger() throws ConfigurationException {
        try {
            Logger.setDBLogger(new DataLayer(getDatabaseConnection()));
        } catch (Exception e) {
            throw new ConfigurationException("Error setting logger data source", e);
        }
    }

    private void loadDbConfiguration() throws ConfigurationException {
        try {
            DataLayer dataLayer = new DataLayer(getDatabaseConnection());
            this.dbConfiguration = dataLayer.getConfiguration(getMachineName());
        } catch (Exception e) {
            throw new ConfigurationException("Error loading DB configuration, see inner exception", e);
        }
    }

    public DataSource getDatabaseConnection() throws SQLException
    {
        return PgDataSource.get(postgresUrl, postgresUsername, postgresPassword);
    }

    public String getMachineName()
    {
        return machineName;
    }

    public DbConfiguration getDbConfiguration() {
        return dbConfiguration;
    }
}
