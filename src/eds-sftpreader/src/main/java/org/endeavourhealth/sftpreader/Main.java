package org.endeavourhealth.sftpreader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;

public class Main
{
	private static final String PROGRAM_DISPLAY_NAME = "EDS SFTP poller";
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static Configuration configuration;

	public static void main(String[] args)
	{
		try
		{
            configuration = Configuration.getInstance();

            LOG.info("--------------------------------------------------");
            LOG.info(PROGRAM_DISPLAY_NAME);
            LOG.info("--------------------------------------------------");

            SftpTask sftpTask = new SftpTask(configuration);

            Timer timer = new Timer();

            timer.scheduleAtFixedRate(sftpTask, 0, configuration.getDbConfiguration().getPollFrequencySeconds() * 1000);
		}
		catch (Exception e)
		{
			LOG.error("Fatal exception occurred", e);
		}
	}
}

