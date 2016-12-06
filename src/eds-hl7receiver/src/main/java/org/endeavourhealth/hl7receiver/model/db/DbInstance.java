package org.endeavourhealth.hl7receiver.model.db;

public class DbInstance {
    private String instanceId;
    private String instanceName;
    private String instanceDescription;

    public String getInstanceId() {
        return instanceId;
    }

    public DbInstance setInstanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public DbInstance setInstanceName(String instanceName) {
        this.instanceName = instanceName;
        return this;
    }

    public String getInstanceDescription() {
        return instanceDescription;
    }

    public DbInstance setInstanceDescription(String instanceDescription) {
        this.instanceDescription = instanceDescription;
        return this;
    }
}
