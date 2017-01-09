package org.endeavourhealth.core.messaging.pipeline.components;

import com.google.common.base.Strings;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.endeavourhealth.core.cache.ObjectMapperPool;
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
		UUID messageUuid = exchange.getExchangeId();
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
		if (Strings.isNullOrEmpty(multicastHeader)) {

			LOG.trace("Posting exchange {} to Rabbit exchange {} with routing key {}", messageUuid, config.getExchange(), routingKey);
			publishMessage(routingKey, messageUuid, channel, properties);

		} else {
			LOG.trace("Posting exchange {} to Rabbit exchange {} with routing key {} and multicast header {}", messageUuid, config.getExchange(), routingKey, multicastHeader);
			String multicastData = exchange.getHeader(multicastHeader);

			//adding handler for when we're missing multicast header data
			if (Strings.isNullOrEmpty(multicastData)) {
				throw new PipelineException("No multicast data for " + multicastHeader + " to post exchange " + exchange.getExchangeId() + " to " + config.getExchange());
			}

			try {
				Object[] multicastItems = ObjectMapperPool.getInstance().readValue(multicastData, Object[].class);

				for (Object multicastItem : multicastItems) {
					String itemData = ObjectMapperPool.getInstance().writeValueAsString(multicastItem);
					// Replace header list with individual value
					headers.put(multicastHeader, itemData);
					properties = properties.builder().headers(headers).build();
					publishMessage(routingKey, messageUuid, channel, properties);
				}
			} catch (IOException e) {
				throw new PipelineException("Could not parse multicast data", e);
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
			LOG.error("Unable to publish message", e);
			throw new PipelineException("Unable to publish message: " + e.getMessage(), e);
		}
	}

	private Channel getChannel(Connection connection) throws PipelineException {
		Channel channel;
		try {
			channel = ConnectionManager.getPublishChannel(connection, config.getExchange());
			channel.confirmSelect();
		} catch (IOException e) {
			LOG.error("Unable to get publish channel", e);
			throw new PipelineException("Unable to get publish channel: " + e.getMessage(), e);
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
			LOG.error("Unable to connect to rabbit", e);
			throw new PipelineException("Unable to connect to Rabbit : " + e.getMessage(), e);
		} catch (TimeoutException e) {
			LOG.error("Connection to Rabbit timed out", e);
			throw new PipelineException("Connection to rabbit timed out : " + e.getMessage(), e);
		}
	}

	private String getRoutingKey(Exchange exchange) {
		String routingIdentifier = "Unknown";

	if (config.getRoutingHeader() != null && !config.getRoutingHeader().isEmpty())
			routingIdentifier = exchange.getHeader(config.getRoutingHeader());

		return RoutingManager.getInstance().getRoutingKeyForIdentifier(routingIdentifier);
	}
}
