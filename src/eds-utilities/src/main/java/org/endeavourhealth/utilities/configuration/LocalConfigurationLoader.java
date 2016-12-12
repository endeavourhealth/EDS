package org.endeavourhealth.utilities.configuration;

import org.endeavourhealth.utilities.configuration.model.LocalConfiguration;
import org.endeavourhealth.utilities.xml.XmlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalConfigurationLoader {

    private static final String CONFIG_XSD = "LocalConfiguration.xsd";
    private static final String CONFIG_RESOURCE = "LocalConfiguration.xml";
    private static final String CONFIG_PATH_JAVA_PROPERTY = "localConfigurationFile";
    private static final Logger LOG = LoggerFactory.getLogger(LocalConfigurationLoader.class);

    public static LocalConfiguration loadLocalConfiguration() throws LocalConfigurationException
    {
        String path = System.getProperty(CONFIG_PATH_JAVA_PROPERTY);

        try {
            if (path != null) {
                LOG.info("Loading local configuration file from path " + path);
                return XmlSerializer.deserializeFromFile(LocalConfiguration.class, path, CONFIG_XSD);
            } else {
                LOG.info("Did not find java property " + CONFIG_PATH_JAVA_PROPERTY + ", loading local configuration file from resource " + CONFIG_RESOURCE);
                return XmlSerializer.deserializeFromResource(LocalConfiguration.class, CONFIG_RESOURCE, CONFIG_XSD);
            }
        } catch (Exception e) {
            throw new LocalConfigurationException("Error loading local configuration, see inner exception", e);
        }
    }
}
