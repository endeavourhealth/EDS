package org.endeavourhealth.messaging.protocols;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.endeavourhealth.messaging.MessagePipeline;
import org.endeavourhealth.messaging.configuration.Configuration;
import org.endeavourhealth.messaging.model.IReceiver;
import org.endeavourhealth.messaging.model.Message;
import org.endeavourhealth.messaging.model.MessageIdentity;
import org.endeavourhealth.messaging.model.RabbitMessage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeoutException;

public class RabbitHandler extends DefaultConsumer{
	private Channel channel;
	private String serviceId;
	private String queueName;
	private IReceiver receivePortHandler;

	public RabbitHandler(Channel channel, String serviceId, String queueName, String receiverClass) throws Exception {
		super(channel);
		this.channel = channel;
		this.serviceId = serviceId;
		this.queueName = queueName;
		Configuration configuration = Configuration.getInstance();

		receivePortHandler = configuration.getReceivePortHandler(receiverClass);

		channel.basicQos(1);
		channel.queueDeclare(queueName, false, false, false, null);
	}

	public void start() throws IOException {
		channel.basicConsume(queueName, true, this);
	}

	public void stop() throws IOException, TimeoutException {
		channel.close();
	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] bytes) throws UnsupportedEncodingException {
		Message message = RabbitMessage.fromRabbitMessage(properties, bytes);

		try
		{
			MessageIdentity messageIdentity = receivePortHandler.identifyMessage(message);
			message.setMessageIdentity(messageIdentity);

			MessagePipeline pipeline = new MessagePipeline();
			pipeline.Process(message);
		}
		catch (Exception e)
		{
			receivePortHandler.handleError(e);
		}

	}
}
