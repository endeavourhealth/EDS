package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonQueueServer {

    private String serverNumber;
    private String hostName;
    private String ipAddress;
    private String awsInstanceId;
    private String queues;

    public JsonQueueServer() {
    }

    public String getServerNumber() {
        return serverNumber;
    }

    public void setServerNumber(String serverNumber) {
        this.serverNumber = serverNumber;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getAwsInstanceId() {
        return awsInstanceId;
    }

    public void setAwsInstanceId(String awsInstanceId) {
        this.awsInstanceId = awsInstanceId;
    }

    public String getQueues() {
        return queues;
    }

    public void setQueues(String queues) {
        this.queues = queues;
    }
}
