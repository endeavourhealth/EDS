package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonQueueAdminCommand {

    private String serverNumber;
    private String serverHostName;
    private String queueName;
    private String memory;
    private String action;
    private String targetServerNumber;

    public JsonQueueAdminCommand() {
    }

    public String getServerNumber() {
        return serverNumber;
    }

    public void setServerNumber(String serverNumber) {
        this.serverNumber = serverNumber;
    }

    public String getServerHostName() {
        return serverHostName;
    }

    public void setServerHostName(String serverHostName) {
        this.serverHostName = serverHostName;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTargetServerNumber() {
        return targetServerNumber;
    }

    public void setTargetServerNumber(String targetServerNumber) {
        this.targetServerNumber = targetServerNumber;
    }
}
