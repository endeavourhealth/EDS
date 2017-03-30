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

@Path("/dpa")
public final class DpaEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisationEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response get(@Context SecurityContext sc, @QueryParam("uuid") String uuid, @QueryParam("searchData") String searchData) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DPA(s)",
                "DPA Id", uuid,
                "SearchData", searchData);


        if (uuid == null && searchData == null) {
            LOG.trace("getDPA - list");
            return getDPAList();
        } else if (uuid != null){
            LOG.trace("getDPA - single - " + uuid);
            return getSingleDPA(uuid);
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
    public Response post(@Context SecurityContext sc, JsonDPA dpa) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "DPA",
                "DPA", dpa);

        if (dpa.getUuid() != null) {
            MastermappingEntity.deleteAllMappings(dpa.getUuid());
            DataprocessingagreementEntity.updateDPA(dpa);
        } else {
            dpa.setUuid(UUID.randomUUID().toString());
            DataprocessingagreementEntity.saveDPA(dpa);
        }

        MastermappingEntity.saveDataProcessingAgreementMappings(dpa);

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
                "DPA",
                "DPA Id", uuid);

        DataprocessingagreementEntity.deleteDPA(uuid);

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/dataflows")
    public Response getLinkedDataFlows(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "dataflows(s)",
                "DPA Id", uuid);

        return getLinkedDataFlows(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/cohorts")
    public Response getLinkedCohorts(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "cohorts(s)",
                "DPA Id", uuid);

        return getLinkedCohorts(uuid);
    }

    private Response getDPAList() throws Exception {

        List<DataprocessingagreementEntity> dpas = DataprocessingagreementEntity.getAllDPAs();

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(dpas)
                .build();
    }

    private Response getSingleDPA(String uuid) throws Exception {
        DataprocessingagreementEntity dpaEntity = DataprocessingagreementEntity.getDPA(uuid);

        return Response
                .ok()
                .entity(dpaEntity)
                .build();

    }

    private Response search(String searchData) throws Exception {
        Iterable<DataprocessingagreementEntity> dpas = DataprocessingagreementEntity.search(searchData);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(dpas)
                .build();
    }

    private Response getLinkedDataFlows(String dpaUuid) throws Exception {

        List<String> dataFlowUuids = MastermappingEntity.getParentMappings(dpaUuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.DATAFLOW.getMapType());

        List<DataflowEntity> ret = DataflowEntity.getDataFlowsFromList(dataFlowUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getLinkedCohorts(String dpaUuid) throws Exception {
        List<String> regionUuids = MastermappingEntity.getChildMappings(dpaUuid, MapType.DATAPROCESSINGAGREEMENT.getMapType(), MapType.COHORT.getMapType());

        List<CohortEntity> ret = CohortEntity.getCohortsFromList(regionUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

}
