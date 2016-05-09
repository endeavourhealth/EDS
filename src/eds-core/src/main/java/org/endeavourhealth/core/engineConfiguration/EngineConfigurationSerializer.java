package org.endeavourhealth.core.engineConfiguration;

import org.endeavourhealth.core.utility.XmlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class EngineConfigurationSerializer {
    private static final Logger LOG = LoggerFactory.getLogger(EngineConfigurationSerializer.class);

    private static final String XSD = "EngineConfiguration.xsd";
    private static final String XML = "EngineConfiguration.xml";

    private static EngineConfiguration config = null;

    public static EngineConfiguration getConfig() {
        if (config == null) {
            LOG.error("Trying to access engine config before loadConfig(..) is called");
        }
        return config;
    }


    public static void loadConfigFromPropertyIfPossible(String propertyName) throws Exception {
        String path = System.getProperty(propertyName);
        loadConfig(path);
    }
    public static void loadConfigFromArgIfPossible(String[] args, int index) throws Exception {
        String path = null;
        if (index < args.length) {
            path = args[index];
        }
        loadConfig(path);
    }
    private static void loadConfig(String filePath) throws Exception {

        //if the path wasn't defined, read in the config file from our resource bundle
        if (filePath == null) {
            LOG.info("Loading engine config from resource " + XML);
            config = XmlSerializer.deserializeFromResource(EngineConfiguration.class, XML, XSD);
        } else {
            LOG.info("Loading engine config from " + filePath);
            Path path = Paths.get(filePath);
            String fileContent = new String(Files.readAllBytes(path));
            config = XmlSerializer.deserializeFromString(EngineConfiguration.class, fileContent, XSD);
        }
    }
}
