package org.endeavourhealth.hl7receiver.model.db;

import java.util.List;

public class DbConfiguration {

    private List<DbChannel> dbChannels;

    public List<DbChannel> getDbChannels() {
        return dbChannels;
    }

    public DbConfiguration setDbChannels(List<DbChannel> dbChannels) {
        this.dbChannels = dbChannels;
        return this;
    }
}
