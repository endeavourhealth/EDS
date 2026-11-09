package org.endeavourhealth.ui.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JsonSubscriberPublisherService {

    private UUID uuid; //service UUID
    private String name; //org name
    private String alias; //other org name
    private String odsCode;
    private Map<String, String> tags;
    private List<JsonSubscriberPublisherSystem> systemStatus = new ArrayList<>();

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getOdsCode() {
        return odsCode;
    }

    public void setOdsCode(String odsCode) {
        this.odsCode = odsCode;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public List<JsonSubscriberPublisherSystem> getSystemStatus() {
        return systemStatus;
    }

    public void setSystemStatus(List<JsonSubscriberPublisherSystem> systemStatus) {
        this.systemStatus = systemStatus;
    }
}
