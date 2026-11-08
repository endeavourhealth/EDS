package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonQueueAdminActionMulti {
    private String queueName;
    private String memory;
    private String[] serverNumbers;

    public JsonQueueAdminActionMulti() {}

    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }
    public String getMemory() { return memory; }
    public void setMemory(String memory) { this.memory = memory; }
    public String[] getServerNumbers() { return serverNumbers; }
    public void setServerNumbers(String[] serverNumbers) { this.serverNumbers = serverNumbers; }
}
