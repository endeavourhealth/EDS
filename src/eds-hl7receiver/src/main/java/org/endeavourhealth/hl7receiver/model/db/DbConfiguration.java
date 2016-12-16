package org.endeavourhealth.hl7receiver.model.db;

import java.util.List;

public class DbConfiguration {

    private Integer instanceId;
    private List<DbChannel> dbChannels;

    public Integer getInstanceId() {
        return instanceId;
    }

    public DbConfiguration setInstanceId(Integer instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public List<DbChannel> getDbChannels() {
        return dbChannels;
    }

    public DbConfiguration setDbChannels(List<DbChannel> dbChannels) {
        this.dbChannels = dbChannels;
        return this;
    }
}
