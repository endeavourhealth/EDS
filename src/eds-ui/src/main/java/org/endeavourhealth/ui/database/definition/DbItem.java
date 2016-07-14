package org.endeavourhealth.ui.database.definition;

import org.endeavourhealth.core.data.admin.models.DefinitionItemType;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.ui.DependencyType;
import org.endeavourhealth.ui.database.*;
import org.endeavourhealth.ui.querydocument.QueryDocumentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DbItem extends DbAbstractTable {

    private static final TableAdapter adapter = new TableAdapter(DbItem.class);

    @DatabaseColumn
    @PrimaryKeyColumn
    private UUID itemUuid = null;
    @DatabaseColumn
    @PrimaryKeyColumn
    private UUID auditUuid = null;
    @DatabaseColumn
    private String xmlContent = null;
    @DatabaseColumn
    private String title = null;
    @DatabaseColumn
    private String description = null;
    @DatabaseColumn
    private boolean isDeleted = false;

    public DbItem() {
    }

    public static DbItem factoryNew(String title, DbAudit audit) {
        DbItem ret = new DbItem();
        ret.setAuditUuid(audit.getAuditUuid());
        ret.setTitle(title);
        return ret;
    }

    public static DbItem retrieveLatestForUUid(UUID itemUuid) throws Exception {
        return DatabaseManager.db().retrieveLatestItemForUuid(itemUuid);
    }

    public static DbItem retrieveForActiveItem(DbActiveItem activeItem) throws Exception {
        return retrieveForUuidAndAudit(activeItem.getItemUuid(), activeItem.getAuditUuid());
    }

    public static DbItem retrieveForUuidAndAudit(UUID uuid, UUID auditUuid) throws Exception {
        return DatabaseManager.db().retrieveForPrimaryKeys(DbItem.class, uuid, auditUuid);
    }

    public static List<DbItem> retrieveDependentItems(UUID itemUuid, DependencyType dependencyType) throws Exception {
        return DatabaseManager.db().retrieveDependentItems(itemUuid, dependencyType);
    }

    public static List<DbItem> retrieveNonDependentItems(UUID organisationUuid, DependencyType dependencyType, DefinitionItemType itemType) throws Exception {
        return DatabaseManager.db().retrieveNonDependentItems(organisationUuid, dependencyType, itemType);
    }

    public static List<DbItem> retrieveForActiveItems(List<DbActiveItem> activeItems) throws Exception {
        return DatabaseManager.db().retrieveItemsForActiveItems(activeItems);
    }

    public static List<DbItem> retrieveLatestForUuids(List<UUID> itemUuids) throws Exception {
        return DatabaseManager.db().retrieveLatestItemsForUuids(itemUuids);
    }

    public static List<LibraryItem> retrieveLibraryItemsForJob(UUID jobUuid) throws Exception {

        List<DbItem> sourceItems = DatabaseManager.db().retrieveItemsForJob(jobUuid);
        List<LibraryItem> libraryItems = new ArrayList<>();

        for (DbItem item: sourceItems) {

            UUID itemUuid = item.getItemUuid();
            String xml = item.getXmlContent();
            LibraryItem libraryItem = QueryDocumentSerializer.readLibraryItemFromXml(xml);

            UUID libraryItemUuid = UUID.fromString(libraryItem.getUuid());

            if (!itemUuid.equals(libraryItemUuid))
                throw new Exception("Database item UUID does not match LibraryItem content: " + itemUuid);

            libraryItems.add(libraryItem);
        }

        return libraryItems;
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

    public String getXmlContent() {
        return xmlContent;
    }

    public void setXmlContent(String content) {
        this.xmlContent = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

}
