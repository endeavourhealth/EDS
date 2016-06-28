package org.endeavourhealth.ui.database.definition;

import org.endeavourhealth.ui.DependencyType;
import org.endeavourhealth.ui.database.*;

import java.util.List;
import java.util.UUID;

public final class DbItemDependency extends DbAbstractTable {

    //register as a DB entity
    private static final TableAdapter adapter = new TableAdapter(DbItemDependency.class);

    @DatabaseColumn
    @PrimaryKeyColumn
    private UUID itemUuid = null;
    @DatabaseColumn
    @PrimaryKeyColumn
    private UUID auditUuid = null;
    @DatabaseColumn
    @PrimaryKeyColumn
    private UUID dependentItemUuid = null;
    @DatabaseColumn
    private DependencyType dependencyTypeId = null;

    public DbItemDependency() {
    }

    /*public static DbActiveItemDependency factoryNew(DbItem item, DbItem dependentItem, DependencyType dependencyType)
    {
        UUID itemUuid = item.getPrimaryUuid();
        if (itemUuid == null)
        {
            throw new RuntimeException("Cannot create ActiveItem without first saving Item to DB");
        }

        UUID dependentItemUuid = dependentItem.getPrimaryUuid();
        if (dependentItemUuid == null)
        {
            throw new RuntimeException("Cannot create ActiveItem without first saving dependent Item to DB");
        }

        DbActiveItemDependency ret = new DbActiveItemDependency();
        ret.setItemUuid(itemUuid);
        ret.setDependentItemUuid(dependentItemUuid);
        ret.setDependencyType(dependencyType);

        return ret;
    }*/

    public static List<DbItemDependency> retrieveForActiveItem(DbActiveItem activeItem) throws Exception {
        return retrieveForItem(activeItem.getItemUuid(), activeItem.getAuditUuid());
    }

    public static List<DbItemDependency> retrieveForItem(UUID itemUuid, UUID auditUuid) throws Exception {
        return DatabaseManager.db().retrieveItemDependenciesForItem(itemUuid, auditUuid);
    }

    public static List<DbItemDependency> retrieveForActiveItemType(DbActiveItem activeItem, DependencyType dependencyType) throws Exception {
        return retrieveForItemType(activeItem.getItemUuid(), activeItem.getAuditUuid(), dependencyType);
    }

    public static List<DbItemDependency> retrieveForItemType(UUID itemUuid, UUID auditUuid, DependencyType dependencyType) throws Exception {
        return DatabaseManager.db().retrieveItemDependenciesForItemType(itemUuid, auditUuid, dependencyType);
    }

    public static List<DbItemDependency> retrieveForDependentItem(UUID dependentItemUuid) throws Exception {
        return DatabaseManager.db().retrieveItemDependenciesForDependentItem(dependentItemUuid);
    }

    public static List<DbItemDependency> retrieveForDependentItemType(UUID dependentItemUuid, DependencyType dependencyType) throws Exception {
        return DatabaseManager.db().retrieveItemDependenciesForDependentItemType(dependentItemUuid, dependencyType);
    }


    @Override
    public TableAdapter getAdapter() {
        return adapter;
    }

    /**
     * gets/sets
     */
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

    public UUID getDependentItemUuid() {
        return dependentItemUuid;
    }

    public void setDependentItemUuid(UUID dependentItemUuid) {
        this.dependentItemUuid = dependentItemUuid;
    }

    public DependencyType getDependencyTypeId() {
        return dependencyTypeId;
    }

    public void setDependencyTypeId(DependencyType dependencyType) {
        this.dependencyTypeId = dependencyType;
    }
}
