package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
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
@Metrics(registry = "EdsRegistry")
public final class DataSharingSummaryEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisationEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.DataSharingSummaryEndpoint.Get")
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
    @Timed(absolute = true, name="EDS-UI.DataSharingSummaryEndpoint.Post")
    @Path("/")
    @RequiresAdmin
    public Response post(@Context SecurityContext sc, JsonDataSharingSummary dataSharingSummary) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Data Sharing Summary",
                "Data Sharing Summary", dataSharingSummary);

        if (dataSharingSummary.getUuid() != null) {
            DataSharingSummaryEntity.updateDataSharingSummary(dataSharingSummary);
        } else {
            dataSharingSummary.setUuid(UUID.randomUUID().toString());
            DataSharingSummaryEntity.saveDataSharingSummary(dataSharingSummary);
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.DataSharingSummaryEndpoint.Delete")
    @Path("/")
    @RequiresAdmin
    public Response delete(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Data Sharing Summary",
                "Data Sharing Summary Id", uuid);

        DataSharingSummaryEntity.deleteDataSharingSummary(uuid);

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.DataSharingSummaryEndpoint.GetServiceStatistics")
    @Path("/statistics")
    @RequiresAdmin
    public Response getServiceStatistics(@Context SecurityContext sc, @QueryParam("type") String type) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Statistics",
                "type", type);

        return generateStatistics(DataSharingSummaryEntity.getStatistics(getStatisticsProcedureFromType(type)));
    }

    private Response getDataSharingSummaryList() throws Exception {

        List<DataSharingSummaryEntity> dataSharingSummaries = DataSharingSummaryEntity.getAllDataSharingSummaries();

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(dataSharingSummaries)
                .build();
    }

    private Response getSingleDataSharingSummary(String uuid) throws Exception {
        DataSharingSummaryEntity dataSharingSummary = DataSharingSummaryEntity.getDataSharingSummary(uuid);

        return Response
                .ok()
                .entity(dataSharingSummary)
                .build();

    }

    private Response search(String searchData) throws Exception {
        Iterable<DataSharingSummaryEntity> datasharingsummaryEntities = DataSharingSummaryEntity.search(searchData);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(datasharingsummaryEntities)
                .build();
    }

    private Response generateStatistics(List<Object []> statistics) throws Exception {

        List<JsonOrganisationManagerStatistics> ret = new ArrayList<>();

        for (Object[] stat : statistics) {
            JsonOrganisationManagerStatistics jsonStat = new JsonOrganisationManagerStatistics();
            jsonStat.setLabel(stat[0].toString());
            jsonStat.setValue(stat[1].toString());

            ret.add(jsonStat);
        }

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private String getStatisticsProcedureFromType(String type) throws Exception {

        switch (type){
            case "summary" : return "getDataSharingSummaryStatistics";
            case "dpa" : return "getDataProcessingAgreementStatistics";
            case "dsa" : return "getDataSharingAgreementStatistics";
            case "dataflow" : return "getDataFlowStatistics";
            case "cohort" : return "getCohortStatistics";
            case "dataset" : return "getDatasetStatistics";
            default : throw new Exception("Invalid statistics type");
        }
    }

}
