package org.endeavourhealth.queuereader;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.PropertyKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineProcessor;
import org.endeavourhealth.core.configuration.QueueReaderConfiguration;

import java.io.IOException;

public class RabbitConsumer extends DefaultConsumer {
	PipelineProcessor pipeline;

	public RabbitConsumer(Channel channel, QueueReaderConfiguration configuration) {
		super(channel);
		pipeline = new PipelineProcessor(configuration.getPipeline());
	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] bytes) throws IOException {
		// Decode the message
		String message = new String(bytes, "UTF-8");

		Exchange exchange = new Exchange(message);
		exchange.setProperty(PropertyKeys.RoutingKey, envelope.getRoutingKey());

		// Process the message
		if (pipeline.execute(exchange)) {
			this.getChannel().basicAck(envelope.getDeliveryTag(), false);
		} else {
			this.getChannel().basicReject(envelope.getDeliveryTag(), false);
		}
	}
}
