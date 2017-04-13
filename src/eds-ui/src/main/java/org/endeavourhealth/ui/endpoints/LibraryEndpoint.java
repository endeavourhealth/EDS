package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.data.admin.models.*;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.endeavourhealth.core.xml.QueryDocument.System;
import org.endeavourhealth.core.xml.QueryDocumentSerializer;
import org.endeavourhealth.ui.DependencyType;
import org.endeavourhealth.ui.json.*;
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
import java.util.*;

@Path("/library")
@Metrics(registry = "EdsRegistry")
public final class LibraryEndpoint extends AbstractItemEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(LibraryEndpoint.class);
    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Library);


	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.LibraryEndpoint.GetFolderContents")
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


	@GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.LibraryEndpoint.GetLibraryItem")
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

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.LibraryEndpoint.SaveLibraryItem")
    @Path("/saveLibraryItem")
    @RequiresAdmin
    public Response saveLibraryItem(@Context SecurityContext sc, LibraryItem libraryItem) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
            "LibraryItem",
            "Item", libraryItem);

        LibraryRepository repository = new LibraryRepository();

        UUID orgUuid = getOrganisationUuidFromToken(sc);
        UUID userUuid = SecurityUtils.getCurrentUserId(sc);

        UUID libraryItemUuid = parseUuidFromStr(libraryItem.getUuid());
        String name = libraryItem.getName();
        String description = libraryItem.getDescription();
        UUID folderUuid = parseUuidFromStr(libraryItem.getFolderUuid());

        Query query = libraryItem.getQuery();
        Resource resource = libraryItem.getResource();
        Test test = libraryItem.getTest();
        CodeSet codeSet = libraryItem.getCodeSet();
        DataSet dataSet = libraryItem.getDataSet();
        Protocol protocol = libraryItem.getProtocol();
        System system = libraryItem.getSystem();
        CountReport countReport = libraryItem.getCountReport();

        LOG.trace("SavingLibraryItem UUID {}, Name {} FolderUuid", libraryItemUuid, name, folderUuid);

        QueryDocument doc = new QueryDocument();
        doc.getLibraryItem().add(libraryItem);

        //work out the item type (query, test etc.) from the content passed up
        DefinitionItemType type = null;
        if (query != null) {
            type = DefinitionItemType.Query;
        } else if (resource != null) {
            type = DefinitionItemType.Resource;
        } else if (test != null) {
            type = DefinitionItemType.Test;
        } else if (codeSet != null) {
            type = DefinitionItemType.CodeSet;
        } else if (dataSet != null) {
            type = DefinitionItemType.DataSet;
        } else if (protocol != null) {
            type = DefinitionItemType.Protocol;
        } else if (system != null) {
            type = DefinitionItemType.System;
            system.setName(name);
        } else if (countReport != null) {
            type = DefinitionItemType.CountReport;
        } else {
            //if we've been passed no proper content, we might just be wanting to rename an existing item,
            //so work out the type from what's on the DB already
            if (libraryItemUuid == null) {
                throw new BadRequestException("Can't save LibraryItem without some content (e.g. query, test etc.)");
            }

            ActiveItem activeItem = repository.getActiveItemByItemId(libraryItemUuid);
            type = DefinitionItemType.get(activeItem.getItemTypeId());
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
    @Timed(absolute = true, name="EDS-UI.LibraryEndpoint.DeleteLibraryItem")
    @Path("/deleteLibraryItem")
    @RequiresAdmin
    public Response deleteLibraryItem(@Context SecurityContext sc, LibraryItem libraryItem) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
            "LibraryItem",
            "Item", libraryItem);

        UUID libraryItemUuid = parseUuidFromStr(libraryItem.getUuid());
        UUID orgUuid = getOrganisationUuidFromToken(sc);
        UUID userUuid = SecurityUtils.getCurrentUserId(sc);

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
    @Timed(absolute = true, name="EDS-UI.LibraryEndpoint.GetContentNamesForReportLibraryItem")
    @Path("/getContentNamesForReportLibraryItem")
    public Response getContentNamesForReportLibraryItem(@Context SecurityContext sc, @QueryParam("uuid") String uuidStr) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "ContentForReport",
            "ReportId", uuidStr);

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
    @Timed(absolute = true, name="EDS-UI.LibraryEndpoint.MoveLibraryItems")
    @Path("/moveLibraryItems")
    @RequiresAdmin
    public Response moveLibraryItems(@Context SecurityContext sc, JsonMoveItems parameters) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Move,
            "MoveItem",
            "Parameters", parameters);

        UUID orgUuid = getOrganisationUuidFromToken(sc);
        UUID userUuid = SecurityUtils.getCurrentUserId(sc);

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
    @Timed(absolute = true, name="EDS-UI.LibraryEndpoint.GetSystems")
    @Path("/getSystems")
    public Response getSystems(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Systems");

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
    @Timed(absolute = true, name="EDS-UI.LibraryEndpoint.GetQueries")
    @Path("/getQueries") // queries define cohorts
    public Response getQueries(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Queries");

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
    @Timed(absolute = true, name="EDS-UI.LibraryEndpoint.GetDataSets")
    @Path("/getDataSets")
    public Response getDataSets(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Data sets");

        DefinitionItemType itemType = DefinitionItemType.DataSet;

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

        List<JsonDataSet> ret = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            UUID itemUuid = item.getId();

            JsonDataSet dataSet = new JsonDataSet();
            dataSet.setName(item.getTitle());
            dataSet.setUuid(item.getId().toString());
            ret.add(dataSet);
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
    @Timed(absolute = true, name="EDS-UI.LibraryEndpoint.GetProtocols")
    @Path("/getProtocols")
    public Response getProtocols(@Context SecurityContext sc, @QueryParam("serviceId") String serviceId) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Protocols");

        List<LibraryItem> ret = LibraryRepositoryHelper.getProtocolsByServiceId(serviceId);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

}









