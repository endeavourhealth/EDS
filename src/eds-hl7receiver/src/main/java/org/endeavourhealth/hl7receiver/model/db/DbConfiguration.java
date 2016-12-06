package org.endeavourhealth.hl7receiver.model.db;

import java.util.List;

public class DbConfiguration {

    private DbInstance dbInstance;
    private List<DbChannel> dbChannels;

    public DbInstance getDbInstance() {
        return dbInstance;
    }

    public DbConfiguration setDbInstance(DbInstance dbInstance) {
        this.dbInstance = dbInstance;
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
