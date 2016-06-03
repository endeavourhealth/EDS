package org.endeavourhealth.ui.queuing.base;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.endeavourhealth.ui.queuing.Message;

import java.io.IOException;

public class Consumer extends DefaultConsumer {
    private DeliveryReceiver receiver;

    public Consumer(Channel channel, DeliveryReceiver receiver) {
        super(channel);
        this.receiver = receiver;
    }

    @Override
    public void handleDelivery(
        String consumerTag,
        Envelope envelope,
        AMQP.BasicProperties properties,
        byte[] body)
        throws IOException
    {
        long deliveryTag = envelope.getDeliveryTag();

        System.out.println("Processing message with tag: " + deliveryTag);

        Message message = new Message(properties, new String(body));
        receiver.handleDelivery(message);

        getChannel().basicAck(deliveryTag, false);
    }

    public interface DeliveryReceiver {
        public void handleDelivery(Message message);
    }
}
