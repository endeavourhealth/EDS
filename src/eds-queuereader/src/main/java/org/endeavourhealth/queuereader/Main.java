package org.endeavourhealth.queuereader;

import org.endeavourhealth.core.configuration.QueueReaderConfiguration;
import org.endeavourhealth.core.data.config.ConfigManager;
import org.endeavourhealth.core.utility.XmlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
	private static final String CONFIG_XSD = "QueueReaderConfiguration.xsd";

	public static void main(String[] args) throws Exception {
		LOG.info("--------------------------------------------------");
		LOG.info("EDS Queue Reader");
		LOG.info("--------------------------------------------------");

		if (args.length != 1) {
			LOG.error("Usage: queuereader config_id");
			return;
		}

		LOG.info("Initialising config manager");
		ConfigManager.Initialize("queuereader");

		LOG.info("Fetching queuereader configuration");
		String configXml = ConfigManager.getConfiguration(args[0]);
		QueueReaderConfiguration configuration = XmlSerializer.deserializeFromString(QueueReaderConfiguration.class, configXml, CONFIG_XSD);

		// Instantiate rabbit handler
		LOG.info("Creating EDS queue reader");
		RabbitHandler rabbitHandler = new RabbitHandler(configuration);

		// Begin consume
		LOG.info("Starting message consumption");
		rabbitHandler.start();
		LOG.info("EDS Queue reader running");
	}
}
