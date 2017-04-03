package org.endeavourhealth.core.configuration;

import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.XmlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		config = deserialise(apiConfigXml);
	}

	public static ApiConfiguration deserialise(String xmlStr) throws UnsupportedOperationException {
		try {
			return XmlSerializer.deserializeFromString(ApiConfiguration.class, xmlStr, XSD);
		} catch (Exception e) {
			throw new UnsupportedOperationException("api configuration not set", e);
		}
	}

}
