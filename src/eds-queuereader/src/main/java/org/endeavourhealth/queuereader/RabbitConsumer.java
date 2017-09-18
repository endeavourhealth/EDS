package org.endeavourhealth.queuereader;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.core.configuration.QueueReaderConfiguration;
import org.endeavourhealth.core.data.admin.QueuedMessageRepository;
import org.endeavourhealth.core.data.admin.models.QueuedMessage;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class RabbitConsumer extends DefaultConsumer {
	private static final Logger LOG = LoggerFactory.getLogger(RabbitConsumer.class);

	private PipelineProcessor pipeline;
	private QueueReaderConfiguration configuration;
	private UUID lastExchangeRejected;

	public RabbitConsumer(Channel channel, QueueReaderConfiguration configuration) {
		super(channel);

		this.pipeline = new PipelineProcessor(configuration.getPipeline());
		this.configuration = configuration;
	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] bytes) throws IOException {
		// Decode the message id
		String messageId = new String(bytes, "UTF-8");
		UUID messageUuid = UUID.fromString(messageId);

		// Get the message from the db
		QueuedMessage queuedMessage = new QueuedMessageRepository().getById(messageUuid);

		//seem to get brokwn messages in dev environments, so handle for now
		if (queuedMessage == null) {
			LOG.warn("Received queued message ID " + messageId + " with no actual message");
			this.getChannel().basicAck(envelope.getDeliveryTag(), false);
			return;
		}

		Exchange exchange = new Exchange(messageUuid, queuedMessage.getMessageBody(), new Date());
		Map<String, Object> headers = properties.getHeaders();
		if (headers != null) {
			headers.keySet().stream()
					.filter(headerKey -> headers.get(headerKey) != null)
					.forEach(headerKey -> exchange.setHeader(headerKey, headers.get(headerKey).toString()));
		}
		LOG.info("Received exchange {} from Rabbit", exchange.getExchangeId());

		// Process the message
		if (pipeline.execute(exchange)) {
			//LOG.info("Successfully processed exchange {}", exchange.getExchangeId());
			this.getChannel().basicAck(envelope.getDeliveryTag(), false);
			//LOG.info("Have sent ACK for exchange {}", exchange.getExchangeId());
		} else {
			//LOG.error("Failed to process exchange {}", exchange.getExchangeId());
			this.getChannel().basicReject(envelope.getDeliveryTag(), true);
			sendSlackAlert(exchange);
			LOG.error("Have sent REJECT for exchange {}", exchange.getExchangeId());
		}
	}

	private void sendSlackAlert(Exchange exchange) {

		String queueName = configuration.getQueue();
		UUID exchangeId = exchange.getExchangeId();

		//it'll just keep failing the same exchange repeatedly, so only send the alert the first time
		if (lastExchangeRejected != null
			&& lastExchangeRejected.equals(exchangeId)) {
			return;
		}
		lastExchangeRejected = exchangeId;

		String s = "Exchange " + exchangeId + " rejected in " + queueName;

		SlackHelper.sendSlackMessage(SlackHelper.Channel.QueueReaderAlerts, s, exchange.getException());
	}
}
