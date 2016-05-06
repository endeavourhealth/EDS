package org.endeavourhealth.queuereader;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.endeavourhealth.messaging.exchange.Exchange;
import org.endeavourhealth.messaging.pipeline.PipelineFactory;
import org.endeavourhealth.queuereader.configuration.model.QueueReaderConfiguration;
import org.endeavourhealth.messaging.pipeline.MessagePipeline;

import java.io.UnsupportedEncodingException;

public class RabbitConsumer extends DefaultConsumer {
	MessagePipeline pipeline;

	public RabbitConsumer(Channel channel, QueueReaderConfiguration configuration) {
		super(channel);
		pipeline = PipelineFactory.create(configuration.getMessagePipelineClass());
	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] bytes) throws UnsupportedEncodingException {
		// Decode the message
		String message = new String(bytes, "UTF-8");

		Exchange exchange = new Exchange();
		exchange.body = message;

		// Process the message
		pipeline.process(exchange);
	}
}
