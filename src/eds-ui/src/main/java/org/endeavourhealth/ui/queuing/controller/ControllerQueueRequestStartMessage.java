package org.endeavourhealth.ui.queuing.controller;

import com.rabbitmq.client.AMQP;
import org.endeavourhealth.ui.queuing.Message;

public class ControllerQueueRequestStartMessage {

    private Message message;
    private static final String type = "requestStart";

    public Message getMessage() {
        return message;
    }

    private ControllerQueueRequestStartMessage(Message message) {
        this.message = message;
    }

    public static ControllerQueueRequestStartMessage CreateFromMessage(Message message) {
        return new ControllerQueueRequestStartMessage(message);
    }

    public static ControllerQueueRequestStartMessage CreateAsNew() {

        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();

        AMQP.BasicProperties properties = builder
                .type(type)
                .build();

        Message newMessage = new Message(properties, null);

        return new ControllerQueueRequestStartMessage(newMessage);
    }

    public static boolean isTypeOf(Message message) {
        return type.equals(message.getProperties().getType());
    }
}
