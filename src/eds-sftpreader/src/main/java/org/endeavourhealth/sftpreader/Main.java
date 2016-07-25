package org.endeavourhealth.sftpreader;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.db.DBAppender;
import ch.qos.logback.core.db.DriverManagerConnectionSource;
import org.endeavourhealth.core.data.logging.LogbackCassandraAppender;
import org.endeavourhealth.sftpreader.dbModel.DbConfiguration;
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
			DbConfiguration dbConfiguration = Configuration.getInstance().getDbConfiguration();

			writeHeaderLogLine(PROGRAM_DISPLAY_NAME);

			runSftpHandlerAndWaitForInput(dbConfiguration);

			writeHeaderLogLine(PROGRAM_DISPLAY_NAME + " stopped");
		}
		catch (Exception e)
		{
			LOG.error("Fatal exception occurred", e);
		}
	}

	private static void writeHeaderLogLine(String text)
	{
		LOG.info("--------------------------------------------------");
		LOG.info(text);
		LOG.info("--------------------------------------------------");
	}

	private static void runSftpHandlerAndWaitForInput(DbConfiguration dbConfiguration) throws IOException
	{
		SftpTask sftpTask = new SftpTask(dbConfiguration);

		Timer timer = new Timer(true);

		try
		{
			timer.scheduleAtFixedRate(sftpTask, 0, dbConfiguration.getPollFrequencySeconds() * 1000);

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
