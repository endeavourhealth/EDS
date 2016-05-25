package org.endeavourhealth.core.messaging.pipeline.components;

import com.rabbitmq.client.*;
import org.endeavourhealth.core.configuration.Pipeline;
import org.endeavourhealth.core.configuration.PostMessageToExchangeConfig;
import org.endeavourhealth.core.configuration.RMQExchange;
import org.endeavourhealth.core.configuration.RMQQueue;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.PropertyKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.queueing.ConnectionManager;
import org.endeavourhealth.core.queueing.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class PostMessageToExchange implements PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(PostMessageToExchange.class);

	private PostMessageToExchangeConfig config;

	public PostMessageToExchange(PostMessageToExchangeConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws IOException, PipelineException, TimeoutException {
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
			channel.basicPublish(
					config.getExchange(),
					(String)exchange.getProperty(PropertyKeys.Sender),
					properties,
					exchange.getBody().getBytes());
			if (!channel.waitForConfirms())
				throw new PipelineException("Messages posted but not confirmed");
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
