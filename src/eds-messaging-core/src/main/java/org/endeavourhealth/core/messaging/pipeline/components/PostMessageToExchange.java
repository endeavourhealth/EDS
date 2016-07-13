package org.endeavourhealth.core.messaging.pipeline.components;

import com.rabbitmq.client.*;
import org.endeavourhealth.core.configuration.PostMessageToExchangeConfig;
import org.endeavourhealth.core.data.admin.QueuedMessageRepository;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.queueing.ConnectionManager;
import org.endeavourhealth.core.queueing.RoutingManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class PostMessageToExchange extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(PostMessageToExchange.class);

	private PostMessageToExchangeConfig config;

	public PostMessageToExchange(PostMessageToExchangeConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {
		String routingKey = getRoutingKey(exchange);

		// Generate message identifier and store message in db
		UUID messageUuid = UUID.randomUUID();
		new QueuedMessageRepository().save(messageUuid, exchange.getBody());

		Connection connection = getConnection();
		Channel channel = getChannel(connection);

		Map<String, Object> headers = new HashMap<>();
		for (String key : exchange.getHeaders().keySet())
			headers.put(key, exchange.getHeader(key));

		AMQP.BasicProperties properties = new AMQP.BasicProperties()
				.builder()
				.deliveryMode(2)    // Persistent message
				.headers(headers)
				.build();


		// Handle multicast
		String multicastHeader = config.getMulticastHeader();
		if (multicastHeader == null || multicastHeader.isEmpty()) {
			publishMessage(routingKey, messageUuid, channel, properties);
		} else {
			String[] multicastValues = exchange.getHeader(multicastHeader).split(",", -1);

			for (String multicastValue : multicastValues) {
				// Replace header list with individual value
				headers.put(multicastHeader, multicastValue);
				properties = properties.builder().headers(headers).build();
				publishMessage(routingKey, messageUuid, channel, properties);
			}
		}

		waitForConfirmations(channel);
		closeChannel(channel);
	}

	private void closeChannel(Channel channel) {
		if (channel != null)
			try {
				channel.close();
			} catch (IOException e) {
				LOG.warn("Couldn't close Rabbit channel : ", e);
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
	}

	private void waitForConfirmations(Channel channel) throws PipelineException {
		try {
			if (!channel.waitForConfirms())
				throw new PipelineException("Messages posted but not confirmed");
			else
				LOG.debug("Message posted to exchange");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void publishMessage(String routingKey, UUID messageUuid, Channel channel, AMQP.BasicProperties properties) throws PipelineException {
		try {
			channel.basicPublish(
					config.getExchange(),
					routingKey,
					properties,
					messageUuid.toString().getBytes());
		} catch (IOException e) {
			LOG.error("Unable to publish message");
			throw new PipelineException("Unable to publish message: " + e.getMessage());
		}
	}

	private Channel getChannel(Connection connection) throws PipelineException {
		Channel channel;
		try {
			channel = ConnectionManager.getPublishChannel(connection, config.getExchange());
			channel.confirmSelect();
		} catch (IOException e) {
			LOG.error("Unable to get publish channel");
			throw new PipelineException("Unable to get publish channel: " + e.getMessage());
		}
		return channel;
	}

	private Connection getConnection() throws PipelineException {
		try {
			return ConnectionManager.getConnection(
							config.getCredentials().getUsername(),
							config.getCredentials().getPassword(),
							config.getNodes()
					);
		} catch (IOException e) {
			LOG.error("Unable to connect to rabbit");
			throw new PipelineException("Unable to connect to Rabbit : " + e.getMessage());
		} catch (TimeoutException e) {
			LOG.error("Connection to Rabbit timed out");
			throw new PipelineException("Connection to rabbit timed out : " + e.getMessage());
		}
	}

	private String getRoutingKey(Exchange exchange) {
		String routingIdentifier = "Unknown";

	if (config.getRoutingHeader() != null && !config.getRoutingHeader().isEmpty())
			routingIdentifier = exchange.getHeader(config.getRoutingHeader());

		return RoutingManager.getInstance().getRoutingKeyForIdentifier(routingIdentifier);
	}
}
