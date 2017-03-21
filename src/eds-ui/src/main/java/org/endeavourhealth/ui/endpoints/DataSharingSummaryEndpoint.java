package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.mySQLDatabase.models.*;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.coreui.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

@Path("/dataSharingSummary")
public final class DataSharingSummaryEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisationEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response get(@Context SecurityContext sc, @QueryParam("uuid") String uuid, @QueryParam("searchData") String searchData) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Data Sharing Summary(s)",
                "Data Sharing Summary Id", uuid,
                "SearchData", searchData);


        if (uuid == null && searchData == null) {
            LOG.trace("getData Sharing Summary - list");
            return getDataSharingSummaryList();
        } else if (uuid != null){
            LOG.trace("getData Sharing Summary - single - " + uuid);
            Response response = getSingleDataSharingSummary(uuid);
            return response;
            //return getSingleDataSharingSummary(uuid);
        } else {
            LOG.trace("Search DPA - " + searchData);
            return search(searchData);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    @RequiresAdmin
    public Response post(@Context SecurityContext sc, JsonDataSharingSummary dataSharingSummary) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Data Sharing Summary",
                "Data Sharing Summary", dataSharingSummary);

        if (dataSharingSummary.getUuid() != null) {
            dataSharingSummary.setUuid(UUID.randomUUID().toString());
            DatasharingsummaryEntity.updateDataSharingSummary(dataSharingSummary);
        } else {
            DatasharingsummaryEntity.saveDataSharingSummary(dataSharingSummary);
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    @RequiresAdmin
    public Response delete(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Data Sharing Summary",
                "Data Sharing Summary Id", uuid);

        DatasharingsummaryEntity.deleteDataSharingSummary(uuid);

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    private Response getDataSharingSummaryList() throws Exception {

        List<DatasharingsummaryEntity> dataSharingSummaries = DatasharingsummaryEntity.getAllDataSharingSummaries();

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(dataSharingSummaries)
                .build();
    }

    private Response getSingleDataSharingSummary(String uuid) throws Exception {
        DatasharingsummaryEntity dataSharingSummary = DatasharingsummaryEntity.getDataSharingSummary(uuid);

        return Response
                .ok()
                .entity(dataSharingSummary)
                .build();

    }

    private Response search(String searchData) throws Exception {
        Iterable<DatasharingsummaryEntity> datasharingsummaryEntities = DatasharingsummaryEntity.search(searchData);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(datasharingsummaryEntities)
                .build();
    }

}
