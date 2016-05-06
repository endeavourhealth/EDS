package org.endeavourhealth.core.messaging.protocols;

import org.endeavourhealth.core.messaging.configuration.schema.serviceConfiguration.SftpListener;
import org.endeavourhealth.core.messaging.configuration.schema.serviceConfiguration.SftpReceiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class SftpReceivePortManager {
	private List<SftpHandler> consumers;

	public SftpReceivePortManager() {
		consumers = new ArrayList<>();
	}

	public void registerListener(String serviceId, SftpListener sftpListener) throws Exception {
		// Create a channel & consumer for each receiver
		for (SftpReceiver receiver : sftpListener.getReceiver()) {
			SftpHandler consumer = new SftpHandler(
					sftpListener.getCredentials().getUsername(),
					sftpListener.getCredentials().getPassword(),
					sftpListener.getHost(),
					sftpListener.getPort(),
					receiver);
			consumers.add(consumer);
		}
	}

	public void start() throws IOException {
		for (SftpHandler consumer : consumers)
			consumer.start();
	}

	public void shutdown() throws IOException, TimeoutException {
		for (SftpHandler consumer : consumers)
			consumer.stop();
	}
}
