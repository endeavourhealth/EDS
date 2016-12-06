package org.endeavourhealth.hl7receiver;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.hl7receiver.model.db.DbConfiguration;
import org.endeavourhealth.hl7receiver.model.exceptions.ConfigurationException;
import org.endeavourhealth.hl7receiver.model.exceptions.LogbackConfigurationException;
import org.endeavourhealth.hl7receiver.model.xml.DatabaseConnection;
import org.endeavourhealth.hl7receiver.model.xml.Hl7ReceiverConfiguration;
import org.endeavourhealth.utilities.postgres.PgDataSource;
import org.endeavourhealth.utilities.xml.XmlSerializer;
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
    private static final String CONFIG_XSD = "Hl7ReceiverConfiguration.xsd";
    private static final String CONFIG_RESOURCE = "Hl7ReceiverConfiguration.xml";
    private static final String CONFIG_PATH_JAVA_PROPERTY = "hl7receiver.configurationFile";

    private static Configuration instance = null;

    public static Configuration getInstance() throws Exception
    {
        if (instance == null)
            instance = new Configuration();

        return instance;
    }

    // instance members //
    private Hl7ReceiverConfiguration localConfiguration;
    private DbConfiguration dbConfiguration;

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

    private void loadLocalConfiguration() throws ConfigurationException
    {
        String path = System.getProperty(CONFIG_PATH_JAVA_PROPERTY);

        try {
            if (path != null) {
                LOG.info("Loading local configuration file from path " + path);
                localConfiguration = XmlSerializer.deserializeFromFile(Hl7ReceiverConfiguration.class, path, CONFIG_XSD);
            } else {
                LOG.info("Did not find java property " + CONFIG_PATH_JAVA_PROPERTY + ", loading local configuration file from resource " + CONFIG_RESOURCE);
                localConfiguration = XmlSerializer.deserializeFromResource(Hl7ReceiverConfiguration.class, CONFIG_RESOURCE, CONFIG_XSD);
            }
        } catch (Exception e) {
            throw new ConfigurationException("Error loading local configuration, see inner exception", e);
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
        DatabaseConnection db = localConfiguration.getDatabaseConnections().getHl7Receiver();
        return PgDataSource.get(db.getHostname(), db.getPort().intValue(), db.getDatabase(), db.getUsername(), db.getPassword());
    }

    public String getInstanceName()
    {
        return localConfiguration.getInstanceName();
    }

    public DbConfiguration getDbConfiguration() {
        return dbConfiguration;
    }
}
