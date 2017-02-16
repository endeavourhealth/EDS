package org.endeavourhealth.patientexplorer.endpoints;

import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.admin.models.*;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.patientexplorer.models.*;
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
public final class FolderEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(FolderEndpoint.class);
    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Folders);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getFolders")
    public Response getFolders(@Context SecurityContext sc, @QueryParam("folderType") int folderType, @QueryParam("parentUuid") String parentUuidStr) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Folders",
            "Folder Type", folderType,
            "Parent Uuid", parentUuidStr);

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

    private UUID parseUuidFromStr(String uuidStr) {
        if (uuidStr == null || uuidStr.isEmpty()) {
            return null;
        } else {
            return UUID.fromString(uuidStr);
        }
    }
}
