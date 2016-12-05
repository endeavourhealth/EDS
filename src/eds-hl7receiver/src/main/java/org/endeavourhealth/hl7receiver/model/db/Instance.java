package org.endeavourhealth.hl7receiver.model.db;

public class Instance {
    private String instanceId;
    private String instanceDescription;

    public String getInstanceId() {
        return instanceId;
    }

    public Instance setInstanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public String getInstanceDescription() {
        return instanceDescription;
    }

    public Instance setInstanceDescription(String instanceDescription) {
        this.instanceDescription = instanceDescription;
        return this;
    }
}
