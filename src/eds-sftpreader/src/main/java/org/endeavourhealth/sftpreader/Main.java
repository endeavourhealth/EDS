package org.endeavourhealth.sftpreader;

import org.endeavourhealth.core.configuration.SftpReaderConfiguration;
import org.endeavourhealth.core.data.logging.LogbackCassandraAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Timer;

public class Main
{
	private static final String PROGRAM_DISPLAY_NAME = "EDS SFTP poller";
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args)
	{
		try
		{
			Configuration.initialiseEngineConfiguration(args);

			startCassandraLogging();

			writeHeaderLogLine(PROGRAM_DISPLAY_NAME);

			SftpReaderConfiguration configuration = Configuration.loadLocalConfiguration(args);

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
}
