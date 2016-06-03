package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.admin.models.ActiveItem;
import org.endeavourhealth.core.data.admin.models.Item;
import org.endeavourhealth.core.data.admin.models.ItemDependency;
import org.endeavourhealth.ui.json.JsonDeleteResponse;
import org.endeavourhealth.ui.json.JsonFolderContent;
import org.endeavourhealth.ui.json.JsonFolderContentsList;
import org.endeavourhealth.ui.json.JsonMoveItems;
import org.endeavourhealth.core.data.admin.models.DefinitionItemType;
import org.endeavourhealth.ui.DependencyType;
import org.endeavourhealth.ui.querydocument.QueryDocumentSerializer;
import org.endeavourhealth.ui.querydocument.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.UUID;

@Path("/library")
public final class LibraryEndpoint extends AbstractItemEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(LibraryEndpoint.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getLibraryItem")
    public Response getLibraryItem(@Context SecurityContext sc, @QueryParam("uuid") String uuidStr) throws Exception {
        super.setLogbackMarkers(sc);

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

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/saveLibraryItem")
    public Response saveLibraryItem(@Context SecurityContext sc, LibraryItem libraryItem) throws Exception {
        super.setLogbackMarkers(sc);

        LibraryRepository repository = new LibraryRepository();

        UUID orgUuid = getOrganisationUuidFromToken(sc);
        UUID userUuid = getEndUserUuidFromToken(sc);

        UUID libraryItemUuid = parseUuidFromStr(libraryItem.getUuid());
        String name = libraryItem.getName();
        String description = libraryItem.getDescription();
        UUID folderUuid = parseUuidFromStr(libraryItem.getFolderUuid());

        Query query = libraryItem.getQuery();
        DataSource dataSource = libraryItem.getDataSource();
        Test test = libraryItem.getTest();
        CodeSet codeSet = libraryItem.getCodeSet();
        ListReport listOutput = libraryItem.getListReport();
        Protocol protocol = libraryItem.getProtocol();

        LOG.trace("SavingLibraryItem UUID {}, Name {} FolderUuid", libraryItemUuid, name, folderUuid);

        QueryDocument doc = new QueryDocument();
        doc.getLibraryItem().add(libraryItem);

        //work out the item type (query, test etc.) from the content passed up
        DefinitionItemType type = null;
        if (query != null) {
            type = DefinitionItemType.Query;
        } else if (dataSource != null) {
            type = DefinitionItemType.DataSource;
        } else if (test != null) {
            type = DefinitionItemType.Test;
        } else if (codeSet != null) {
            type = DefinitionItemType.CodeSet;
        } else if (listOutput != null) {
            type = DefinitionItemType.ListOutput;
        } else if (protocol != null) {
            type = DefinitionItemType.Protocol;
        } else {
            //if we've been passed no proper content, we might just be wanting to rename an existing item,
            //so work out the type from what's on the DB already
            if (libraryItemUuid == null) {
                throw new BadRequestException("Can't save LibraryItem without some content (e.g. query, test etc.)");
            }

            ActiveItem activeItem = repository.getActiveItemByItemId(libraryItemUuid);
            type = DefinitionItemType.values()[activeItem.getItemTypeId()];
            doc = null; //clear this, because we don't want to overwrite what's on the DB with an empty query doc
        }

        boolean inserting = libraryItemUuid == null;
        if (inserting) {
            libraryItemUuid = UUID.randomUUID();
            libraryItem.setUuid(libraryItemUuid.toString());
        }

        super.saveItem(inserting, libraryItemUuid, orgUuid, userUuid, type, name, description, doc, folderUuid);

        //return the UUID of the libraryItem
        LibraryItem ret = new LibraryItem();
        ret.setUuid(libraryItemUuid.toString());

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/deleteLibraryItem")
    public Response deleteLibraryItem(@Context SecurityContext sc, LibraryItem libraryItem) throws Exception {
        super.setLogbackMarkers(sc);

        UUID libraryItemUuid = parseUuidFromStr(libraryItem.getUuid());
        UUID orgUuid = getOrganisationUuidFromToken(sc);
        UUID userUuid = getEndUserUuidFromToken(sc);

        LOG.trace("DeletingLibraryItem UUID {}", libraryItemUuid);

        JsonDeleteResponse ret = deleteItem(libraryItemUuid, orgUuid, userUuid);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getContentNamesForReportLibraryItem")
    public Response getContentNamesForReportLibraryItem(@Context SecurityContext sc, @QueryParam("uuid") String uuidStr) throws Exception {
        super.setLogbackMarkers(sc);

        LibraryRepository repository = new LibraryRepository();

        UUID itemUuid = UUID.fromString(uuidStr);

        LOG.trace("getContentNamesforReportLibraryItem for UUID {}", itemUuid);

        JsonFolderContentsList ret = new JsonFolderContentsList();

        ActiveItem activeItem = repository.getActiveItemByItemId(itemUuid);
        Iterable<ItemDependency> dependentItems = repository.getItemDependencyByTypeId(activeItem.getItemId(), activeItem.getAuditId(), DependencyType.Uses.getValue());

        for (ItemDependency dependentItem: dependentItems) {
            UUID dependentItemUuid = dependentItem.getDependentItemId();
            ActiveItem aItem = repository.getActiveItemByItemId(dependentItemUuid);

            Item item = repository.getItemByKey(aItem.getItemId(), aItem.getAuditId());

            JsonFolderContent content = new JsonFolderContent(item, null);
            ret.addContent(content);
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/moveLibraryItems")
    public Response moveLibraryItems(@Context SecurityContext sc, JsonMoveItems parameters) throws Exception {
        super.setLogbackMarkers(sc);

        UUID orgUuid = getOrganisationUuidFromToken(sc);
        UUID userUuid = getEndUserUuidFromToken(sc);

        LOG.trace("moveLibraryItems");

        super.moveItems(userUuid, orgUuid, parameters);

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }
}
