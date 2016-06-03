package org.endeavourhealth.ui.framework.config;

import org.endeavourhealth.ui.framework.config.models.Config;
import org.endeavourhealth.ui.XmlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Drew on 22/03/2016.
 */
public abstract class ConfigSerializer {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigSerializer.class);
    private static final String XSD = "Config.xsd";
    private static final String XML = "Config.xml";

    private static Config config = null;

    public static Config getConfig() {
        if (config == null) {
            try {
                config = deserializeConfig();
            } catch (Exception ex) {
                LOG.error("Error reading config", ex);
            }
        }
        return config;
    }

    private static Config deserializeConfig() throws Exception {

        String property = System.getProperty("enterprise.configurationFile");

        //if the property wasn't defined, read in the config file from our resource bundle
        if (property == null) {
            return XmlSerializer.deserializeFromResource(Config.class, XML, XSD);
        } else {
            Path path = Paths.get(property);
            String fileContent = new String(Files.readAllBytes(path));
            return XmlSerializer.deserializeFromString(Config.class, fileContent, XSD);
        }
    }
}
