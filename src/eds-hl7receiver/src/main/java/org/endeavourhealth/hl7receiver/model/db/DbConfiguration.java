package org.endeavourhealth.hl7receiver.model.db;

import java.util.List;

public class DbConfiguration {

    private Integer instanceId;
    private List<DbChannel> dbChannels;
    private DbEds dbEds;
    private List<DbNotificationRetryInterval> dbNotificationRetryIntervals;

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

    public DbEds getDbEds() {
        return dbEds;
    }

    public DbConfiguration setDbEds(DbEds dbEds) {
        this.dbEds = dbEds;
        return this;
    }

    public List<DbNotificationRetryInterval> getDbNotificationRetryIntervals() {
        return dbNotificationRetryIntervals;
    }

    public DbConfiguration setDbNotificationRetryInterval(List<DbNotificationRetryInterval> dbNotificationRetryIntervals) {
        this.dbNotificationRetryIntervals = dbNotificationRetryIntervals;
        return this;
    }
}
