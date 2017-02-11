package org.endeavourhealth.sftpreader;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.core.data.config.ConfigManager;
import org.endeavourhealth.core.data.config.ConfigManagerException;
import org.endeavourhealth.core.postgres.PgDataSource;
import org.endeavourhealth.core.postgres.PgStoredProcException;
import org.endeavourhealth.core.postgres.logdigest.LogDigestAppender;
import org.endeavourhealth.sftpreader.model.db.DbConfiguration;
import org.endeavourhealth.sftpreader.model.exceptions.SftpReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;

public final class Configuration
{
    // class members //
    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);
    private static final String PROGRAM_CONFIG_MANAGER_NAME = "sftpreader";
    private static final String INSTANCE_NAME_JAVA_PROPERTY = "INSTANCE_NAME";

    private static Configuration instance = null;

    public static Configuration getInstance() throws Exception
    {
        if (instance == null)
            instance = new Configuration();

        return instance;
    }

    // instance members //
    private DbConfiguration dbConfiguration;
    private String instanceName;
    private String postgresUrl;
    private String postgresUsername;
    private String postgresPassword;

    private Configuration() throws Exception
    {
        retrieveInstanceName();
        initialiseConfigManager();
        addHL7LogAppender();
        loadDbConfiguration();
    }

    private void retrieveInstanceName() throws SftpReaderException {
        try {
            instanceName = System.getProperty(INSTANCE_NAME_JAVA_PROPERTY);

            if (StringUtils.isEmpty(instanceName))
                throw new SftpReaderException("Could not find " + INSTANCE_NAME_JAVA_PROPERTY + " Java -D property");

        } catch (Exception e) {
            throw new SftpReaderException("Could not read " + INSTANCE_NAME_JAVA_PROPERTY + " Java -D property");
        }
    }

    private void initialiseConfigManager() throws ConfigManagerException {
        ConfigManager.Initialize(PROGRAM_CONFIG_MANAGER_NAME);

        postgresUrl = ConfigManager.getConfiguration("postgres-url");
        postgresUsername = ConfigManager.getConfiguration("postgres-username");
        postgresPassword = ConfigManager.getConfiguration("postgres-password");
    }

    private void addHL7LogAppender() throws SftpReaderException {
        try {
            LogDigestAppender.addLogAppender(new DataLayer(getDatabaseConnection()));
        } catch (Exception e) {
            throw new SftpReaderException("Error adding SFTP Reader log appender", e);
        }
    }

    private void loadDbConfiguration() throws PgStoredProcException, SQLException, SftpReaderException {
        DataLayer dataLayer = new DataLayer(getDatabaseConnection());
        this.dbConfiguration = dataLayer.getConfiguration(getInstanceName());

        if (this.dbConfiguration == null)
            throw new SftpReaderException("No configuration found with instance name " + getInstanceName());
    }

    public DataSource getDatabaseConnection() throws SQLException {
        return PgDataSource.get(postgresUrl, postgresUsername, postgresPassword);
    }

    public String getInstanceName()
    {
        return instanceName;
    }

    public DbConfiguration getDbConfiguration()
    {
        return this.dbConfiguration;
    }
}
