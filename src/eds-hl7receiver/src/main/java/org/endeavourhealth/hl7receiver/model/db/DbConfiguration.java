package org.endeavourhealth.hl7receiver.model.db;

public class DbConfiguration {
    private String instanceId;
    private String instanceDescription;

    public String getInstanceId() {
        return instanceId;
    }

    public DbConfiguration setInstanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public String getInstanceDescription() {
        return instanceDescription;
    }

    public DbConfiguration setInstanceDescription(String instanceDescription) {
        this.instanceDescription = instanceDescription;
        return this;
    }
}
