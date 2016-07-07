package org.endeavourhealth.ui.database.definition;

import org.endeavourhealth.core.data.admin.models.DefinitionItemType;
import org.endeavourhealth.ui.DependencyType;
import org.endeavourhealth.ui.database.*;

import java.util.List;
import java.util.UUID;

public final class DbActiveItem extends DbAbstractTable {

    private static final TableAdapter adapter = new TableAdapter(DbActiveItem.class);

    @DatabaseColumn
    @PrimaryKeyColumn
    private UUID activeItemUuid = null;
    @DatabaseColumn
    private UUID organisationUuid = null;
    @DatabaseColumn
    private UUID itemUuid = null;
    @DatabaseColumn
    private UUID auditUuid = null;
    @DatabaseColumn
    private DefinitionItemType itemTypeId = null;
    @DatabaseColumn
    private boolean isDeleted = false;

    public DbActiveItem() {
    }

    public static DbActiveItem factoryNew(DbItem item, UUID organisationUuid, DefinitionItemType itemType) {
        UUID itemUuid = item.getItemUuid();
        UUID auditUuid = item.getAuditUuid();

        if (itemUuid == null) {
            throw new RuntimeException("Cannot create ActiveItem without first saving Item to DB");
        }

        DbActiveItem ret = new DbActiveItem();
        ret.setOrganisationUuid(organisationUuid);
        ret.setItemUuid(itemUuid);
        ret.setAuditUuid(auditUuid);
        ret.setItemTypeId(itemType);

        return ret;
    }

    public static DbActiveItem retrieveForItemUuid(UUID itemUuid) throws Exception {
        return DatabaseManager.db().retrieveActiveItemForItemUuid(itemUuid);
    }

    public static int retrieveCountDependencies(UUID itemUuid, DependencyType dependencyType) throws Exception {
        return DatabaseManager.db().retrieveCountDependencies(itemUuid, dependencyType);
    }

    public static List<DbActiveItem> retrieveDependentItems(UUID orgUuid, UUID itemUuid, DependencyType dependencyType) throws Exception {
        return DatabaseManager.db().retrieveActiveItemDependentItems(orgUuid, itemUuid, dependencyType);
    }

    @Override
    public TableAdapter getAdapter() {
        return adapter;
    }

    /**
     * gets/sets
     */
    public UUID getActiveItemUuid() {
        return activeItemUuid;
    }

    public void setActiveItemUuid(UUID activeItemUuid) {
        this.activeItemUuid = activeItemUuid;
    }

    public UUID getOrganisationUuid() {
        return organisationUuid;
    }

    public void setOrganisationUuid(UUID organisationUuid) {
        this.organisationUuid = organisationUuid;
    }

    public UUID getItemUuid() {
        return itemUuid;
    }

    public void setItemUuid(UUID itemUuid) {
        this.itemUuid = itemUuid;
    }

    public UUID getAuditUuid() {
        return auditUuid;
    }

    public void setAuditUuid(UUID auditUuid) {
        this.auditUuid = auditUuid;
    }

    public DefinitionItemType getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(DefinitionItemType itemType) {
        this.itemTypeId = itemType;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
