package org.endeavourhealth.core.messaging.pipeline.components;

import com.rabbitmq.client.*;
import org.endeavourhealth.core.configuration.PostMessageToExchangeConfig;
import org.endeavourhealth.core.data.admin.QueuedMessageRepository;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.PropertyKeys;
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

public class PostMessageToExchange implements PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(PostMessageToExchange.class);

	private PostMessageToExchangeConfig config;

	public PostMessageToExchange(PostMessageToExchangeConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws IOException, PipelineException, TimeoutException {

		// Generate message identifier and store message in db
		UUID messageUuid = UUID.randomUUID();
		new QueuedMessageRepository().save(messageUuid, exchange.getBody());

		Channel channel = null;
		try {
			Connection connection = ConnectionManager.getConnection(
					config.getCredentials().getUsername(),
					config.getCredentials().getPassword(),
					config.getNodes()
			);

			channel = ConnectionManager.getPublishChannel(connection, config.getExchange());

			Map<String, Object> headers = new HashMap<>();
			for (String key : exchange.getHeaders().keySet())
				headers.put(key, exchange.getHeader(key));

			AMQP.BasicProperties properties = new AMQP.BasicProperties()
					.builder()
					.deliveryMode(2)		// Persistent message
					.headers(headers)
					.build();

			channel.confirmSelect();

			// Publish message id to exchange for each routing key
			String[] routingKeys = ((String)exchange.getProperty(PropertyKeys.RoutingKey)).split(",", -1);

			for (String routingKey : routingKeys) {
				channel.basicPublish(
						config.getExchange(),
						RoutingManager.getInstance().getRoutingKeyForIdentifier(routingKey),
						properties,
						messageUuid.toString().getBytes());
			}

			if (!channel.waitForConfirms())
				throw new PipelineException("Messages posted but not confirmed");
			else
				LOG.debug("Message posted to exchange");
		}
		catch (TimeoutException e) {
			LOG.error("Queue connection timed out");
			throw new PipelineException(e.getMessage());
		} catch (InterruptedException e) {
			LOG.error("Unable to post message");
			throw new PipelineException("Unable to post message to exchange");
		} finally {
			if (channel != null)
				channel.close();
		}
	}
}
