package org.endeavourhealth.core.messaging.pipeline.components;

import com.rabbitmq.client.*;
import org.endeavourhealth.core.configuration.PostMessageToQueueConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class PostMessageToQueue implements PipelineComponent {
	private PostMessageToQueueConfig config;

	public PostMessageToQueue(PostMessageToQueueConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws IOException, TimeoutException {
		Connection connection = getConnection(
				config.getCredentials().getUsername(),
				config.getCredentials().getPassword(),
				config.getNodes()
		);

		Channel channel = connection.createChannel();
		channel.exchangeDeclare(
				config.getExchange(),
				"TOPIC");

		AMQP.BasicProperties properties = new AMQP.BasicProperties();

		for (String key : exchange.getHeaders().keySet())
			properties.getHeaders().put(key, exchange.getHeader(key));

		channel.basicPublish(
				config.getExchange(),
				"DETERMINE_KEY_SOMEHOW!!!",		// TODO
				properties,
				exchange.getBody().getBytes());
	}

	private Connection getConnection(String username, String password, String nodes) throws IOException, TimeoutException {
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setAutomaticRecoveryEnabled(true);
		connectionFactory.setTopologyRecoveryEnabled(true);
		connectionFactory.setUsername(username);
		connectionFactory.setPassword(password);

		Address[] addresses = Address.parseAddresses(nodes);

		return connectionFactory.newConnection(addresses);
	}
}
