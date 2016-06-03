package org.endeavourhealth.ui.queuing;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class ChannelFacade implements AutoCloseable {
    private Connection connection;
    private Channel channel;
    private Object queues;

    public Channel getInternalChannel() {
        return channel;
    }

    public ChannelFacade(QueueConnectionProperties connectionProperties) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost(connectionProperties.getIpAddress());
        factory.setUsername(connectionProperties.getUsername());
        factory.setPassword(connectionProperties.getPassword());

        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.basicQos(1);  //only allow a single unacknowledged message to be pre-fetched from the server
    }

    @Override
    public void close() throws Exception {
        if (channel != null && channel.isOpen())
            channel.close();

        if (connection != null && connection.isOpen())
            connection.close();

        channel = null;
        connection = null;
    }

    public void publishToExchange(String exchangeName, Message message) throws IOException {
        channel.basicPublish(exchangeName, "", message.getProperties(), message.getBodyAsByteArray());
    }

    public void publishDirectlyToQueue(String queueName, Message message) throws IOException {
        channel.basicPublish("", queueName, message.getProperties(), message.getBodyAsByteArray());
    }

    public void purgeQueue(String queueName) throws IOException {
        AMQP.Queue.PurgeOk result = channel.queuePurge(queueName);

        if (result == null)
            throw new IOException("Queue purge failed");
    }

    public void queueCreate(String queueName, Long queueExpirationTimeInDays) throws IOException {
        boolean durable = false;
        boolean exclusive = false;
        boolean autoDelete = true;  //Doesn't seem to work so adding queue expiration time
        Map<String, Object> arguments = null;

        if (queueExpirationTimeInDays != null && queueExpirationTimeInDays > 0) {

            long timeInMS = queueExpirationTimeInDays * 24 * 60 * 60 * 1000;

            arguments = new HashMap<String, Object>();
            arguments.put("x-expires", timeInMS);
        }

        channel.queueDeclare(queueName, durable, exclusive, autoDelete, arguments);
    }

    public void basicAck(long deliveryTag) throws IOException {
        channel.basicAck(deliveryTag, false);
    }

    public void basicConsume(String queueName, Consumer consumer) throws IOException {
        boolean autoAck = false;
        channel.basicConsume(queueName, autoAck, consumer);
    }

    public GetResponse basicGet(String queueName) throws IOException {
        boolean autoAck = false;
        return channel.basicGet(queueName, autoAck);
    }

    public void queueBind(String queueName, String exchangeName) throws IOException {
        channel.queueBind(queueName, exchangeName, "");
    }
}
