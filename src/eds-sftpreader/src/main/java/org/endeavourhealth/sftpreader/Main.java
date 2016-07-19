package org.endeavourhealth.sftpreader;

import com.google.common.io.Resources;
import org.endeavourhealth.core.configuration.SftpReaderConfiguration;
import org.endeavourhealth.core.data.logging.LogbackCassandraAppender;
import org.endeavourhealth.core.engineConfiguration.EngineConfigurationSerializer;
import org.endeavourhealth.core.utility.XmlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Timer;

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

		configuration.getCredentials().setClientPrivateKeyFilePath(resolveFilePath(configuration.getCredentials().getClientPrivateKeyFilePath()));
		configuration.getCredentials().setHostPublicKeyFilePath(resolveFilePath(configuration.getCredentials().getHostPublicKeyFilePath()));

		if (configuration.getPgpDecryption() != null)
		{
			configuration.getPgpDecryption().setRecipientPrivateKeyFilePath(resolveFilePath(configuration.getPgpDecryption().getRecipientPrivateKeyFilePath()));
			configuration.getPgpDecryption().setSenderPublicKeyFilePath(resolveFilePath(configuration.getPgpDecryption().getSenderPublicKeyFilePath()));
		}

		return configuration;
	}

	private static void runSftpHandlerAndWaitForInput(SftpReaderConfiguration configuration) throws IOException
	{
		SftpTask sftpTask = new SftpTask(configuration);

		Timer timer = new Timer(true);

		try
		{
			timer.scheduleAtFixedRate(sftpTask, 0, configuration.getPollDelaySeconds() * 1000);

			LOG.info("");
			LOG.info("Press any key to exit...");

			System.in.read();

			LOG.info("Stopping...");
		}
		finally
		{
			timer.cancel();
		}
	}

	private static String resolveFilePath(String filePath)
	{
		if (!Files.exists(Paths.get(filePath)))
			return Resources.getResource(filePath).getPath();

		return filePath;
	}
}
