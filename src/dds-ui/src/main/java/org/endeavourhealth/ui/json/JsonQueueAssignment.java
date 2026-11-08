package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonQueueAssignment {

    private String queueName = null;
    private String type = null;
    private String memory = null;
    private String[] serverNumbers = null;

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String[] getServerNumbers() {
        return serverNumbers;
    }

    public void setServerNumbers(String[] serverNumbers) {
        this.serverNumbers = serverNumbers;
    }
}
