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
import org.endeavourhealth.coreui.json.JsonTreeNode;
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.RegionEndpoint.getTreeNodes")
    @Path("/getTreeNodes")
    public Response getTreeNodes(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "get Tree Nodes");

        return getTreeNodes();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.RegionEndpoint.getTreeNodes")
    @Path("/getChildTreeNodes")
    public Response getChildTreeNodes(@Context SecurityContext sc, @QueryParam("uuid") String uuid, @QueryParam("type") Short type) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "get Child Tree Nodes");

        return getChildTreeNodes(uuid, type);
    }

    private Response getTreeNodes() throws Exception {

        List<RegionEntity> regions = RegionEntity.getAllRegions();

        List<JsonTreeNode> tree = new ArrayList<>();

        for (RegionEntity region : regions ) {
            JsonTreeNode node = new JsonTreeNode();
            node.setName(region.getName());
            node.setItemUuid(region.getUuid());
            node.setId(UUID.randomUUID().toString());
            node.setHasChildren(true);
            node.setType(MapType.REGION.getMapType());

            tree.add(node);
        }

        return Response
                .ok()
                .entity(tree)
                .build();
    }



    private Response getChildTreeNodes(String parentUuid, Short type) throws Exception {

        List<MasterMappingEntity> all = MasterMappingEntity.getAllChildMappings(parentUuid, type);
        List<String> organisationUuids = new ArrayList<>();
        List<String> serviceUuids = new ArrayList<>();
        List<String> regionUuids = new ArrayList<>();
        List<OrganisationEntity> organisations = new ArrayList<>();
        List<OrganisationEntity> services = new ArrayList<>();
        List<RegionEntity> regions = new ArrayList<>();

        for (MasterMappingEntity mme : all) {
            switch (mme.getChildMapTypeId()) {
                case 0 : serviceUuids.add(mme.getChildUuid());
                    break;
                case 1 : organisationUuids.add(mme.getChildUuid());
                    break;
                case 2 : regionUuids.add(mme.getChildUuid());
                    break;
                default : break;
            }
        }

        if (organisationUuids.size() > 0)
            organisations = OrganisationEntity.getOrganisationsFromList(organisationUuids);
        if (serviceUuids.size() > 0)
            services = OrganisationEntity.getOrganisationsFromList(serviceUuids);
        if (regionUuids.size() > 0)
            regions = RegionEntity.getRegionsFromList(regionUuids);

        List<JsonTreeNode> tree = new ArrayList<>();

        if (organisations.size() > 0) {
            JsonTreeNode parentNode = new JsonTreeNode();
            parentNode.setName("Organisations");
            parentNode.setId(UUID.randomUUID().toString());
            parentNode.setHasChildren(true);

            List<JsonTreeNode> children = new ArrayList<>();
            for (OrganisationEntity org : organisations) {
                JsonTreeNode node = new JsonTreeNode();
                node.setName(org.getName());
                node.setItemUuid(org.getUuid());
                node.setId(UUID.randomUUID().toString());
                node.setHasChildren(true);
                node.setType(MapType.ORGANISATION.getMapType());

                children.add(node);
            }

            parentNode.setChildren(children);
            tree.add(parentNode);
        }

        if (services.size() > 0) {
            JsonTreeNode parentNode = new JsonTreeNode();
            parentNode.setName("Services");
            parentNode.setId(UUID.randomUUID().toString());
            parentNode.setHasChildren(true);

            List<JsonTreeNode> children = new ArrayList<>();
            for (OrganisationEntity ser : services) {
                JsonTreeNode node = new JsonTreeNode();
                node.setName(ser.getName());
                node.setItemUuid(ser.getUuid());
                node.setId(UUID.randomUUID().toString());
                node.setHasChildren(true);
                node.setType(MapType.SERVICE.getMapType());

                children.add(node);
            }

            parentNode.setChildren(children);
            tree.add(parentNode);
        }

        if (regions.size() > 0) {
            JsonTreeNode parentNode = new JsonTreeNode();
            parentNode.setName("Regions");
            parentNode.setId(UUID.randomUUID().toString());
            parentNode.setHasChildren(true);

            List<JsonTreeNode> children = new ArrayList<>();
            for (RegionEntity reg : regions) {
                JsonTreeNode node = new JsonTreeNode();
                node.setName(reg.getName());
                node.setItemUuid(reg.getUuid());
                node.setId(UUID.randomUUID().toString());
                node.setHasChildren(true);
                node.setType(MapType.REGION.getMapType());

                children.add(node);
            }

            parentNode.setChildren(children);
            tree.add(parentNode);
        }

        return Response
                .ok()
                .entity(tree)
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
