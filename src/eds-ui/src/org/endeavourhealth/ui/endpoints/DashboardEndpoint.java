package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.admin.models.ActiveItem;
import org.endeavourhealth.core.data.admin.models.Audit;
import org.endeavourhealth.core.data.admin.models.Item;
import org.endeavourhealth.ui.json.JsonFolderContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Path("/dashboard")
public final class DashboardEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(DashboardEndpoint.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getRecentDocuments")
    public Response getRecentDocuments(@Context SecurityContext sc, @QueryParam("count") int count) throws Exception {
        super.setLogbackMarkers(sc);

        UUID userUuid = getEndUserUuidFromToken(sc);
        UUID orgUuid = getOrganisationUuidFromToken(sc);

        LOG.trace("getRecentDocuments {}", count);

        List<JsonFolderContent> ret = new ArrayList<>();

        LibraryRepository repository = new LibraryRepository();

        Iterable<Audit> audit = repository.getAuditByOrgAndDateDesc(orgUuid);
        for (Audit auditItem: audit) {
            Iterable<ActiveItem> activeItems = repository.getActiveItemByAuditId(auditItem.getId());
            for (ActiveItem activeItem: activeItems) {
                if (activeItem.getIsDeleted()==false) {
                    Item item = repository.getItemByKey(activeItem.getItemId(), activeItem.getAuditId());

                    JsonFolderContent content = new JsonFolderContent(activeItem, item, auditItem);
                    ret.add(content);
                }
            }
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }




}
