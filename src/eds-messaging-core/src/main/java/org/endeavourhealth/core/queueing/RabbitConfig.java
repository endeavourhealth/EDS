package org.endeavourhealth.core.queueing;

import org.endeavourhealth.core.configuration.RMQConfig;
import org.endeavourhealth.core.configuration.RMQExchange;
import org.endeavourhealth.core.configuration.RMQQueue;
import org.endeavourhealth.core.utility.XmlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.List;

public class RabbitConfig {
	private static final Logger LOG = LoggerFactory.getLogger(RabbitConfig.class);
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
	}
}
