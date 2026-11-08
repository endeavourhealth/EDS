package org.endeavourhealth.ui.json;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonQueueDefault {

    private String queuePrefix;
    private String defaultMemory;

    public JsonQueueDefault() {
    }

    public String getQueuePrefix() {
        return queuePrefix;
    }

    public void setQueuePrefix(String queuePrefix) {
        this.queuePrefix = queuePrefix;
    }

    public String getDefaultMemory() {
        return defaultMemory;
    }

    public void setDefaultMemory(String defaultMemory) {
        this.defaultMemory = defaultMemory;
    }
}
