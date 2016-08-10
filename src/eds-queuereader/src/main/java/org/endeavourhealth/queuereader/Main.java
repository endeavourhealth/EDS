package org.endeavourhealth.queuereader;

import org.endeavourhealth.core.configuration.QueueReaderConfiguration;
import org.endeavourhealth.core.engineConfiguration.EngineConfigurationSerializer;
import org.endeavourhealth.core.utility.XmlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
	private static final String CONFIG_XSD = "QueueReaderConfiguration.xsd";
	private static final String CONFIG_RESOURCE = "QueueReaderConfiguration.xml";

	public static void main(String[] args) throws Exception {
		LOG.info("--------------------------------------------------");
		LOG.info("EDS Queue Reader");
		LOG.info("--------------------------------------------------");

		// Load config - from file if passed in, otherwise from resources
		QueueReaderConfiguration configuration = null;
		if (args.length > 0) {
			LOG.info("Loading configuration file (" + args[0] + ")");
			configuration = XmlSerializer.deserializeFromFile(QueueReaderConfiguration.class, args[0], CONFIG_XSD);
		} else {
			LOG.info("Loading configuration file from resource " + CONFIG_RESOURCE);
			configuration = XmlSerializer.deserializeFromResource(QueueReaderConfiguration.class, CONFIG_RESOURCE, CONFIG_XSD);
		}

		//load common config
		EngineConfigurationSerializer.loadConfigFromArgIfPossible(args, 1);

		// Instantiate rabbit handler
		LOG.info("Creating EDS queue reader");
		RabbitHandler rabbitHandler = new RabbitHandler(configuration);

		// Begin consume
		LOG.info("Starting message consumption");
		rabbitHandler.start();
		LOG.info("EDS Queue reader running");

		LOG.info("Press any key to exit...");
		System.in.read();

		// Shutdown
		LOG.info("Shutting down rabbit handler...");
		rabbitHandler.stop();

		LOG.info("Waiting for Async logger to shutdown...");
	}
}
