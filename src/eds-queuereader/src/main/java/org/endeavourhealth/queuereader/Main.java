package org.endeavourhealth.queuereader;

import org.endeavourhealth.queuereader.configuration.ConfigManager;
import org.endeavourhealth.queuereader.configuration.model.QueueReaderConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
		LOG.info("--------------------------------------------------");
		LOG.info("EDS Queue Reader");
		LOG.info("--------------------------------------------------");

		if (args.length != 1) {
			LOG.info("Usage:");
			LOG.info("    queuereader [configuration.xml]");
			return;
		}

		// Load config
		LOG.info("Loading configuration file (" + args[0] + ")");
		QueueReaderConfiguration configuration = ConfigManager.readFromFile(args[0]);

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
		LOG.info("Shutting down");
		rabbitHandler.stop();

		LOG.info("Done");
	}
}
