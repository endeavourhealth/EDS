package org.endeavourhealth.sftpreader;

import org.endeavourhealth.core.configuration.SftpReaderConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;

public class SftpHandler {
	private SftpReaderConfiguration configuration;
	private SftpConsumer consumer;
	private Timer timer;

	public SftpHandler(SftpReaderConfiguration configuration) {
		this.configuration = configuration;
		consumer = new SftpConsumer(configuration);
	}

	public void start() {
		timer = new Timer(true);
		timer.scheduleAtFixedRate(consumer, 0, configuration.getPolltime()*1000);
	}

	public void stop() {
		timer.cancel();
		timer = null;
	}
}