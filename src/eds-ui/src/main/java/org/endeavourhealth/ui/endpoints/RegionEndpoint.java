package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import io.astefanutti.metrics.aspectj.Metrics;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.mySQLDatabase.MapType;
import org.endeavourhealth.core.mySQLDatabase.models.*;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.coreui.json.JsonRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;

@Path("/region")
@Metrics(registry = "EdsRegistry")
public final class RegionEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisationEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.RegionEndpoint.Get")
    @Path("/")
    public Response get(@Context SecurityContext sc, @QueryParam("uuid") String uuid, @QueryParam("searchData") String searchData) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Organisation(s)",
                "Organisation Id", uuid,
                "SearchData", searchData);

        if (uuid == null && searchData == null) {
            LOG.trace("getRegion - list");
            return getRegionList();
        } else if (uuid != null){
            LOG.trace("getRegion - single - " + uuid);
            return getSingleRegion(uuid);
        } else {
            LOG.trace("Search Region - " + searchData);
            return search(searchData);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.RegionEndpoint.Post")
    @Path("/")
    @RequiresAdmin
    public Response post(@Context SecurityContext sc, JsonRegion region) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Region",
                "Region", region);

        if (region.getUuid() != null) {
            RegionEntity.updateRegion(region);
            MasterMappingEntity.deleteAllMappings(region.getUuid());
        } else {
            region.setUuid(UUID.randomUUID().toString());
            RegionEntity.saveRegion(region);
        }

        //Process Mappings
        MasterMappingEntity.saveRegionMappings(region);

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.RegionEndpoint.Delete")
    @Path("/")
    @RequiresAdmin
    public Response deleteRegion(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Region",
                "Region Id", uuid);

        RegionEntity.deleteRegion(uuid);

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.RegionEndpoint.GetOrganisations")
    @Path("/organisations")
    public Response get(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Organisation(s)",
                "Region Id", uuid);

        return getRegionOrganisations(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.RegionEndpoint.GetParentRegions")
    @Path("/parentRegions")
    public Response getParentRegions(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Parent Region(s)",
                "Region Id", uuid);

        return getParentRegions(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.RegionEndpoint.GetChildRegions")
    @Path("/childRegions")
    public Response getChildRegions(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Child Region(s)",
                "Region Id", uuid);

        return getChildRegions(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.RegionEndpoint.GetSharingAgreements")
    @Path("/sharingAgreements")
    public Response getSharingAgreements(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Sharing Agreement(s)",
                "Region Id", uuid);

        return getSharingAgreements(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.RegionEndpoint.getApiKey")
    @Path("/getApiKey")
    public Response getApiKey(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "get Api Key");

        JsonNode json = ConfigManager.getConfigurationAsJson("GoogleMapsAPI");
       // String apiKey = json.get("apiKey").asText();
        return Response
                .ok()
                .entity(json)
                .build();
    }
    private Response getRegionList() throws Exception {

        List<RegionEntity> regions = RegionEntity.getAllRegions();

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(regions)
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

        List<String> organisationUuids = MasterMappingEntity.getChildMappings(regionUUID, MapType.REGION.getMapType(), MapType.ORGANISATION.getMapType());
        List<OrganisationEntity> ret = new ArrayList<>();

        if (organisationUuids.size() > 0)
            ret = OrganisationEntity.getOrganisationsFromList(organisationUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response search(String searchData) throws Exception {
        Iterable<RegionEntity> regions = RegionEntity.search(searchData);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(regions)
                .build();
    }

    private Response getParentRegions(String regionUuid) throws Exception {

        List<String> regionUuids = MasterMappingEntity.getParentMappings(regionUuid, MapType.REGION.getMapType(), MapType.REGION.getMapType());
        List<RegionEntity> ret = new ArrayList<>();

        if (regionUuids.size() > 0)
            ret = RegionEntity.getRegionsFromList(regionUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getChildRegions(String regionUuid) throws Exception {

        List<String> regionUuids = MasterMappingEntity.getChildMappings(regionUuid, MapType.REGION.getMapType(), MapType.REGION.getMapType());
        List<RegionEntity> ret = new ArrayList<>();

        if (regionUuids.size() > 0)
            ret = RegionEntity.getRegionsFromList(regionUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getSharingAgreements(String regionUuid) throws Exception {

        List<String> sharingAgreementUuids = MasterMappingEntity.getChildMappings(regionUuid, MapType.REGION.getMapType(), MapType.DATASHARINGAGREEMENT.getMapType());
        List<DataSharingAgreementEntity> ret = new ArrayList<>();

        if (sharingAgreementUuids.size() > 0)
            ret = DataSharingAgreementEntity.getDSAsFromList(sharingAgreementUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

}
