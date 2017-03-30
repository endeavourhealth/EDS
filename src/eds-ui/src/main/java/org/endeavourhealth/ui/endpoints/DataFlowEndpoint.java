package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.mySQLDatabase.MapType;
import org.endeavourhealth.core.mySQLDatabase.models.*;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.coreui.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

@Path("/dataFlow")
public final class DataFlowEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisationEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response get(@Context SecurityContext sc, @QueryParam("uuid") String uuid, @QueryParam("searchData") String searchData) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Data Flow(s)",
                "Data Flow Id", uuid,
                "SearchData", searchData);


        if (uuid == null && searchData == null) {
            LOG.trace("getDataFlow - list");

            return getDataFlowList();
        } else if (uuid != null){
            LOG.trace("getDataFlow - single - " + uuid);
            return getSingleDataFlow(uuid);
        } else {
            LOG.trace("Search Data Flow - " + searchData);
            return search(searchData);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    @RequiresAdmin
    public Response post(@Context SecurityContext sc, JsonDataFlow dataFlow) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Data Flow",
                "Data Flow", dataFlow);

        if (dataFlow.getUuid() != null) {
            MastermappingEntity.deleteAllMappings(dataFlow.getUuid());
            DataflowEntity.updateDataFlow(dataFlow);
        } else {
            dataFlow.setUuid(UUID.randomUUID().toString());
            DataflowEntity.saveDataFlow(dataFlow);
        }

        MastermappingEntity.saveDataFlowMappings(dataFlow);

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
    public Response deleteOrganisation(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Data Flow",
                "Data Flow Id", uuid);

        DataflowEntity.deleteDataFlow(uuid);

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/dpas")
    public Response getLinkedDpas(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DPA(s)",
                "DPA Id", uuid);

        return getLinkedDpas(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/dsas")
    public Response getLinkedDsas(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DSA(s)",
                "DSA Id", uuid);

        return getLinkedDsas(uuid);
    }

    private Response getDataFlowList() throws Exception {

        List<DataflowEntity> dataFlows = DataflowEntity.getAllDataFlows();

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(dataFlows)
                .build();
    }

    private Response getSingleDataFlow(String uuid) throws Exception {
        DataflowEntity dataFlow = DataflowEntity.getDataFlow(uuid);

        return Response
                .ok()
                .entity(dataFlow)
                .build();

    }

    private Response search(String searchData) throws Exception {
        Iterable<DataflowEntity> dataflows = DataflowEntity.search(searchData);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(dataflows)
                .build();
    }

    private Response getLinkedDpas(String dataFlowUuid) throws Exception {

        List<String> dpaUuids = MastermappingEntity.getChildMappings(dataFlowUuid, MapType.DATAFLOW.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType());

        List<DataprocessingagreementEntity> ret = DataprocessingagreementEntity.getDPAsFromList(dpaUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getLinkedDsas(String dataFlowUuid) throws Exception {

        List<String> dsaUuids = MastermappingEntity.getParentMappings(dataFlowUuid, MapType.DATAFLOW.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType());

        List<DatasharingagreementEntity> ret = DatasharingagreementEntity.getDSAsFromList(dsaUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

}
