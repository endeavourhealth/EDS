package org.endeavourhealth.ui.queuing.controller;

import com.rabbitmq.client.AMQP;
import org.endeavourhealth.common.utility.JsonSerializer;
import org.endeavourhealth.ui.queuing.Message;

import java.util.UUID;

public class ControllerQueueProcessorNodeStartedMessage
{

    private Message message;
    private ProcessorNodeStartedPayload payload;
    private static final String type = "processorNodeStarted";

    public Message getMessage() {
        return message;
    }

    public ProcessorNodeStartedPayload getPayload() {
        return payload;
    }

    public static class ProcessorNodeStartedPayload {
        private UUID executionUuid;
        private UUID processorUuid;

        public UUID getExecutionUuid() {
            return executionUuid;
        }

        public void setExecutionUuid(UUID executionUuid) {
            this.executionUuid = executionUuid;
        }

        public UUID getProcessorUuid() {
            return processorUuid;
        }

        public void setProcessorUuid(UUID processorUuid) {
            this.processorUuid = processorUuid;
        }
    }

    private ControllerQueueProcessorNodeStartedMessage(Message message) {
        this.message = message;
        this.payload = JsonSerializer.deserialize(message.getBody(), ProcessorNodeStartedPayload.class);
    }

    public static ControllerQueueProcessorNodeStartedMessage CreateFromMessage(Message message) {
        return new ControllerQueueProcessorNodeStartedMessage(message);
    }

    public static ControllerQueueProcessorNodeStartedMessage CreateAsNew(UUID executionUuid, UUID processorUuid) {

        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();

        AMQP.BasicProperties properties = builder
                .type(type)
                .build();

        ProcessorNodeStartedPayload payload = new ProcessorNodeStartedPayload();
        payload.setExecutionUuid(executionUuid);
        payload.setProcessorUuid(processorUuid);

        String serialisedMessage = JsonSerializer.serialize(payload);
        Message newMessage = new Message(properties, serialisedMessage);

        return new ControllerQueueProcessorNodeStartedMessage(newMessage);
    }

    public static boolean isTypeOf(Message message) {
        return type.equals(message.getProperties().getType());
    }
}
