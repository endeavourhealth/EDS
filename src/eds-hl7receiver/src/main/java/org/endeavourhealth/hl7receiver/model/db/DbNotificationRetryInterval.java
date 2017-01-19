package org.endeavourhealth.hl7receiver.model.db;

public class DbNotificationRetryInterval {
    private int intervalSeconds;

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public DbNotificationRetryInterval setIntervalSeconds(int intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
        return this;
    }
}
