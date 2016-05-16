package org.endeavourhealth.messagingapi.configuration;

import org.endeavourhealth.core.configuration.ApiConfiguration;
import org.endeavourhealth.core.utility.XmlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {
	private static final Logger LOG = LoggerFactory.getLogger(ConfigManager.class);
	private static final String XSD = "ApiConfiguration.xsd";
	private static ApiConfiguration config = null;

	public static ApiConfiguration getInstance() {
		if (config == null) {
			LOG.error("Trying to access API config before loadConfigurationFromProperty(..) is called");
		}
		return config;
	}

	public static void loadConfigurationFromProperty(String propertyName) throws Exception {
		String filePath = System.getProperty(propertyName);

		//if the system property was defined, then read that file in. If not, then go for the default resource
		if (filePath == null) {
			config = XmlSerializer.deserializeFromResource(ApiConfiguration.class, "ApiConfiguration.xml", XSD);
		} else {
			Path path = Paths.get(filePath);
			String fileContent = new String(Files.readAllBytes(path));
			config = XmlSerializer.deserializeFromString(ApiConfiguration.class, fileContent, XSD);
		}
	}

}
