package org.endeavourhealth.queuereader;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineProcessor;
import org.endeavourhealth.core.configuration.QueueReaderConfiguration;

import java.io.UnsupportedEncodingException;

public class RabbitConsumer extends DefaultConsumer {
	PipelineProcessor pipeline;

	public RabbitConsumer(Channel channel, QueueReaderConfiguration configuration) {
		super(channel);
		pipeline = new PipelineProcessor(configuration.getPipeline());
	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] bytes) throws UnsupportedEncodingException {
		// Decode the message
		String message = new String(bytes, "UTF-8");

		Exchange exchange = new Exchange();
		exchange.body = message;

		// Process the message
		pipeline.execute(exchange);
	}
}
