package org.endeavourhealth.messaging.protocols;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.endeavourhealth.messaging.MessagePipeline;
import org.endeavourhealth.messaging.configuration.Configuration;
import org.endeavourhealth.messaging.configuration.schema.serviceConfiguration.RabbitReceiver;
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
	private RabbitReceiver receiver;
	private IReceiver receivePortHandler;

	public RabbitHandler(Channel channel, String serviceId, RabbitReceiver receiver) throws Exception {
		super(channel);
		this.channel = channel;
		this.serviceId = serviceId;
		this.receiver = receiver;
		Configuration configuration = Configuration.getInstance();

		receivePortHandler = configuration.getReceivePortHandler(receiver.getReceiverClass());

		channel.basicQos(1);
		channel.queueDeclare(receiver.getQueue(), false, false, false, null);
	}

	public void start() throws IOException {
		// If exchange is given then bind
		if (receiver.getExchange() != null && receiver.getRoutingKey() != null)
			channel.queueBind(receiver.getQueue(), receiver.getExchange(), receiver.getRoutingKey());

		channel.basicConsume(receiver.getQueue(), true, this);
	}

	public void stop() throws IOException, TimeoutException {
		if (receiver.getExchange() != null && receiver.getRoutingKey() != null)
			channel.queueUnbind(receiver.getQueue(), receiver.getExchange(), receiver.getRoutingKey());

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
