package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonJarVersion {
    private String configId;
    private String jarPath;
    private String timestamp;
    private String serverHostName;

    public JsonJarVersion() {}

    public String getConfigId() { return configId; }
    public void setConfigId(String configId) { this.configId = configId; }
    public String getJarPath() { return jarPath; }
    public void setJarPath(String jarPath) { this.jarPath = jarPath; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getServerHostName() { return serverHostName; }
    public void setServerHostName(String serverHostName) { this.serverHostName = serverHostName; }
}
