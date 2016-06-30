package org.endeavourhealth.queuereader;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.endeavourhealth.core.data.admin.QueuedMessageRepository;
import org.endeavourhealth.core.data.admin.models.QueuedMessage;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineProcessor;
import org.endeavourhealth.core.configuration.QueueReaderConfiguration;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class RabbitConsumer extends DefaultConsumer {
	PipelineProcessor pipeline;

	public RabbitConsumer(Channel channel, QueueReaderConfiguration configuration) {
		super(channel);
		pipeline = new PipelineProcessor(configuration.getPipeline());
	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] bytes) throws IOException {
		// Decode the message id
		String messageId = new String(bytes, "UTF-8");
		UUID messageUuid = UUID.fromString(messageId);

		// Get the message from the db
		QueuedMessage queuedMessage = new QueuedMessageRepository().getById(messageUuid);

		Exchange exchange = new Exchange(queuedMessage.getMessageBody());
		Map<String, Object> headers = properties.getHeaders();
		if (headers != null) {
			for (String headerKey : headers.keySet()) {
				exchange.setHeader(headerKey, headers.get(headerKey).toString());
			}
		}

		// Process the message
		if (pipeline.execute(exchange)) {
			this.getChannel().basicAck(envelope.getDeliveryTag(), false);
		} else {
			this.getChannel().basicReject(envelope.getDeliveryTag(), false);
		}
	}
}
