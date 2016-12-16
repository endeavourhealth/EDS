package org.endeavourhealth.sftpreader;

import org.endeavourhealth.core.data.config.ConfigManager;
import org.endeavourhealth.core.data.config.ConfigManagerException;
import org.endeavourhealth.core.postgres.PgDataSource;
import org.endeavourhealth.core.postgres.PgStoredProcException;
import org.endeavourhealth.sftpreader.model.db.DbConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;

public final class Configuration
{
    // class members //
    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);
    private static final String PROGRAM_CONFIG_MANAGER_NAME = "sftpreader";

    private static Configuration instance = null;

    public static Configuration getInstance() throws Exception
    {
        if (instance == null)
            instance = new Configuration();

        return instance;
    }

    // instance members //
    private DbConfiguration dbConfiguration;
    private String postgresUrl;
    private String postgresUsername;
    private String postgresPassword;


    private Configuration() throws Exception
    {
        initialiseConfigManager();
        loadDbConfiguration();
    }

    private void initialiseConfigManager() throws ConfigManagerException {
        ConfigManager.Initialize(PROGRAM_CONFIG_MANAGER_NAME);

        postgresUrl = ConfigManager.getConfiguration("postgres-url");
        postgresUsername = ConfigManager.getConfiguration("postgres-username");
        postgresPassword = ConfigManager.getConfiguration("postgres-username");
    }

    private void loadDbConfiguration() throws PgStoredProcException, SQLException
    {
        DataLayer dataLayer = new DataLayer(getDatabaseConnection());
        this.dbConfiguration = dataLayer.getConfiguration(getInstanceName());
    }

    public DataSource getDatabaseConnection() throws SQLException
    {
        return PgDataSource.get(postgresUrl, postgresUsername, postgresPassword);
    }

    public String getInstanceName()
    {
        return "INSTANCE-NAME";
    }

//    public LocalConfiguration getLocalConfiguration()
//    {
//        return this.localConfiguration;
//    }

    public DbConfiguration getDbConfiguration()
    {
        return this.dbConfiguration;
    }
}
