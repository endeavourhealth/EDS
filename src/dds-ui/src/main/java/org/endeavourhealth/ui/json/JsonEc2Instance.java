package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonEc2Instance {
    private String instanceId;
    private String instanceName;
    private String instanceType;
    private String state;
    private String instanceStatus;
    private String systemStatus;

    public JsonEc2Instance() {}

    public String getInstanceId() { return instanceId; }
    public void setInstanceId(String instanceId) { this.instanceId = instanceId; }
    public String getInstanceName() { return instanceName; }
    public void setInstanceName(String instanceName) { this.instanceName = instanceName; }
    public String getInstanceType() { return instanceType; }
    public void setInstanceType(String instanceType) { this.instanceType = instanceType; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getInstanceStatus() { return instanceStatus; }
    public void setInstanceStatus(String instanceStatus) { this.instanceStatus = instanceStatus; }
    public String getSystemStatus() { return systemStatus; }
    public void setSystemStatus(String systemStatus) { this.systemStatus = systemStatus; }
}
