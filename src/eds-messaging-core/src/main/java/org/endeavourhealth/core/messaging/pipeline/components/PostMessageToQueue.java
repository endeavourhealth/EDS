package org.endeavourhealth.core.messaging.pipeline.components;

import com.rabbitmq.client.*;
import org.endeavourhealth.core.configuration.PostMessageToQueueConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.PropertyKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class PostMessageToQueue implements PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(PostMessageToQueue.class);

	private PostMessageToQueueConfig config;

	public PostMessageToQueue(PostMessageToQueueConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws IOException, PipelineException {
		try {
			Connection connection = getConnection(
					config.getCredentials().getUsername(),
					config.getCredentials().getPassword(),
					config.getNodes()
			);

			Channel channel = connection.createChannel();
			channel.exchangeDeclare(
					config.getExchange(),
					"topic");

			Map<String, Object> headers = new HashMap<>();
			for (String key : exchange.getHeaders().keySet())
				headers.put(key, exchange.getHeader(key));

			AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().headers(headers).build();

			channel.basicPublish(
					config.getExchange(),
					(String)exchange.getProperty(PropertyKeys.Sender),
					properties,
					exchange.getBody().getBytes());
		}
		catch (TimeoutException e) {
			LOG.error("Queue connection timed out");
			throw new PipelineException(e.getMessage());
		}
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
