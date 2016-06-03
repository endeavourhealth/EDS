package org.endeavourhealth.ui.database.execution;

import org.endeavourhealth.ui.database.*;

import java.util.List;
import java.util.UUID;

public final class DbJobContent extends DbAbstractTable {

    private static final TableAdapter adapter = new TableAdapter(DbJobContent.class);

    @DatabaseColumn
    @PrimaryKeyColumn
    private UUID jobUuid = null;
    @PrimaryKeyColumn
    @DatabaseColumn
    private UUID itemUuid = null;
    @DatabaseColumn
    private UUID auditUuid = null;

    @Override
    public TableAdapter getAdapter() {
        return adapter;
    }

    public static List<DbJobContent> retrieveForJob(UUID jobUuid) throws Exception {
        return DatabaseManager.db().retrieveJobContentsForJob(jobUuid);
    }

    /**
     * gets/sets
     */
    public UUID getAuditUuid() {
        return auditUuid;
    }

    public void setAuditUuid(UUID auditUuid) {
        this.auditUuid = auditUuid;
    }

    public UUID getItemUuid() {
        return itemUuid;
    }

    public void setItemUuid(UUID itemUuid) {
        this.itemUuid = itemUuid;
    }

    public UUID getJobUuid() {
        return jobUuid;
    }

    public void setJobUuid(UUID jobUuid) {
        this.jobUuid = jobUuid;
    }
}
