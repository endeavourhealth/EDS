package org.endeavourhealth.messaging.protocols;

import com.rabbitmq.client.*;
import org.endeavourhealth.messaging.configuration.schema.serviceConfiguration.RabbitListener;
import org.endeavourhealth.messaging.configuration.schema.serviceConfiguration.RabbitReceiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class RabbitReceivePortManager {
	private List<Connection> connections;
	private List<RabbitHandler> consumers;

	public RabbitReceivePortManager() {
		connections = new ArrayList<>();
		consumers = new ArrayList<>();
	}

	public void registerListener(String serviceId, RabbitListener rabbitListener) throws Exception {
		// Connect to rabbit
		Connection connection = createRabbitConnection(rabbitListener);
		connections.add(connection);

		// Create a channel & consumer for each receiver
		for (RabbitReceiver receiver : rabbitListener.getReceiver()) {
			Channel channel = connection.createChannel();
			RabbitHandler consumer = new RabbitHandler(channel, serviceId, receiver);
			consumers.add(consumer);
		}
	}

	private Connection createRabbitConnection(RabbitListener rabbitListener) throws IOException, TimeoutException {
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setAutomaticRecoveryEnabled(true);
		connectionFactory.setTopologyRecoveryEnabled(true);
		connectionFactory.setUsername(rabbitListener.getCredentials().getUsername());
		connectionFactory.setPassword(rabbitListener.getCredentials().getPassword());

		Address[] addresses = Address.parseAddresses(rabbitListener.getNodes());

		return connectionFactory.newConnection(addresses);
	}

	public void start() throws IOException {
		for (RabbitHandler consumer : consumers)
			consumer.start();
	}

	public void shutdown() throws IOException, TimeoutException {
		for (RabbitHandler consumer : consumers)
			consumer.stop();

		for (Connection connection : connections)
			connection.close();
	}
}
