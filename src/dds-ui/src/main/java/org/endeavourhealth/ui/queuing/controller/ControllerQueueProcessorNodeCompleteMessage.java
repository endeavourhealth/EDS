package org.endeavourhealth.ui.queuing.controller;

import com.rabbitmq.client.AMQP;
import org.endeavourhealth.common.utility.JsonSerializer;
import org.endeavourhealth.ui.queuing.Message;

import java.util.UUID;

public class ControllerQueueProcessorNodeCompleteMessage
{

    private Message message;
    private ProcessorNodeCompletePayload payload;
    private static final String type = "processorNodeComplete";

    public Message getMessage() {
        return message;
    }

    public ProcessorNodeCompletePayload getPayload() {
        return payload;
    }

    public static class ProcessorNodeCompletePayload {
        private UUID executionUuid;
        private UUID processorUuid;
        private int patientsRetrieved;
        private int patientsProcessed;
        private int executionDurationInSeconds;
        private int initialisationTimeInSeconds;
        private int patientRetrievalTimeInSeconds;
        private int numberOfBatches;

        public ProcessorNodeCompletePayload() {

        }

        public ProcessorNodeCompletePayload(UUID executionUuid, UUID processorUuid, int patientsRetrieved, int patientsProcessed, int executionDurationInSeconds, int initialisationTimeInSeconds, int patientRetrievalTimeInSeconds, int numberOfBatches) {
            this.executionUuid = executionUuid;
            this.processorUuid = processorUuid;
            this.patientsRetrieved = patientsRetrieved;
            this.patientsProcessed = patientsProcessed;
            this.executionDurationInSeconds = executionDurationInSeconds;
            this.initialisationTimeInSeconds = initialisationTimeInSeconds;
            this.patientRetrievalTimeInSeconds = patientRetrievalTimeInSeconds;
            this.numberOfBatches = numberOfBatches;
        }

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

        public int getPatientsRetrieved() {
            return patientsRetrieved;
        }

        public void setPatientsRetrieved(int patientsRetrieved) {
            this.patientsRetrieved = patientsRetrieved;
        }

        public int getPatientsProcessed() {
            return patientsProcessed;
        }

        public void setPatientsProcessed(int patientsProcessed) {
            this.patientsProcessed = patientsProcessed;
        }

        public int getExecutionDurationInSeconds() {
            return executionDurationInSeconds;
        }

        public void setExecutionDurationInSeconds(int executionDurationInSeconds) {
            this.executionDurationInSeconds = executionDurationInSeconds;
        }

        public int getInitialisationTimeInSeconds() {
            return initialisationTimeInSeconds;
        }

        public void setInitialisationTimeInSeconds(int initialisationTimeInSeconds) {
            this.initialisationTimeInSeconds = initialisationTimeInSeconds;
        }

        public int getPatientRetrievalTimeInSeconds() {
            return patientRetrievalTimeInSeconds;
        }

        public void setPatientRetrievalTimeInSeconds(int patientRetrievalTimeInSeconds) {
            this.patientRetrievalTimeInSeconds = patientRetrievalTimeInSeconds;
        }

        public int getNumberOfBatches() {
            return numberOfBatches;
        }

        public void setNumberOfBatches(int numberOfBatches) {
            this.numberOfBatches = numberOfBatches;
        }
    }

    private ControllerQueueProcessorNodeCompleteMessage(Message message) {
        this.message = message;
        this.payload = JsonSerializer.deserialize(message.getBody(), ProcessorNodeCompletePayload.class);
    }

    public static ControllerQueueProcessorNodeCompleteMessage CreateFromMessage(Message message) {
        return new ControllerQueueProcessorNodeCompleteMessage(message);
    }

    public static ControllerQueueProcessorNodeCompleteMessage CreateAsNew(
            UUID executionUuid, UUID processorUuid, int patientsRetrieved, int patientsProcessed, int executionDurationInSeconds, int initialisationTimeInSeconds, int patientRetrievalTimeInSeconds, int numberOfBatches) {

        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();

        AMQP.BasicProperties properties = builder
                .type(type)
                .build();

        ProcessorNodeCompletePayload payload = new ProcessorNodeCompletePayload(
                executionUuid,
                processorUuid,
                patientsRetrieved,
                patientsProcessed,
                executionDurationInSeconds,
                initialisationTimeInSeconds,
                patientRetrievalTimeInSeconds,
                numberOfBatches
        );

        String serialisedMessage = JsonSerializer.serialize(payload);
        Message newMessage = new Message(properties, serialisedMessage);

        return new ControllerQueueProcessorNodeCompleteMessage(newMessage);
    }

    public static boolean isTypeOf(Message message) {
        return type.equals(message.getProperties().getType());
    }
}
