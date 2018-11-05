package org.endeavourhealthX.subscriber.configuration;

import org.endeavourhealth.common.utility.XmlSerializer;
import org.endeavourhealthX.subscriber.configuration.models.SubscriberConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationProvider {

    private static final String CONFIG_XSD = "SubscriberConfiguration.xsd";
    private static final String CONFIG_RESOURCE = "SubscriberConfiguration.xml";

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationProvider.class);
    private static ConfigurationProvider instance = new ConfigurationProvider();

    private SubscriberConfiguration configuration = null;

    public static ConfigurationProvider getInstance() throws Exception {
        return instance;
    }

    private ConfigurationProvider() {

        String path = System.getProperty("sftpreader.configurationFile");

        try {
            if (path != null) {
                LOG.info("Loading subscriber configuration from path " + path);
                configuration = XmlSerializer.deserializeFromFile(SubscriberConfiguration.class, path, CONFIG_XSD);

            } else {
                LOG.info("Loading subscriber configuration from resource " + CONFIG_RESOURCE);
                configuration = XmlSerializer.deserializeFromResource(SubscriberConfiguration.class, CONFIG_RESOURCE, CONFIG_XSD);

            }
        } catch (Exception ex) {
            LOG.error("Failed to read configuration", ex);
        }

    }

    public SubscriberConfiguration getConfiguration() {
        return configuration;
    }
}
