package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonQueueAdminAction {
    private String serverNumber;
    private String serverHostName;
    private String queueName;
    private String memory;
    private String action;

    public JsonQueueAdminAction() {}

    public String getServerNumber() { return serverNumber; }
    public void setServerNumber(String serverNumber) { this.serverNumber = serverNumber; }
    public String getServerHostName() { return serverHostName; }
    public void setServerHostName(String serverHostName) { this.serverHostName = serverHostName; }
    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }
    public String getMemory() { return memory; }
    public void setMemory(String memory) { this.memory = memory; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
