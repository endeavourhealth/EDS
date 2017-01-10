package org.endeavourhealth.core.queueing;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.core.data.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitConfig {
	private static final Logger LOG = LoggerFactory.getLogger(RabbitConfig.class);

	private String password = null;
	private String username = null;
	private String nodes = null;

	private static RabbitConfig instance;

	public static RabbitConfig getInstance()  {
		if (instance == null)
			instance = new RabbitConfig();

		return instance;
	}

	private RabbitConfig() {

		try {
			JsonNode rabbitConfig = ConfigManager.getConfigurationAsJson("rabbit");

			this.nodes = rabbitConfig.get("nodes").asText();
			this.username = rabbitConfig.get("username").asText();
			this.password = rabbitConfig.get("password").asText();

		} catch (Exception e) {
			LOG.error("Failed to load rabbit config", e);
		}
	}

	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

	public String getNodes() {
		return nodes;
	}

	/*private static final Logger LOG = LoggerFactory.getLogger(RabbitConfig.class);
	private static final String CONFIG_XSD = "RabbitConfig.xsd";
	private static final String CONFIG_RESOURCE = "RabbitConfig.xml";

	private static RabbitConfig instance;

	public static RabbitConfig getInstance()  {
		if (instance == null)
			instance = new RabbitConfig("RabbitConfig.xml");

		return instance;
	}

	private RMQConfig rmqConfig;

	public RabbitConfig(String configFile) {
		try {
			File f = new File(configFile);
			if (f.exists() && !f.isDirectory()) {
				LOG.info("Loading configuration file (" + configFile + ")");
				rmqConfig = XmlSerializer.deserializeFromFile(RMQConfig.class, configFile, CONFIG_XSD);
			} else {
				LOG.info("Loading configuration file from resource " + CONFIG_RESOURCE);
				rmqConfig = XmlSerializer.deserializeFromResource(RMQConfig.class, CONFIG_RESOURCE, CONFIG_XSD);
			}
		} catch (Exception e) {
			LOG.error("Unable to load rabbit configuration ", e);
		}
	}

	public RMQExchange getExchange(String exchangeName) {
		if (rmqConfig != null) {
			for (RMQExchange rmqExchange : rmqConfig.getExchange()) {
				if (rmqExchange.getName().equals(exchangeName))
					return rmqExchange;
			}
		}

		return null;
	}*/
}
