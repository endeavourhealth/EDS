package org.endeavourhealth.queuereader;

import com.rabbitmq.client.*;
import org.endeavourhealth.queuereader.configuration.model.QueueReaderConfiguration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitHandler {
	private QueueReaderConfiguration configuration;
	private Connection connection;
	private Channel channel;
	private RabbitConsumer consumer;

	public RabbitHandler(QueueReaderConfiguration configuration) throws Exception {
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
		connectionFactory.setUsername(configuration.getCredentials().getUsername());
		connectionFactory.setPassword(configuration.getCredentials().getPassword());

		Address[] addresses = Address.parseAddresses(configuration.getNodes());

		return connectionFactory.newConnection(addresses);
	}

	public void start() throws IOException {
		// Declare queue
		channel.queueDeclare(configuration.getQueue(), false, false, false, null);

		// Bind queue to exchange on all routing keys
		String routingKeyList = configuration.getRoutingKeys();
		String[] routingKeys = routingKeyList.split(",", -1);

		for(String routingKey : routingKeys)
			channel.queueBind(configuration.getQueue(), configuration.getExchange(), routingKey);

		// Begin consuming messages
		channel.basicConsume(configuration.getQueue(), false, consumer);
	}

	public void stop() throws IOException, TimeoutException {
		// Unbind queue from exchange on all routing keys
		String routingKeyList = configuration.getRoutingKeys();
		String[] routingKeys = routingKeyList.split(",", -1);

		for(String routingKey : routingKeys)
			channel.queueUnbind(configuration.getQueue(), configuration.getExchange(), routingKey);

		// Close channel
		channel.close();
	}

}
