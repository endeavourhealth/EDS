package org.endeavourhealth.ui.queuing.controller;

import com.rabbitmq.client.AMQP;
import org.endeavourhealth.ui.queuing.Message;

public class ControllerQueueRequestStopMessage {

    private Message message;
    private static final String type = "requestStop";

    public Message getMessage() {
        return message;
    }

    private ControllerQueueRequestStopMessage(Message message) {
        this.message = message;
    }

    public static ControllerQueueRequestStopMessage CreateFromMessage(Message message) {
        return new ControllerQueueRequestStopMessage(message);
    }

    public static ControllerQueueRequestStopMessage CreateAsNew() {

        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();

        AMQP.BasicProperties properties = builder
                .type(type)
                .build();

        Message newMessage = new Message(properties, null);

        return new ControllerQueueRequestStopMessage(newMessage);
    }

    public static boolean isTypeOf(Message message) {
        return type.equals(message.getProperties().getType());
    }
}
