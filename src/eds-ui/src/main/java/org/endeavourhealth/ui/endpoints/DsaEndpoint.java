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

@Path("/dsa")
public final class DsaEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisationEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response get(@Context SecurityContext sc, @QueryParam("uuid") String uuid, @QueryParam("searchData") String searchData) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "DSA(s)",
                "DSA Id", uuid,
                "SearchData", searchData);


        if (uuid == null && searchData == null) {
            LOG.trace("getDSA - list");
            return getDSAList();
        } else if (uuid != null){
            LOG.trace("getDSA - single - " + uuid);
            return getSingleDSA(uuid);
        } else {
            LOG.trace("Search DSA - " + searchData);
            return search(searchData);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    @RequiresAdmin
    public Response post(@Context SecurityContext sc, JsonDSA dsa) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "DSA",
                "DSA", dsa);

        if (dsa.getUuid() != null) {
            DatasharingagreementEntity.updateDSA(dsa);
        } else {
            DatasharingagreementEntity.saveDSA(dsa);
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
                "DSA",
                "DSA Id", uuid);

        DatasharingagreementEntity.deleteDSA(uuid);

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    private Response getDSAList() throws Exception {

        List<DatasharingagreementEntity> dsas = DatasharingagreementEntity.getAllDSAs();

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(dsas)
                .build();
    }

    private Response getSingleDSA(String uuid) throws Exception {
        DatasharingagreementEntity dsaEntity = DatasharingagreementEntity.getDSA(uuid);

        return Response
                .ok()
                .entity(dsaEntity)
                .build();

    }

    private Response search(String searchData) throws Exception {
        Iterable<DatasharingagreementEntity> dsas = DatasharingagreementEntity.search(searchData);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(dsas)
                .build();
    }

}
