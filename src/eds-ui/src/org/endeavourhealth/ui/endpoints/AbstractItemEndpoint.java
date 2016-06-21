package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.admin.models.ActiveItem;
import org.endeavourhealth.core.data.admin.models.Audit;
import org.endeavourhealth.core.data.admin.models.ItemDependency;
import org.endeavourhealth.core.data.admin.models.Item;
import org.endeavourhealth.ui.json.JsonDeleteResponse;
import org.endeavourhealth.ui.json.JsonMoveItem;
import org.endeavourhealth.ui.json.JsonMoveItems;
import org.endeavourhealth.ui.utility.QueryDocumentReaderFindDependentUuids;
import org.endeavourhealth.core.data.admin.models.DefinitionItemType;
import org.endeavourhealth.ui.DependencyType;
import org.endeavourhealth.ui.querydocument.QueryDocumentSerializer;
import org.endeavourhealth.ui.querydocument.models.QueryDocument;

import javax.ws.rs.BadRequestException;
import java.util.*;

public abstract class AbstractItemEndpoint extends AbstractEndpoint {

    private static final int MAX_VALIDATION_ERRORS_FOR_DELETE = 5;

    protected ActiveItem retrieveActiveItem(UUID itemUuid, UUID orgUuid, DefinitionItemType itemTypeDesired) throws Exception {

        ActiveItem activeItem = retrieveActiveItem(itemUuid, orgUuid);

        if (activeItem.getItemTypeId() != itemTypeDesired.getValue()) {
            throw new RuntimeException("Trying to retrieve a " + itemTypeDesired + " but item is a " + activeItem.getItemTypeId());
        }
        return activeItem;
    }

    protected ActiveItem retrieveActiveItem(UUID itemUuid, UUID orgUuid) throws Exception {
        LibraryRepository repository = new LibraryRepository();
        ActiveItem activeItem = repository.getActiveItemByItemId(itemUuid);

        if (activeItem == null) {
            throw new BadRequestException("UUID does not exist");
        }

        if (!activeItem.getOrganisationId().equals(orgUuid)) {
            throw new BadRequestException("Item for another organisation");
        }

        return activeItem;
    }

    protected JsonDeleteResponse deleteItem(UUID itemUuid, UUID orgUuid, UUID userUuid) throws Exception {
        ActiveItem activeItem = retrieveActiveItem(itemUuid, orgUuid);
        LibraryRepository repository = new LibraryRepository();
        Item item = repository.getItemByKey(activeItem.getItemId(), activeItem.getAuditId());

        //recursively build up the full list of items we want to delete
        List<Item> itemsToDelete = new ArrayList<>();
        List<ActiveItem> activeItemsToDelete = new ArrayList<>();
        findItemsToDelete(item, activeItem, itemsToDelete, activeItemsToDelete);

        JsonDeleteResponse ret = new JsonDeleteResponse();

        //now do the deleting, building up a list of all entities to update, which is then done atomically
        List<Object> toSave = new ArrayList<>();

        Audit audit = Audit.factoryNow(userUuid, orgUuid);
        toSave.add(audit);
        UUID auditUuid = audit.getId();

        for (Item itemToDelete: itemsToDelete) {
            itemToDelete.setAuditId(auditUuid);
            itemToDelete.setIsDeleted(true);
            toSave.add(itemToDelete);
        }

        for (ActiveItem activeItemToDelete: activeItemsToDelete) {
            activeItemToDelete.setAuditId(auditUuid);
            activeItemToDelete.setIsDeleted(true);
            toSave.add(activeItemToDelete);
        }

        repository.save(toSave);
        //DatabaseManager.db().writeEntities(toSave);
        return ret;
    }


    private void findItemsToDelete(Item item, ActiveItem activeItem, List<Item> itemsToDelete, List<ActiveItem> activeItemsToDelete) throws Exception {

        itemsToDelete.add(item);
        activeItemsToDelete.add(activeItem);

        LibraryRepository repository = new LibraryRepository();
        Iterable<ItemDependency> dependencies = repository.getItemDependencyByItemId(item.getId());
        for (ItemDependency dependency: dependencies) {

            //only recurse for containing or child folder-type dependencies
            if (dependency.getDependencyTypeId() == DependencyType.IsChildOf.getValue()
                    || dependency.getDependencyTypeId() == DependencyType.IsContainedWithin.getValue()) {
                ActiveItem childActiveItem = repository.getActiveItemByItemId(dependency.getItemId());
                Item childItem = repository.getItemByKey(childActiveItem.getItemId(), childActiveItem.getAuditId());
                //findItemsToDelete(childItem, childActiveItem, itemsToDelete, activeItemsToDelete);
            }
        }
    }

    private static void validateItemTypeMatchesContainingFolder(boolean insert, DefinitionItemType itemType, UUID containingFolderUuid) throws Exception {

        //if saving a new library item, it must have a containing folder item
        if (insert
                && containingFolderUuid == null
                && itemType != DefinitionItemType.LibraryFolder) {
            throw new BadRequestException("LibraryItems must have a containing folder UUID");
        }

        //if saving a folder or we're AMENDING a library item, then there's notning more to validate
        if (containingFolderUuid == null) {
            return;
        }

        LibraryRepository repository = new LibraryRepository();

        ActiveItem containingFolderActiveItem = repository.getActiveItemByItemId(containingFolderUuid);
        DefinitionItemType containingFolderType = DefinitionItemType.values()[containingFolderActiveItem.getItemTypeId()];

        if (containingFolderType == DefinitionItemType.LibraryFolder) {
            //library folders can only contain other library folders and queries etc.
            if (itemType != DefinitionItemType.LibraryFolder
                    && itemType != DefinitionItemType.CodeSet
                    && itemType != DefinitionItemType.Query
                    && itemType != DefinitionItemType.ListOutput
                    && itemType != DefinitionItemType.Protocol
                    && itemType != DefinitionItemType.System) {
                throw new BadRequestException("Library folder UUID " + containingFolderUuid + " cannot contain a " + itemType);
            }
        } else {
            throw new BadRequestException("Parent folder UUID " + containingFolderUuid + " isn't a folder");
        }
    }

    protected void saveItem(boolean insert, UUID itemUuid, UUID orgUuid, UUID userUuid, DefinitionItemType itemType,
                            String name, String description, QueryDocument queryDocument, UUID containingFolderUuid) throws Exception {

        LibraryRepository repository = new LibraryRepository();

        //validate the containing folder type matches the itemType we're saving
        validateItemTypeMatchesContainingFolder(insert, itemType, containingFolderUuid);

        ActiveItem activeItem = null;
        Item item = null;

        if (insert) {
            //if creating a NEW item, we need to validate we have the content we need
            if (name == null) {
                throw new BadRequestException("No name specified");
            }
            if (description == null) {
                //we can live without a description, but need a non-null value
                description = "";
            }
            if (containingFolderUuid == null
                    && itemType != DefinitionItemType.LibraryFolder ) {
                throw new BadRequestException("Must specify a containing folder for new items");
            }

            activeItem = new ActiveItem();
            activeItem.setOrganisationId(orgUuid);
            activeItem.setItemId(itemUuid);
            activeItem.setItemTypeId(itemType.getValue());
            activeItem.setIsDeleted(false);

            item = new Item();
            item.setId(itemUuid);
            item.setIsDeleted(false);
            item.setXmlContent(""); //when creating folders, we don't store XML, so this needs to be non-null
        } else {
            activeItem = retrieveActiveItem(itemUuid, orgUuid, itemType);
            item = repository.getItemByKey(activeItem.getItemId(), activeItem.getAuditId());
        }

        UUID previousAuditUuid = activeItem.getAuditId();

        //update the AuditUuid on both objects
        Audit audit = Audit.factoryNow(userUuid, orgUuid);
        activeItem.setAuditId(audit.getId());
        item.setAuditId(audit.getId());

        if (name != null) {
            item.setTitle(name);
        }
        if (description != null) {
            item.setDescription(description);
        }
        if (queryDocument != null) {
            String xmlContent = QueryDocumentSerializer.writeToXml(queryDocument);
            item.setXmlContent(xmlContent);
        }

        if (activeItem.getId()==null) {
            activeItem.setId(UUID.randomUUID());
        }

        //build up a list of entities to save, so they can all be inserted atomically
        List<Object> toSave = new ArrayList<>();
        toSave.add(audit);
        toSave.add(item);
        toSave.add(activeItem);

        //work out any UUIDs our new item is dependent on
        if (queryDocument != null) {
            createUsingDependencies(queryDocument, activeItem, toSave);
        } else if (item.getXmlContent().length() > 0) {
            //if a new queryDocument wasn't provided, but the item already had one, we still need to recreate the "using" dependencies
            QueryDocument oldQueryDocument = QueryDocumentSerializer.readQueryDocumentFromXml(item.getXmlContent());
            createUsingDependencies(oldQueryDocument, activeItem, toSave);
        }

        //work out the child/contains dependency
        ItemDependency itemDependency = createFolderDependency(insert, itemType, item, previousAuditUuid, containingFolderUuid, toSave);

        // write to Cassandra
        repository.save(toSave);
    }

    /**
     * when a libraryItem is saved, process the query document to find all UUIDs that it requires to be run
     */
    private static void createUsingDependencies(QueryDocument queryDocument, ActiveItem activeItem, List<Object> toSave) throws Exception {

        //find all the UUIDs in the XML and then see if we need to create or delete dependencies
        QueryDocumentReaderFindDependentUuids reader = new QueryDocumentReaderFindDependentUuids(queryDocument);
        HashSet<UUID> uuidsInDoc = reader.findUuids();

        Iterator<UUID> iter = uuidsInDoc.iterator();
        while (iter.hasNext()) {
            UUID uuidInDoc = iter.next();

            ItemDependency dependency = new ItemDependency();
            dependency.setItemId(activeItem.getItemId());
            dependency.setAuditId(activeItem.getAuditId());
            dependency.setDependentItemId(uuidInDoc);
            dependency.setDependencyTypeId(DependencyType.Uses.getValue());

            toSave.add(dependency);
        }
    }

    /**
     * when a libraryItem, or folder is saved, link it to the containing folder
     */
    private static ItemDependency createFolderDependency(boolean insert, DefinitionItemType itemType, Item item, UUID previousAuditUuid, UUID containingFolderUuid, List<Object> toSave) throws Exception {
        LibraryRepository repository = new LibraryRepository();

        //work out the dependency type, based on what item type we're saving
        DependencyType dependencyType = null;
        if (itemType == DefinitionItemType.LibraryFolder) {
            //if we're saving a folder, we're working with "child of" dependencies
            dependencyType = DependencyType.IsChildOf;
        } else {
            //if we're saving anything else, we're working with "contained within" dependencies
            dependencyType = DependencyType.IsContainedWithin;
        }

        if (containingFolderUuid == null) {

            //if saving a new item without a folder, then return out as there's no dependency to create
            if (insert) {
                return null;
            }

            //if we're just renaming an item, then no folder UUID would have been supplied,
            //so find out the old folder UUID so we can maintain the relationship
            Iterable<ItemDependency> oldFolderDependencies = repository.getItemDependencyByTypeId(item.getId(), previousAuditUuid, dependencyType.getValue());

            for (ItemDependency dependency: oldFolderDependencies) {
                containingFolderUuid = dependency.getDependentItemId();
            }
        }

        ItemDependency linkToParent = new ItemDependency();
        linkToParent.setItemId(item.getId());
        linkToParent.setAuditId(item.getAuditId());
        linkToParent.setDependentItemId(containingFolderUuid);
        linkToParent.setDependencyTypeId(dependencyType.getValue());
        toSave.add(linkToParent);

        return linkToParent;
    }

    protected UUID parseUuidFromStr(String uuidStr) {
        if (uuidStr == null || uuidStr.isEmpty()) {
            return null;
        } else {
            return UUID.fromString(uuidStr);
        }
    }

    protected void moveItems(UUID userUuid, UUID orgUuid, JsonMoveItems parameters) throws Exception {

        LibraryRepository repository = new LibraryRepository();

        UUID folderUuid = parameters.getDestinationFolder();
        if (folderUuid == null) {
            throw new BadRequestException("No destination folder UUID supplied");
        }
        ActiveItem folderActiveItem = repository.getActiveItemByItemId(folderUuid);
        if (!folderActiveItem.getOrganisationId().equals(orgUuid)) {
            throw new BadRequestException("Cannot move items to folder owned by another organisation");
        }

        List<Object> toSave = new ArrayList<>();

        Audit audit = Audit.factoryNow(userUuid, orgUuid);
        UUID auditUuid = audit.getId();
        toSave.add(audit);

        for (JsonMoveItem itemParameter: parameters.getItems()) {
            UUID itemUuid = itemParameter.getUuid();

            ActiveItem activeItem = repository.getActiveItemByItemId(itemUuid);
            Item item = repository.getItemByKey(activeItem.getId(), activeItem.getAuditId());

            if (!activeItem.getOrganisationId().equals(orgUuid)) {
                throw new BadRequestException("Cannot move items belonging to another organisation");
            }

            item.setAuditId(auditUuid);
            toSave.add(item);

            UUID previousAuditUuid = activeItem.getAuditId();
            activeItem.setAuditId(auditUuid);
            toSave.add(activeItem);

            //"using" dependencies
            QueryDocument oldQueryDocument = QueryDocumentSerializer.readQueryDocumentFromXml(item.getXmlContent());
            createUsingDependencies(oldQueryDocument, activeItem, toSave);

            //folder dependecies
            createFolderDependency(false,  DefinitionItemType.values()[activeItem.getItemTypeId()], item, previousAuditUuid, folderUuid, toSave);
        }

        repository.save(toSave);
    }
}

