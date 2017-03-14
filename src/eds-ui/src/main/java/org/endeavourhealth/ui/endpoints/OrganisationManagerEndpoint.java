package org.endeavourhealth.ui.endpoints;

import net.sourceforge.jtds.jdbc.DateTime;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.mySQLDatabase.models.OrganisationEntity;
import org.endeavourhealth.core.mySQLDatabase.models.RegionEntity;
import org.endeavourhealth.core.mySQLDatabase.models.RegionorganisationmapEntity;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.coreui.json.JsonOrganisation;
import org.endeavourhealth.coreui.json.JsonOrganisationManager;
import org.endeavourhealth.coreui.json.JsonRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.text.SimpleDateFormat;
import java.util.*;

@Path("/organisationManager")
public final class OrganisationManagerEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisationEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response get(@Context SecurityContext sc, @QueryParam("uuid") String uuid, @QueryParam("searchData") String searchData) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Organisation(s)",
                "Organisation Id", uuid,
                "SearchData", searchData);


        if (uuid == null && searchData == null) {
            LOG.trace("getOrganisation - list");

            return getOrganisationList();
        } else if (uuid != null){
            LOG.trace("getOrganisation - single - " + uuid);
            return getSingleOrganisation(uuid);
        } else {
            LOG.trace("Search Organisations - " + searchData);
            return search(searchData);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    @RequiresAdmin
    public Response post(@Context SecurityContext sc, JsonOrganisationManager organisationManager) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Organisation",
                "Organisation", organisationManager);

        if (organisationManager.getUuid() != null) {
            RegionorganisationmapEntity.deleteOrganisationMap(organisationManager.getUuid());
            OrganisationEntity.updateOrganisation(organisationManager);
            RegionorganisationmapEntity.saveOrganisationMappings(organisationManager);
        } else {
            RegionorganisationmapEntity.deleteOrganisationMap(organisationManager.getUuid());
            OrganisationEntity.saveOrganisation(organisationManager);
            RegionorganisationmapEntity.saveOrganisationMappings(organisationManager);
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
    public Response deleteOrganisation(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Organisation",
                "Organisation Id", uuid);

        OrganisationEntity.deleteOrganisation(uuid);

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/regions")
    public Response get(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Region(s)",
                "Organisation Id", uuid);

        return getRegionsForOrganisation(uuid);
    }

    private Response getOrganisationList() throws Exception {

        List<OrganisationEntity> organisations = OrganisationEntity.getAllOrganisations();

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(organisations)
                .build();
    }

    private Response getSingleOrganisation(String uuid) throws Exception {
        OrganisationEntity organisationEntity = OrganisationEntity.getOrganisation(uuid);

        return Response
                .ok()
                .entity(organisationEntity)
                .build();

    }

    private Response search(String searchData) throws Exception {
        Iterable<OrganisationEntity> organisations = OrganisationEntity.search(searchData);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(organisations)
                .build();
    }

    private Response getRegionsForOrganisation(String organisationUuid) throws Exception {

        List<Object[]> regions = RegionEntity.getRegionsForOrganisation(organisationUuid);

        List<JsonRegion> ret = new ArrayList<>();

        for (Object[] regionEntity : regions) {
            String name = regionEntity[0].toString();
            String description = regionEntity[1]==null?"":regionEntity[1].toString();
            String Uuid = regionEntity[2]==null?"":regionEntity[2].toString();

            JsonRegion reg = new JsonRegion();
            reg.setName(name);
            reg.setDescription(description);
            reg.setUuid(Uuid);

            ret.add(reg);
        }

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

}
