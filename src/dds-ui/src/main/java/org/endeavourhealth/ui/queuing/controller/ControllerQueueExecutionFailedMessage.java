package org.endeavourhealth.ui.queuing.controller;

import com.rabbitmq.client.AMQP;
import org.endeavourhealth.common.utility.JsonSerializer;
import org.endeavourhealth.ui.queuing.Message;

import java.util.UUID;

public class ControllerQueueExecutionFailedMessage {

    private Message message;
    private ExecutionFailedPayload payload;
    private static final String type = "executionFailed";

    public Message getMessage() {
        return message;
    }

    public ExecutionFailedPayload getPayload() {
        return payload;
    }

    public static class ExecutionFailedPayload {
        private UUID executionUuid;
        private UUID processorUuid;
        private String hostIpAddress;
        private String exceptionMessage;

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

        public String getHostIpAddress() {
            return hostIpAddress;
        }

        public void setHostIpAddress(String hostIpAddress) {
            this.hostIpAddress = hostIpAddress;
        }

        public String getExceptionMessage() {
            return exceptionMessage;
        }

        public void setExceptionMessage(String exceptionMessage) {
            this.exceptionMessage = exceptionMessage;
        }
    }

    private ControllerQueueExecutionFailedMessage(Message message) {
        this.message = message;
        this.payload = JsonSerializer.deserialize(message.getBody(), ExecutionFailedPayload.class);
    }

    public static ControllerQueueExecutionFailedMessage CreateFromMessage(Message message) {
        return new ControllerQueueExecutionFailedMessage(message);
    }

    public static ControllerQueueExecutionFailedMessage CreateAsNew(
            UUID executionUuid,
            UUID processorUuid,
            String hostIpAddress,
            String exceptionMessage) {

        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();

        AMQP.BasicProperties properties = builder
                .type(type)
                .build();

        ExecutionFailedPayload payload = new ExecutionFailedPayload();
        payload.setExecutionUuid(executionUuid);
        payload.setProcessorUuid(processorUuid);
        payload.setHostIpAddress(hostIpAddress);
        payload.setExceptionMessage(exceptionMessage);

        String serialisedMessage = JsonSerializer.serialize(payload);
        Message newMessage = new Message(properties, serialisedMessage);

        return new ControllerQueueExecutionFailedMessage(newMessage);
    }

    public static boolean isTypeOf(Message message) {
        return type.equals(message.getProperties().getType());
    }
}
