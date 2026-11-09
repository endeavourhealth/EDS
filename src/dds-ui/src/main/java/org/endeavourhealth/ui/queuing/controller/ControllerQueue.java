package org.endeavourhealth.ui.queuing.controller;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.endeavourhealth.ui.queuing.ChannelFacade;
import org.endeavourhealth.ui.queuing.Message;
import org.endeavourhealth.ui.queuing.QueueConnectionProperties;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ControllerQueue implements AutoCloseable {

    private final ChannelFacade channel;
    private final String queueName;

    public ControllerQueue(
            QueueConnectionProperties connectionProperties,
            String queueName) throws IOException, TimeoutException {

        this.queueName = queueName;
        channel = new ChannelFacade(connectionProperties);
    }

    @Override
    public void close() throws Exception {
        channel.close();
    }

    public void registerReceiver(IControllerQueueMessageReceiver receiver) throws IOException {

        final Consumer consumer = new DefaultConsumer(channel.getInternalChannel()) {

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                Message message = new Message(properties, new String(body));

                if (ControllerQueueWorkItemCompleteMessage.isTypeOf(message)) {
                    ControllerQueueWorkItemCompleteMessage realMessage = ControllerQueueWorkItemCompleteMessage.CreateFromMessage(message);
                    receiver.receiveWorkerItemCompleteMessage(realMessage.getPayload());
                } else if (ControllerQueueExecutionFailedMessage.isTypeOf(message)) {
                    ControllerQueueExecutionFailedMessage realMessage = ControllerQueueExecutionFailedMessage.CreateFromMessage(message);
                    receiver.receiveExecutionFailedMessage(realMessage.getPayload());
                } else if (ControllerQueueProcessorNodeCompleteMessage.isTypeOf(message)) {
                    ControllerQueueProcessorNodeCompleteMessage realMessage = ControllerQueueProcessorNodeCompleteMessage.CreateFromMessage(message);
                    receiver.receiveProcessorNodeCompleteMessage(realMessage.getPayload());
                } else if (ControllerQueueProcessorNodeStartedMessage.isTypeOf(message)) {
                    ControllerQueueProcessorNodeStartedMessage realMessage = ControllerQueueProcessorNodeStartedMessage.CreateFromMessage(message);
                    receiver.receiveProcessorNodeStartedMessage(realMessage.getPayload());
                } else if (ControllerQueueRequestStartMessage.isTypeOf(message)) {
                    receiver.requestStart();
                } else if (ControllerQueueRequestStopMessage.isTypeOf(message)) {
                    receiver.requestStop();
                } else {
                    throw new UnsupportedOperationException("Message type not supported: " + properties.getType());
                }

                channel.basicAck(envelope.getDeliveryTag());
            }
        };

        channel.basicConsume(queueName, consumer);
    }

    public void sendMessage(ControllerQueueWorkItemCompleteMessage message) throws IOException {
        channel.publishDirectlyToQueue(queueName, message.getMessage());
    }

    public void sendMessage(ControllerQueueExecutionFailedMessage message) throws IOException {
        channel.publishDirectlyToQueue(queueName, message.getMessage());
    }

    public void sendMessage(ControllerQueueProcessorNodeCompleteMessage message) throws IOException {
        channel.publishDirectlyToQueue(queueName, message.getMessage());
    }

    public void sendMessage(ControllerQueueProcessorNodeStartedMessage message) throws IOException {
        channel.publishDirectlyToQueue(queueName, message.getMessage());
    }

    public void sendMessage(ControllerQueueRequestStartMessage message) throws IOException {
        channel.publishDirectlyToQueue(queueName, message.getMessage());
    }

    public void sendMessage(ControllerQueueRequestStopMessage message) throws IOException {
        channel.publishDirectlyToQueue(queueName, message.getMessage());
    }

    public interface IControllerQueueMessageReceiver {
        void receiveWorkerItemCompleteMessage(ControllerQueueWorkItemCompleteMessage.WorkItemCompletePayload payload);
        void receiveExecutionFailedMessage(ControllerQueueExecutionFailedMessage.ExecutionFailedPayload payload);
        void receiveProcessorNodeCompleteMessage(ControllerQueueProcessorNodeCompleteMessage.ProcessorNodeCompletePayload payload);
        void receiveProcessorNodeStartedMessage(ControllerQueueProcessorNodeStartedMessage.ProcessorNodeStartedPayload payload);
        void requestStart();
        void requestStop();
    }
}
