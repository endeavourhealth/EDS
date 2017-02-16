package org.endeavourhealth.patientexplorer.endpoints;

import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.admin.models.*;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.endeavourhealth.core.xml.QueryDocumentSerializer;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.patientexplorer.models.DependencyType;
import org.endeavourhealth.patientexplorer.models.JsonFolderContent;
import org.endeavourhealth.patientexplorer.models.JsonFolderContentsList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;

@Path("/library")
public final class LibraryEndpoint extends AbstractEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(LibraryEndpoint.class);
    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Library);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getLibraryItem")
    public Response getLibraryItem(@Context SecurityContext sc, @QueryParam("uuid") String uuidStr) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "LibraryItem",
            "Item Id", uuidStr);

        UUID libraryItemUuid = UUID.fromString(uuidStr);

        LOG.trace("GettingLibraryItem for UUID {}", libraryItemUuid);
        LibraryRepository repository = new LibraryRepository();

        ActiveItem activeItem = repository.getActiveItemByItemId(libraryItemUuid);

        Item item = repository.getItemByKey(activeItem.getItemId(), activeItem.getAuditId());
        String xml = item.getXmlContent();

        LibraryItem ret = QueryDocumentSerializer.readLibraryItemFromXml(xml);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getFolderContents")
    public Response getFolderContents(@Context SecurityContext sc, @QueryParam("folderUuid") String uuidStr) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "FolderContents",
            "Folder Id", uuidStr);

        LibraryRepository repository = new LibraryRepository();

        UUID folderUuid = UUID.fromString(uuidStr);

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

            DefinitionItemType itemType = DefinitionItemType.get(activeItem.getItemTypeId());
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

            } else if (itemType == DefinitionItemType.CountReport) {

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









