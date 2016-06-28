package org.endeavourhealth.ui.database.definition;

import org.endeavourhealth.ui.database.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DbAudit extends DbAbstractTable {

    private static final TableAdapter adapter = new TableAdapter(DbAudit.class);

    @DatabaseColumn
    @PrimaryKeyColumn
    private UUID auditUuid = null;
    @DatabaseColumn
    private UUID endUserUuid = null;
    @DatabaseColumn
    private Instant timeStamp = null;
    @DatabaseColumn
    @IdentityColumn
    private Integer auditVersion = null;
    @DatabaseColumn
    private UUID organisationUuid = null;

    public DbAudit() {}

    public static DbAudit factoryNow(UUID endUserUuid, UUID organisationUuid) {
        DbAudit ret = new DbAudit();
        ret.setAuditUuid(UUID.randomUUID()); //always explicitly set a new UUID as we'll always want to use it
        ret.setSaveMode(TableSaveMode.INSERT);
        ret.setEndUserUuid(endUserUuid);
        ret.setTimeStamp(Instant.now());
        ret.setOrganisationUuid(organisationUuid);
        return ret;
    }

    public static List<DbAudit> retrieveForActiveItems(List<DbActiveItem> activeItems) throws Exception {
        List<UUID> uuids = new ArrayList<>();
        for (DbActiveItem activeItem: activeItems) {
            uuids.add(activeItem.getAuditUuid());
        }
        return retrieveForUuids(uuids);
    }
    public static List<DbAudit> retrieveForUuids(List<UUID> uuids) throws Exception {
        return DatabaseManager.db().retrieveAuditsForUuids(uuids);
    }

    public static DbAudit retrieveForUuid(UUID auditUuid) throws Exception {
        return DatabaseManager.db().retrieveForPrimaryKeys(DbAudit.class, auditUuid);
    }

    public static DbAudit retrieveLatest() throws Exception {
        return DatabaseManager.db().retrieveLatestAudit();
    }

    @Override
    public TableAdapter getAdapter() {
        return adapter;
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

    public UUID getEndUserUuid() {
        return endUserUuid;
    }

    public void setEndUserUuid(UUID endUserUuid) {
        this.endUserUuid = endUserUuid;
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Instant timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Integer getAuditVersion() {
        return auditVersion;
    }

    public void setAuditVersion(Integer auditVersion) {
        this.auditVersion = auditVersion;
    }

    public UUID getOrganisationUuid() {
        return organisationUuid;
    }

    public void setOrganisationUuid(UUID organisationUuid) {
        this.organisationUuid = organisationUuid;
    }
}
