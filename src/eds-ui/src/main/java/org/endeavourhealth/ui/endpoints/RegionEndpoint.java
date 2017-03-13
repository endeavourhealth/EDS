package org.endeavourhealth.ui.endpoints;

import com.rabbitmq.tools.json.JSONReader;
import net.sourceforge.jtds.jdbc.DateTime;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.mySQLDatabase.models.OrganisationEntity;
import org.endeavourhealth.core.mySQLDatabase.models.RegionEntity;
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
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

@Path("/region")
public final class RegionEndpoint extends AbstractEndpoint {
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
            return getRegionList();
        } else { //if (uuid != null){
            LOG.trace("getOrganisation - single - " + uuid);
            return getSingleRegion(uuid);
        } /*else {
            LOG.trace("Search Organisations - " + searchData);
            return search(searchData);
        }*/
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/organisations")
    public Response get(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Organisation(s)",
                "Organisation Id", uuid);

        return getRegionOrganisations(uuid);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    @RequiresAdmin
    public Response post(@Context SecurityContext sc, JsonRegion region) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Region",
                "Region", region);

        boolean saved = false;
        if (region.getUuid() != null) {
            saved = RegionEntity.updateRegion(region);
        } else {
            saved = RegionEntity.saveRegion(region);
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(region)
                .build();
    }

    private Response getRegionList() throws Exception {

        List<Object[]> regions = RegionEntity.getAllRegions();

        List<JsonRegion> ret = new ArrayList<>();

        for (Object[] regionEntity : regions) {
            String name = regionEntity[0].toString();
            String description = regionEntity[1].toString();
            String uuid = regionEntity[2].toString();
            String organisationCount = regionEntity[3].toString();

            JsonRegion region = new JsonRegion();
            region.setName(name);
            region.setDescription(description);
            region.setUuid(uuid);
            region.setOrganisationCount(Integer.parseInt(organisationCount));

            ret.add(region);
        }

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getRegion(String uuid) throws Exception {

        List<Object[]> regions = RegionEntity.getRegion(uuid);

        JsonRegion region = new JsonRegion();

        for (Object[] regionEntity : regions) {
            String name = regionEntity[0].toString();
            String description = regionEntity[1].toString();
            String regionUuid = regionEntity[2].toString();

            region.setName(name);
            region.setDescription(description);
            region.setUuid(regionUuid);
        }

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(region)
                .build();
    }

    private Response getSingleRegion(String uuid) throws Exception {
        RegionEntity regionEntity = RegionEntity.getSingleRegion(uuid);

        return Response
                .ok()
                .entity(regionEntity)
                .build();

    }

    private Response getRegionOrganisations(String regionUUID) throws Exception {

        List<Object[]> organisations = OrganisationEntity.getOrganisationsForRegion(regionUUID);

        List<JsonOrganisationManager> ret = new ArrayList<>();

        for (Object[] OrganisationEntity : organisations) {
            String name = OrganisationEntity[0].toString();
            String alternativeName = OrganisationEntity[1]==null?"":OrganisationEntity[1].toString();
            String odsCode = OrganisationEntity[2]==null?"":OrganisationEntity[2].toString();
            String icoCode = OrganisationEntity[3]==null?"":OrganisationEntity[3].toString();
            String igToolKitStatus = OrganisationEntity[4]==null?"":OrganisationEntity[4].toString();
            String dateOfReg = OrganisationEntity[5]==null?"":OrganisationEntity[5].toString();
            String registrationPerson = OrganisationEntity[6]==null?"":OrganisationEntity[6].toString();
            String evidence = OrganisationEntity[7]==null?"":OrganisationEntity[7].toString();
            String organisationUuid = OrganisationEntity[8]==null?"":OrganisationEntity[8].toString();

            JsonOrganisationManager org = new JsonOrganisationManager();
            org.setName(name);
            org.setAlternativeName(alternativeName);
            org.setOdsCode(odsCode);
            org.setIcoCode(icoCode);
            org.setIgToolkitStatus(igToolKitStatus);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date date = simpleDateFormat.parse(dateOfReg);
            org.setDateofRegistration(date);
            org.setRegistrationPerson(registrationPerson);
            org.setEvidenceOfRegistration(evidence);
            org.setOrganisationUUID(organisationUuid);

            ret.add(org);
        }

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

}
