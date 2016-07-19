package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.admin.models.ActiveItem;
import org.endeavourhealth.core.data.admin.models.Audit;
import org.endeavourhealth.core.data.admin.models.Item;
import org.endeavourhealth.core.data.admin.models.ItemDependency;
import org.endeavourhealth.core.security.annotations.RequiresAdmin;
import org.endeavourhealth.ui.json.*;
import org.endeavourhealth.core.data.admin.models.DefinitionItemType;
import org.endeavourhealth.ui.DependencyType;

import org.endeavourhealth.core.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;

/**
 * Endpoint for functions related to creating and managing folders
 */
@Path("/folder")
public final class FolderEndpoint extends AbstractItemEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(FolderEndpoint.class);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/saveFolder")
    @RequiresAdmin
    public Response saveFolder(@Context SecurityContext sc, JsonFolder folderParameters) throws Exception {
        super.setLogbackMarkers(sc);

        UUID folderUuid = folderParameters.getUuid();
        String folderName = folderParameters.getFolderName();
        Integer folderType = folderParameters.getFolderType();
        UUID parentUuid = folderParameters.getParentFolderUuid();

        UUID orgUuid = getOrganisationUuidFromToken(sc);
        UUID userUuid = SecurityUtils.getCurrentUserId(sc);

        //work out the ItemType, either from the parameters passed up or from our parent folder
        //if the folder type wasn't specified, see if we can derive it from our parent
        DefinitionItemType itemType = null;

        LibraryRepository repository = new LibraryRepository();

        //if folder type was passed up from client
        if (folderType != null) {
            if (folderType == JsonFolder.FOLDER_TYPE_LIBRARY) {
                itemType = DefinitionItemType.LibraryFolder;
            } else if (folderType == JsonFolder.FOLDER_TYPE_REPORTS) {
                itemType = DefinitionItemType.ReportFolder;
            } else {
                throw new BadRequestException("Invalid folder type " + folderType);
            }
        }

        //if we're amending an existing folder, we can use its item type
        else if (folderUuid != null) {
            ActiveItem activeItem = repository.getActiveItemByItemId(folderUuid);
            itemType = DefinitionItemType.values()[activeItem.getItemTypeId()];
        }
        //if we're creating a new folder, we can get the item type from our parent
        else if (parentUuid != null) {
            ActiveItem parentActiveItem = repository.getActiveItemByItemId(parentUuid);
            itemType = DefinitionItemType.values()[parentActiveItem.getItemTypeId()];
        } else {
            throw new BadRequestException("Must specify folder type");
        }

        LOG.trace("SavingFolder FolderUUID {}, FolderName {} FolderType {} ParentUUID {} ItemType {}", folderUuid, folderName, folderType, parentUuid, itemType);

        //before letting our superclass do the normal item saving,
        //validate that we're not making a folder a child of itself
        if (parentUuid != null
                && folderUuid != null) {

            UUID currentParentUuid = parentUuid;
            while (currentParentUuid != null) {
                if (currentParentUuid.equals(folderUuid)) {
                    throw new BadRequestException("Cannot move a folder to be a child of itself");
                }

                ActiveItem activeItem = repository.getActiveItemByItemId(currentParentUuid);
                Iterable<ItemDependency> parents = repository.getItemDependencyByTypeId(activeItem.getItemId(), activeItem.getAuditId(), DependencyType.IsChildOf.getValue());
                if (!parents.iterator().hasNext()) {
                    currentParentUuid = null;
                } else {
                    currentParentUuid = parents.iterator().next().getDependentItemId();
                }
            }
        }

        boolean inserting = folderUuid == null;
        if (inserting) {
            folderUuid = UUID.randomUUID();
        }

        super.saveItem(inserting, folderUuid, orgUuid, userUuid, itemType, folderName, "", null, parentUuid);

        //return the UUID of the folder we just saved or updated
        JsonFolder ret = new JsonFolder();
        ret.setUuid(folderUuid);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/deleteFolder")
    @RequiresAdmin
    public Response deleteFolder(@Context SecurityContext sc, JsonFolder folderParameters) throws Exception {
        super.setLogbackMarkers(sc);

        UUID orgUuid = getOrganisationUuidFromToken(sc);
        UUID userUuid = SecurityUtils.getCurrentUserId(sc);

        UUID folderUuid = folderParameters.getUuid();

        LOG.trace("DeletingFolder FolderUUID {}", folderUuid);

        LibraryRepository repository = new LibraryRepository();

        //to delete it, we need to find out the item type
        ActiveItem activeItem = repository.getActiveItemByItemId(folderUuid);
        DefinitionItemType itemType = DefinitionItemType.values()[activeItem.getItemTypeId()];
        if (itemType != DefinitionItemType.LibraryFolder
                && itemType != DefinitionItemType.ReportFolder) {
            throw new BadRequestException("UUID is a " + itemType + " not a folder");
        }

        JsonDeleteResponse ret = deleteItem(folderUuid, orgUuid, userUuid);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getFolders")
    public Response getFolders(@Context SecurityContext sc, @QueryParam("folderType") int folderType, @QueryParam("parentUuid") String parentUuidStr) throws Exception {
        super.setLogbackMarkers(sc);

        //convert the nominal folder type to the actual Item DefinitionType
        DefinitionItemType itemType = null;
        if (folderType == JsonFolder.FOLDER_TYPE_LIBRARY) {
            itemType = DefinitionItemType.LibraryFolder;
        } else {
            throw new BadRequestException("Invalid folder type " + folderType);
        }

        UUID orgUuid = getOrganisationUuidFromToken(sc);

        LOG.trace("GettingFolders under parent UUID {} and folderType {}, which is itemType {}", parentUuidStr, folderType, itemType);

        Iterable<ActiveItem> activeItems = null;
        List<Item> items = new ArrayList();
        Iterable<ItemDependency> itemDependency = null;

        LibraryRepository repository = new LibraryRepository();

        //if we have no parent, then we're looking for the TOP-LEVEL folder
        if (parentUuidStr == null) {
            activeItems = repository.getActiveItemByOrgAndTypeId(orgUuid, itemType.getValue(), false);

            for (ActiveItem activeItem: activeItems) {
                itemDependency = repository.getItemDependencyByItemId(activeItem.getItemId());

                if (!itemDependency.iterator().hasNext()) {
                    Item item = repository.getItemByKey(activeItem.getItemId(), activeItem.getAuditId());
                    if (item.getIsDeleted()==false)
                        items.add(item);
                }
            }

            //if we don't have a top-level folder, for some reason, re-create it
            if (items.size() == 0) {
                UUID userUuid = SecurityUtils.getCurrentUserId(sc);
                FolderEndpoint.createTopLevelFolder(orgUuid, userUuid, itemType);

                //then re-run the select
                activeItems = repository.getActiveItemByOrgAndTypeId(orgUuid, itemType.getValue(), false);

                for (ActiveItem activeItem: activeItems) {
                    itemDependency = repository.getItemDependencyByItemId(activeItem.getItemId());

                    if (!itemDependency.iterator().hasNext()) {
                        Item item = repository.getItemByKey(activeItem.getItemId(), activeItem.getAuditId());
                        if (item.getIsDeleted()==false)
                            items.add(item);
                    }
                }
            }
        }
        //if we have a parent, then we want the child folders under it
        else {
            UUID parentUuid = parseUuidFromStr(parentUuidStr);

            itemDependency = repository.getItemDependencyByDependentItemId(parentUuid, DependencyType.IsChildOf.getValue());

            for (ItemDependency dependency: itemDependency) {
                Iterable<ActiveItem> aItem = repository.getActiveItemByAuditId(dependency.getAuditId());
                for (ActiveItem activeItem: aItem) {
                    Item item = repository.getItemByKey(activeItem.getItemId(), activeItem.getAuditId());
                    items.add(item);
                }
            }
        }

        LOG.trace("Found {} child folders", items.size());

        JsonFolderList ret = new JsonFolderList();

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            UUID itemUuid = item.getId();

            int childFolders = 1;//ActiveItem.retrieveCountDependencies(itemUuid, DependencyType.IsChildOf);
            int contentCount = 1;//ActiveItem.retrieveCountDependencies(itemUuid, DependencyType.IsContainedWithin);

            JsonFolder folder = new JsonFolder(item, contentCount, childFolders > 0);
            ret.add(folder);
        }

        Collections.sort(ret.getFolders());

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    public static void createTopLevelFolder(UUID organisationUuid, UUID userUuid, DefinitionItemType itemType) throws Exception {

        LOG.trace("Creating top-level folder of type {}", itemType);

        LibraryRepository repository = new LibraryRepository();

        String title = null;
        if (itemType == DefinitionItemType.LibraryFolder) {
            title = "Library";
        } else {
            throw new RuntimeException("Trying to create folder for type " + itemType);
        }

        List<Object> toSave = new ArrayList<>();

        Audit audit = Audit.factoryNow(userUuid, organisationUuid);
        toSave.add(audit);

        Item item = Item.factoryNew(title, audit);
        item.setXmlContent(""); //need non-null values
        item.setDescription("");
        toSave.add(item);

        ActiveItem activeItemReports = ActiveItem.factoryNew(item, organisationUuid, itemType);
        toSave.add(activeItemReports);

        // write to Cassandra
        repository.save(toSave);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getFolderContents")
    public Response getFolderContents(@Context SecurityContext sc, @QueryParam("folderUuid") String uuidStr) throws Exception {
        super.setLogbackMarkers(sc);

        LibraryRepository repository = new LibraryRepository();

        UUID folderUuid = UUID.fromString(uuidStr);
        UUID orgUuid = getOrganisationUuidFromToken(sc);

        LOG.trace("GettingFolderContents for folder {}", folderUuid);

        JsonFolderContentsList ret = new JsonFolderContentsList();

        List<ActiveItem> childActiveItems = new ArrayList();

        Iterable<ItemDependency> itemDependency = repository.getItemDependencyByDependentItemId(folderUuid, DependencyType.IsContainedWithin.getValue());

        for (ItemDependency dependency: itemDependency) {
            Iterable<ActiveItem> item = repository.getActiveItemByAuditId(dependency.getAuditId());
            for (ActiveItem activeItem: item) {
                if (activeItem.getIsDeleted()==false)
                    childActiveItems.add(activeItem);
            }
        }

        HashMap<UUID, Audit> hmAuditsByAuditUuid = new HashMap<>();
        List<Audit> audits = new ArrayList<>();
        for (ActiveItem activeItem: childActiveItems) {
            Audit audit = repository.getAuditByKey(activeItem.getAuditId());
            audits.add(audit);
        }

        for (Audit audit: audits) {
            hmAuditsByAuditUuid.put(audit.getId(), audit);
        }

        HashMap<UUID, Item> hmItemsByItemUuid = new HashMap<>();
        List<Item> items = new ArrayList<>();
        for (ActiveItem activeItem: childActiveItems) {
            Item item = repository.getItemByKey(activeItem.getItemId(), activeItem.getAuditId());
            items.add(item);
        }
        
        for (Item item: items) {
            hmItemsByItemUuid.put(item.getId(), item);
        }

        for (int i = 0; i < childActiveItems.size(); i++) {

            ActiveItem activeItem = childActiveItems.get(i);
            Item item = hmItemsByItemUuid.get(activeItem.getItemId());

            DefinitionItemType itemType = DefinitionItemType.values()[activeItem.getItemTypeId()];
            Audit audit = hmAuditsByAuditUuid.get(item.getAuditId());

            JsonFolderContent c = new JsonFolderContent(activeItem, item, audit);
            ret.addContent(c);

            //and set any extra data we need
            if (itemType == DefinitionItemType.Query) {

            } else if (itemType == DefinitionItemType.Test) {

            } else if (itemType == DefinitionItemType.Resource) {

            } else if (itemType == DefinitionItemType.CodeSet) {

            } else if (itemType == DefinitionItemType.DataSet) {

            } else if (itemType == DefinitionItemType.Protocol) {

            } else if (itemType == DefinitionItemType.System) {

            } else {
                throw new RuntimeException("Unexpected content " + item + " in folder");
            }
        }

        if (ret.getContents() != null) {
            Collections.sort(ret.getContents());
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

}
