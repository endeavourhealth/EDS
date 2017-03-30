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

@Path("/cohort")
public final class CohortEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisationEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response get(@Context SecurityContext sc, @QueryParam("uuid") String uuid, @QueryParam("searchData") String searchData) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Cohort(s)",
                "Cohort Id", uuid,
                "SearchData", searchData);


        if (uuid == null && searchData == null) {
            LOG.trace("getCohort - list");

            return getCohortList();
        } else if (uuid != null){
            LOG.trace("getCohort - single - " + uuid);
            return getSingleCohort(uuid);
        } else {
            LOG.trace("Search Cohort - " + searchData);
            return search(searchData);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    @RequiresAdmin
    public Response post(@Context SecurityContext sc, JsonCohort cohort) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Cohort",
                "Cohort", cohort);

        if (cohort.getUuid() != null) {
            MastermappingEntity.deleteAllMappings(cohort.getUuid());
            CohortEntity.updateCohort(cohort);
        } else {
            cohort.setUuid(UUID.randomUUID().toString());
            CohortEntity.saveCohort(cohort);
        }

        MastermappingEntity.saveCohortMappings(cohort);

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
                "Cohort",
                "Cohort Id", uuid);

        CohortEntity.deleteCohort(uuid);

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/dpas")
    public Response get(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DPA(s)",
                "Cohort Id", uuid);

        return getLinkedDpas(uuid);
    }

    private Response getCohortList() throws Exception {

        List<CohortEntity> cohorts = CohortEntity.getAllCohorts();

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(cohorts)
                .build();
    }

    private Response getSingleCohort(String uuid) throws Exception {
        CohortEntity cohortEntity = CohortEntity.getCohort(uuid);

        return Response
                .ok()
                .entity(cohortEntity)
                .build();

    }

    private Response search(String searchData) throws Exception {
        Iterable<CohortEntity> cohorts = CohortEntity.search(searchData);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(cohorts)
                .build();
    }

    private Response getLinkedDpas(String cohortUuid) throws Exception {

        List<String> dpaUuids = MastermappingEntity.getParentMappings(cohortUuid, MapType.COHORT.getMapType(), MapType.DATAPROCESSINGAGREEMENT.getMapType());

        List<DataprocessingagreementEntity> ret = new ArrayList<>();

        if (dpaUuids.size() > 0)
            ret = DataprocessingagreementEntity.getDPAsFromList(dpaUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

}
