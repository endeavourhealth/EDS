package org.endeavourhealth.hl7receiver;

import org.endeavourhealth.core.data.config.ConfigManager;
import org.endeavourhealth.core.data.config.ConfigManagerException;
import org.endeavourhealth.core.postgres.PgDataSource;
import org.endeavourhealth.core.postgres.logdigest.LogDigestAppender;
import org.endeavourhealth.hl7receiver.model.db.DbConfiguration;
import org.endeavourhealth.hl7receiver.model.exceptions.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.sql.SQLException;

public final class Configuration
{
    // class members //
    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);
    private static final String PROGRAM_CONFIG_MANAGER_NAME = "hl7receiver";
    private static final String POSTGRES_URL_CONFIG_MANAGER_KEY = "postgres-url";
    private static final String POSTGRES_USERNAME_CONFIG_MANAGER_KEY = "postgres-username";
    private static final String POSTGRES_PASSWORD_CONFIG_MANAGER_KEY = "postgres-password";

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
        addHL7LogAppender();
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

            postgresUrl = ConfigManager.getConfiguration(POSTGRES_URL_CONFIG_MANAGER_KEY);
            postgresUsername = ConfigManager.getConfiguration(POSTGRES_USERNAME_CONFIG_MANAGER_KEY);
            postgresPassword = ConfigManager.getConfiguration(POSTGRES_PASSWORD_CONFIG_MANAGER_KEY);

        } catch (ConfigManagerException e) {
            throw new ConfigurationException("Error loading ConfigManager configuration", e);
        }
    }

    private void addHL7LogAppender() throws ConfigurationException {
        try {
            LogDigestAppender.addLogAppender(new DataLayer(getDatabaseConnection()));
        } catch (Exception e) {
            throw new ConfigurationException("Error adding HL7 log appender", e);
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

    public int getInstanceId() {
        return this.dbConfiguration.getInstanceId();
    }
}
