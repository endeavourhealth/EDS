package org.endeavourhealth.sftpreader;

import org.endeavourhealth.core.configuration.SftpReaderConfiguration;
import org.endeavourhealth.core.data.logging.LogbackCassandraAppender;
import org.endeavourhealth.core.engineConfiguration.EngineConfigurationSerializer;
import org.endeavourhealth.core.utility.XmlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class Main
{
	private static final String PROGRAM_DISPLAY_NAME = "EDS SFTP poller";
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
	private static final String CONFIG_XSD = "SftpReaderConfiguration.xsd";
	private static final String CONFIG_RESOURCE = "SftpReaderConfiguration.xml";

	public static void main(String[] args)
	{
		try
		{
			initialiseEngineConfiguration(args);

			startCassandraLogging();

			writeHeaderLogLine(PROGRAM_DISPLAY_NAME);

			SftpReaderConfiguration configuration = loadSftpConfiguration(args);

			runSftpHandlerAndWaitForInput(configuration);

			writeHeaderLogLine(PROGRAM_DISPLAY_NAME + " stopped");
		}
		catch (Exception e)
		{
			LOG.error("Fatal exception occurred", e);
		}
	}

	private static void startCassandraLogging()
	{
		LogbackCassandraAppender.tryRegisterDbAppender();
	}

	private static void writeHeaderLogLine(String text)
	{
		LOG.info("--------------------------------------------------");
		LOG.info(text);
		LOG.info("--------------------------------------------------");
	}

	private static void initialiseEngineConfiguration(String[] args) throws Exception
	{
		EngineConfigurationSerializer.loadConfigFromArgIfPossible(args, 1);
	}

	private static SftpReaderConfiguration loadSftpConfiguration(String[] args) throws Exception
	{
		SftpReaderConfiguration configuration = null;

		if (args.length > 0)
		{
			LOG.info("Loading configuration file (" + args[0] + ")");
			configuration = XmlSerializer.deserializeFromFile(SftpReaderConfiguration.class, args[0], CONFIG_XSD);
		}
		else
		{
			LOG.info("Loading configuration file from resource " + CONFIG_RESOURCE);
			configuration = XmlSerializer.deserializeFromResource(SftpReaderConfiguration.class, CONFIG_RESOURCE, CONFIG_XSD);
		}

		return configuration;
	}

	private static void runSftpHandlerAndWaitForInput(SftpReaderConfiguration configuration) throws IOException
	{
		LOG.info("Starting SFTP handler");

		SftpHandler sftpHandler = new SftpHandler(configuration);
		sftpHandler.start();

		LOG.info("SFTP handler started");

		LOG.info("");
		LOG.info("Press any key to exit...");

		System.in.read();

		LOG.info("Stopping SFTP handler");

		sftpHandler.stop();

		LOG.info("SFTP handler stopped");
	}
}
