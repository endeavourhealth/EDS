package org.endeavourhealth.messagingapi.configuration;

import org.endeavourhealth.core.configuration.ApiConfiguration;
import org.endeavourhealth.core.data.config.ConfigManager;
import org.endeavourhealth.core.utility.XmlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigWrapper {
	private static final Logger LOG = LoggerFactory.getLogger(ConfigWrapper.class);
	private static final String XSD = "ApiConfiguration.xsd";
	private static ApiConfiguration config = null;

	public static ApiConfiguration getInstance() {
		if (config == null) {
			initialize();
		}
		return config;
	}

	public static void initialize() {
		String apiConfigXml = ConfigManager.getConfiguration("api-configuration");
		try {
			config = XmlSerializer.deserializeFromString(ApiConfiguration.class, apiConfigXml, XSD);
		} catch (Exception e) {
			throw new UnsupportedOperationException("api configuration not set", e);
		}
	}

}
