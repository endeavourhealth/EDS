package org.endeavourhealth.ui.queuing.controller;

import com.rabbitmq.client.AMQP;
import org.endeavourhealth.common.utility.JsonSerializer;
import org.endeavourhealth.ui.queuing.Message;

import java.util.UUID;

public class ControllerQueueWorkItemCompleteMessage {

    private Message message;
    private WorkItemCompletePayload payload;
    private static final String type = "workItemComplete";

    public Message getMessage() {
        return message;
    }

    public WorkItemCompletePayload getPayload() {
        return payload;
    }

    public static class WorkItemCompletePayload {
        private UUID executionUuid;
        private long startId;
        private UUID processorNodeId;

        public WorkItemCompletePayload() {
        }

        public WorkItemCompletePayload(UUID executionUuid, long startId, UUID processorNodeId) {
            this.executionUuid = executionUuid;
            this.startId = startId;
            this.processorNodeId = processorNodeId;
        }

        public UUID getExecutionUuid() {
            return executionUuid;
        }

        public void setExecutionUuid(UUID executionUuid) {
            this.executionUuid = executionUuid;
        }

        public long getStartId() {
            return startId;
        }

        public void setStartId(long startId) {
            this.startId = startId;
        }

        public void setProcessorNodeId(UUID processorNodeId) {
            this.processorNodeId = processorNodeId;
        }

        public UUID getProcessorNodeId() {
            return processorNodeId;
        }
    }

    private ControllerQueueWorkItemCompleteMessage(Message message) {
        this.message = message;
        this.payload = JsonSerializer.deserialize(message.getBody(), WorkItemCompletePayload.class);
    }

    public static ControllerQueueWorkItemCompleteMessage CreateFromMessage(Message message) {
        return new ControllerQueueWorkItemCompleteMessage(message);
    }

    public static ControllerQueueWorkItemCompleteMessage CreateAsNew(
            UUID executionUuid,
            long startId) {

        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();

        AMQP.BasicProperties properties = builder
                .type(type)
                .build();

        WorkItemCompletePayload payload = new WorkItemCompletePayload();
        payload.setExecutionUuid(executionUuid);
        payload.setStartId(startId);

        String serialisedMessage = JsonSerializer.serialize(payload);
        Message newMessage = new Message(properties, serialisedMessage);

        return new ControllerQueueWorkItemCompleteMessage(newMessage);
    }

    public static boolean isTypeOf(Message message) {
        if (type.equals(message.getProperties().getType()))
            return true;
        else
            return false;
    }
}
