package org.endeavourhealth.sftpreader;

import org.endeavourhealth.core.configuration.SftpReaderConfiguration;
import org.endeavourhealth.core.logging.CassandraDbAppender;
import org.endeavourhealth.core.engineConfiguration.EngineConfigurationSerializer;
import org.endeavourhealth.core.utility.XmlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
	private static final String CONFIG_XSD = "SftpReaderConfiguration.xsd";
	private static final String CONFIG_RESOURCE = "SftpReaderConfiguration.xml";

	public static void main(String[] args) throws Exception {
		LOG.info("--------------------------------------------------");
		LOG.info("EDS Queue Reader");
		LOG.info("--------------------------------------------------");

		// Load config - from file if passed in, otherwise from resources
		SftpReaderConfiguration configuration = null;
		if (args.length > 0) {
			LOG.info("Loading configuration file (" + args[0] + ")");
			configuration = XmlSerializer.deserializeFromFile(SftpReaderConfiguration.class, args[0], CONFIG_XSD);
		} else {
			LOG.info("Loading configuration file from resource " + CONFIG_RESOURCE);
			configuration = XmlSerializer.deserializeFromResource(SftpReaderConfiguration.class, CONFIG_RESOURCE, CONFIG_XSD);
		}

		//load common config
		EngineConfigurationSerializer.loadConfigFromArgIfPossible(args, 1);

		//logging
		CassandraDbAppender.tryRegisterDbAppender();

		// Create SFTP reader
		SftpHandler sftpHandler = new SftpHandler(configuration);

		// Begin consume
		LOG.info("Starting message consumption");
		sftpHandler.start();
		LOG.info("EDS Sftp reader running");

		LOG.info("Press any key to exit...");
		System.in.read();

		// Shutdown
		LOG.info("Shutting down");
		sftpHandler.stop();

		LOG.info("Done");
	}
}
