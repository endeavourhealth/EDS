package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.admin.models.*;
import org.endeavourhealth.ui.json.*;
import org.endeavourhealth.ui.DependencyType;
import org.endeavourhealth.ui.querydocument.QueryDocumentSerializer;
import org.endeavourhealth.ui.querydocument.models.*;
import org.endeavourhealth.ui.querydocument.models.System;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
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
        System system = libraryItem.getSystem();

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
        } else if (system != null) {
            type = DefinitionItemType.System;
            system.setName(name);
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
            if (system != null) {
                system.setUuid(libraryItemUuid.toString());
                for (TechnicalInterface technicalInterface : system.getTechnicalInterface()) {
                    technicalInterface.setUuid(UUID.randomUUID().toString());
                }
            }
        }
        else {
            if (system != null) {
                for (TechnicalInterface technicalInterface : system.getTechnicalInterface()) {
                    if (technicalInterface.getUuid() == null)
                        technicalInterface.setUuid(UUID.randomUUID().toString());
                }
            }
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getSystems")
    public Response getSystems(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        DefinitionItemType itemType = DefinitionItemType.System;

        UUID orgUuid = getOrganisationUuidFromToken(sc);

        Iterable<ActiveItem> activeItems = null;
        List<Item> items = new ArrayList();
        Iterable<ItemDependency> itemDependency = null;

        LibraryRepository repository = new LibraryRepository();

        activeItems = repository.getActiveItemByOrgAndTypeId(orgUuid, itemType.getValue(), false);

        for (ActiveItem activeItem: activeItems) {
            Item item = repository.getItemByKey(activeItem.getItemId(), activeItem.getAuditId());
            if (item.getIsDeleted()==false)
                items.add(item);
        }

        List<JsonSystem> ret = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            UUID itemUuid = item.getId();

            JsonSystem system = new JsonSystem();
            system.setName(item.getTitle());
            system.setUuid(item.getId().toString());

            String systemXml = item.getXmlContent();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(systemXml));

            Document doc = builder.parse(inputSource);

            doc.getDocumentElement().normalize ();

            NodeList listOfEntries = doc.getElementsByTagName("technicalInterface");

            List<TechnicalInterface> technicalInterfaces = new ArrayList<>();

            for(int s=0; s<listOfEntries.getLength() ; s++){
                Node firstEntryNode = listOfEntries.item(s);
                if(firstEntryNode.getNodeType() == Node.ELEMENT_NODE){
                    Element firstEntryElement = (Element)firstEntryNode;
                    NodeList uuidList = firstEntryElement.getElementsByTagName("uuid");
                    Element uuidElement = (Element)uuidList.item(0);
                    NodeList textuuidList = uuidElement.getChildNodes();
                    String uuid = ((Node)textuuidList.item(0)).getNodeValue();

                    NodeList nameList = firstEntryElement.getElementsByTagName("name");
                    Element nameElement = (Element)nameList.item(0);
                    NodeList textnameList = nameElement.getChildNodes();
                    String name = ((Node)textnameList.item(0)).getNodeValue();

                    TechnicalInterface inter = new TechnicalInterface();
                    inter.setUuid(uuid);
                    inter.setName(name);
                    technicalInterfaces.add(inter);
                }
            }

            system.setTechnicalInterface(technicalInterfaces);
            ret.add(system);
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getQueries")
    public Response getQueries(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        DefinitionItemType itemType = DefinitionItemType.Query;

        UUID orgUuid = getOrganisationUuidFromToken(sc);

        Iterable<ActiveItem> activeItems = null;
        List<Item> items = new ArrayList();
        Iterable<ItemDependency> itemDependency = null;

        LibraryRepository repository = new LibraryRepository();

        activeItems = repository.getActiveItemByOrgAndTypeId(orgUuid, itemType.getValue(), false);

        for (ActiveItem activeItem: activeItems) {
            Item item = repository.getItemByKey(activeItem.getItemId(), activeItem.getAuditId());
            if (item.getIsDeleted()==false)
                items.add(item);
        }

        List<JsonQuery> ret = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            UUID itemUuid = item.getId();

            JsonQuery query = new JsonQuery();
            query.setName(item.getTitle());
            query.setUuid(item.getId().toString());
            ret.add(query);
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getListReports")
    public Response getListReports(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        DefinitionItemType itemType = DefinitionItemType.ListOutput;

        UUID orgUuid = getOrganisationUuidFromToken(sc);

        Iterable<ActiveItem> activeItems = null;
        List<Item> items = new ArrayList();
        Iterable<ItemDependency> itemDependency = null;

        LibraryRepository repository = new LibraryRepository();

        activeItems = repository.getActiveItemByOrgAndTypeId(orgUuid, itemType.getValue(), false);

        for (ActiveItem activeItem: activeItems) {
            Item item = repository.getItemByKey(activeItem.getItemId(), activeItem.getAuditId());
            if (item.getIsDeleted()==false)
                items.add(item);
        }

        List<JsonListReport> ret = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            UUID itemUuid = item.getId();

            JsonListReport listReport = new JsonListReport();
            listReport.setName(item.getTitle());
            listReport.setUuid(item.getId().toString());
            ret.add(listReport);
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

}









