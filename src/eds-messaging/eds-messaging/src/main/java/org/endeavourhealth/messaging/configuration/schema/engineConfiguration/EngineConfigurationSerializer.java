package org.endeavourhealth.messaging.configuration.schema.engineConfiguration;

import org.endeavourhealth.messaging.utilities.XmlHelper;
import org.endeavourhealth.messaging.utilities.XmlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class EngineConfigurationSerializer {

    private static final String XSD = "EngineConfiguration.xsd";
    private static final String XML = "EngineConfiguration.xml";

    private static EngineConfiguration config = null;

    public static EngineConfiguration getConfig() {
        if (config == null) {
            try {
                config = deserializeConfig();
            } catch (Exception ex) {
                //can't use logback to log, since this class is used by the initialisation of the DB logging appender
                ex.printStackTrace();
                //LOG.error("Error reading config", ex);
            }
        }
        return config;
    }

    private static EngineConfiguration deserializeConfig() throws Exception {

        String property = System.getProperty("enterprise.configurationFile");

        //if the property wasn't defined, read in the config file from our resource bundle
        if (property == null) {
            return XmlSerializer.deserializeFromResource(EngineConfiguration.class, XML, XSD);
        } else {
            Path path = Paths.get(property);
            String fileContent = new String(Files.readAllBytes(path));
            return XmlSerializer.deserializeFromString(EngineConfiguration.class, fileContent, XSD);
        }
    }
}
