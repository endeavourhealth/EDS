package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonServerPowerAction {
    private String serverNumber;
    private String serverHostName;
    private String awsInstanceId;
    private String action;

    public JsonServerPowerAction() {}

    public String getServerNumber() { return serverNumber; }
    public void setServerNumber(String serverNumber) { this.serverNumber = serverNumber; }
    public String getServerHostName() { return serverHostName; }
    public void setServerHostName(String serverHostName) { this.serverHostName = serverHostName; }
    public String getAwsInstanceId() { return awsInstanceId; }
    public void setAwsInstanceId(String awsInstanceId) { this.awsInstanceId = awsInstanceId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
