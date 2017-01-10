package org.endeavourhealth.queuereader;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.endeavourhealth.core.configuration.QueueReaderConfiguration;
import org.endeavourhealth.core.queueing.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitHandler {
	private static final Logger LOG = LoggerFactory.getLogger(RabbitHandler.class);

	private QueueReaderConfiguration configuration;
	private Connection connection;
	private Channel channel;
	private RabbitConsumer consumer;

	public RabbitHandler(QueueReaderConfiguration configuration) throws Exception {
		LOG.info("Connecting to Rabbit queue {} at {}", configuration.getQueue(), RabbitConfig.getInstance().getNodes());

		this.configuration = configuration;

		// Connect to rabbit cluster
		connection = createRabbitConnection(configuration);

		// Create a channel
		channel = connection.createChannel();
		channel.basicQos(1);

		// Create consumer
		consumer = new RabbitConsumer(channel, configuration);
	}

	private Connection createRabbitConnection(QueueReaderConfiguration configuration) throws IOException, TimeoutException {
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setAutomaticRecoveryEnabled(true);
		connectionFactory.setTopologyRecoveryEnabled(true);
		connectionFactory.setUsername(RabbitConfig.getInstance().getUsername());
		connectionFactory.setPassword(RabbitConfig.getInstance().getPassword());

		LOG.trace("User: " + RabbitConfig.getInstance().getUsername());
		LOG.trace("Password: " + RabbitConfig.getInstance().getPassword());

		Address[] addresses = Address.parseAddresses(RabbitConfig.getInstance().getNodes());
		for (Address address: addresses) {
			LOG.trace("Node: " + address);
		}

		return connectionFactory.newConnection(addresses);
	}

	public void start() throws IOException {
		// Begin consuming messages
		channel.queueDeclare(
				configuration.getQueue(),
				true,		// Durable
				false, 	// Exclusive
				false, 	// Auto delete
				null);
		channel.basicConsume(configuration.getQueue(), false, consumer);
	}

	public void stop() throws IOException, TimeoutException {
		// Close channel
		channel.close();

		// Close connection
		connection.close();
	}
}
