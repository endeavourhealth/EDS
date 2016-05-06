package org.endeavourhealth.messagingapi.configuration;

import org.endeavourhealth.core.configuration.ApiConfiguration;
import org.endeavourhealth.core.utilities.XmlSerializer;

public class ConfigManager {
	private static ApiConfiguration config = null;

	public static ApiConfiguration getInstance() {
		try {
			if (config == null)
				config = XmlSerializer.deserializeFromResource(ApiConfiguration.class, "ApiConfiguration.xml", null);

			return config;
		} catch (Exception e) {
			e.printStackTrace();
			return  null;
		}
	}
}
