package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.*;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import org.endeavourhealth.coreui.json.*;
import org.endeavourhealth.ui.metrics.EdsInstrumentedFilterContextListener;
import org.endeavourhealth.ui.utility.CsvHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Path("/organisationManager")
@Metrics(registry = "EdsRegistry")
public final class OrganisationManagerEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisationEndpoint.class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);


    private static final MetricRegistry metricRegistry = EdsInstrumentedFilterContextListener.REGISTRY;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="OrganisationManager.Get")
    @Path("/")
    public Response get(@Context SecurityContext sc, @QueryParam("uuid") String uuid, @QueryParam("searchData") String searchData, @QueryParam("searchType") String searchType) throws Exception {

        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Organisation(s)",
                "Organisation Id", uuid,
                "SearchData", searchData);

        boolean searchServices = false;
        if (searchType != null && searchType.equals("services"))
            searchServices = true;

        if (uuid == null && searchData == null) {
            LOG.trace("getOrganisation - list");

            return getOrganisationList(searchServices);
        } else if (uuid != null) {
            LOG.trace("getOrganisation - single - " + uuid);
            return getSingleOrganisation(uuid);
        } else {
            LOG.trace("Search Organisations - " + searchData + searchType);
            return search(searchData, searchServices);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="OrganisationManager.Post")
    @Path("/")
    @RequiresAdmin
    public Response post(@Context SecurityContext sc, JsonOrganisationManager organisationManager) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Organisation",
                "Organisation", organisationManager);

        if (organisationManager.getUuid() != null) {
            MasterMappingEntity.deleteAllMappings(organisationManager.getUuid());
            OrganisationEntity.updateOrganisation(organisationManager);
        } else {
            organisationManager.setUuid(UUID.nameUUIDFromBytes((organisationManager.getName() + organisationManager.getOdsCode()).getBytes()).toString());
            OrganisationEntity.saveOrganisation(organisationManager);
        }


        //Process Mappings
        MasterMappingEntity.saveOrganisationMappings(organisationManager);

        List<JsonAddress> addresses = organisationManager.getAddresses();
        if (addresses.size() > 0) {
            for (JsonAddress address : addresses) {
                if (address.getOrganisationUuid() == null)
                    address.setOrganisationUuid(organisationManager.getUuid());

                if (address.getUuid() == null) {
                    address.setUuid(UUID.randomUUID().toString());
                    AddressEntity.saveAddress(address);
                }
                else
                    AddressEntity.updateAddress(address);

                getGeolocation(address);
            }

        }

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="OrganisationManager.Delete")
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
    @Timed(absolute = true, name="OrganisationManager.GetRegions")
    @Path("/regions")
    public Response get(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Region(s)",
                "Organisation Id", uuid);

        return getRegionsForOrganisation(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="OrganisationManager.GetChildOrganisations")
    @Path("/childOrganisations")
    public Response getChildOrganisations(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Child Organisations(s)",
                "Organisation Id", uuid);

        return getChildOrganisations(uuid, MapType.ORGANISATION.getMapType());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="OrganisationManager.GetServices")
    @Path("/services")
    public Response getServices(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "services(s)",
                "Organisation Id", uuid);

        return getChildOrganisations(uuid, MapType.SERVICE.getMapType());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="OrganisationManager.GetParentOrganisations")
    @Path("/parentOrganisations")
    public Response getParentOrganisations(@Context SecurityContext sc, @QueryParam("uuid") String uuid, @QueryParam("isService") String isService) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Parent Organisations(s)",
                "Organisation Id", uuid);

        Short orgType = MapType.ORGANISATION.getMapType();
        if (isService.equals("1"))
            orgType = MapType.SERVICE.getMapType();


        return getParentOrganisations(uuid, orgType);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="OrganisationManager.GetAddresses")
    @Path("/addresses")
    public Response getAddresses(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Address(es)",
                "Organisation Id", uuid);

        return getOrganisationAddressList(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="OrganisationManager.GetMarkers")
    @Path("/markers")
    public Response getMarkers(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Marker(s)",
                "Region Id", uuid);

        return getOrganisationMarkers(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="OrganisationManager.GetEditedBulks")
    @Path("/editedBulks")
    @RequiresAdmin
    public Response getUpdatedBulkOrganisations(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Organisation",
                "Organisation", null);

        List<OrganisationEntity> organisations = OrganisationEntity.getUpdatedBulkOrganisations();

        return Response
                .ok()
                .entity(organisations)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="OrganisationManager.GetConflicts")
    @Path("/conflicts")
    @RequiresAdmin
    public Response getConflictedOrganisations(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Organisation",
                "Organisation", null);

        List<OrganisationEntity> organisations = OrganisationEntity.getConflictedOrganisations();

        return Response
                .ok()
                .entity(organisations)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="OrganisationManager.GetOrganisationStatistics")
    @Path("/organisationStatistics")
    @RequiresAdmin
    public Response getOrganisationsStatistics(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Organisation Statistics",
                "Organisation", null);

        LOG.trace("Statistics obtained");
        return generateStatistics(OrganisationEntity.getStatistics("getOrganisationStatistics"));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="OrganisationManager.GetServiceStatistics")
    @Path("/serviceStatistics")
    @RequiresAdmin
    public Response getServiceStatistics(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Service Statistics",
                "Organisation", null);

       return generateStatistics(OrganisationEntity.getStatistics("getServiceStatistics"));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="OrganisationManager.GetRegionStatistics")
    @Path("/regionStatistics")
    @RequiresAdmin
    public Response getRegionStatistics(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Service Statistics",
                "Organisation", null);

        return generateStatistics(OrganisationEntity.getStatistics("getRegionStatistics"));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="OrganisationManager.DeleteBulks")
    @Path("/deleteBulks")
    @RequiresAdmin
    public Response deleteBulks(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Organisation",
                "Organisation Id", null);

        OrganisationEntity.deleteUneditedBulkOrganisations();

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    @Timed(absolute = true, name="OrganisationManager.UploadBulkOrganisations")
    @Path("/upload")
    @RequiresAdmin
    public Response post(@Context SecurityContext sc, String csvFile) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Organisation",
                "Organisation", csvFile);

        return processCSVFile(csvFile);
    }


    private Response getOrganisationList(boolean searchServices) throws Exception {

        List<OrganisationEntity> organisations = OrganisationEntity.getAllOrganisations(searchServices);

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

    private Response search(String searchData, boolean searchServices) throws Exception {
        Iterable<OrganisationEntity> organisations = OrganisationEntity.search(searchData, searchServices);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(organisations)
                .build();
    }

    private Response getRegionsForOrganisation(String organisationUuid) throws Exception {

        List<String> regionUuids = MasterMappingEntity.getParentMappings(organisationUuid, MapType.ORGANISATION.getMapType(), MapType.REGION.getMapType());
        List<RegionEntity> ret = new ArrayList<>();

        if (regionUuids.size() > 0)
            ret = RegionEntity.getRegionsFromList(regionUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
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

    private Response getOrganisationMarkers(String regionUuid) throws Exception {

        List<Object[]> markers = AddressEntity.getOrganisationsMarkers(regionUuid);

        List<JsonMarker> ret = new ArrayList<>();

        for (Object[] marker : markers) {
            String name = marker[0].toString();
            Double lat = marker[1]==null?0.0:Double.parseDouble(marker[1].toString());
            Double lng = marker[2]==null?0.0:Double.parseDouble(marker[2].toString());

            JsonMarker jsonMarker = new JsonMarker();
            jsonMarker.setName(name);
            jsonMarker.setLat(lat);
            jsonMarker.setLng(lng);

            ret.add(jsonMarker);
        }

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getOrganisationAddressList(String uuid) throws Exception {

        List<AddressEntity> addresses = AddressEntity.getAddressesForOrganisation(uuid);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(addresses)
                .build();
    }

    private void getGeolocation(JsonAddress address) throws Exception {
        Client client = ClientBuilder.newClient();

        JsonNode json = ConfigManager.getConfigurationAsJson("GoogleMapsAPI");
        String url = json.get("url").asText();
        String apiKey = json.get("apiKey").asText();

        WebTarget resource = client.target(url + address.getPostcode().replace(" ", "+") + "&key=" + apiKey);

        Invocation.Builder request = resource.request();
        request.accept(MediaType.APPLICATION_JSON_TYPE);

        Response response = request.get();

        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            String s = response.readEntity(String.class);
            JsonParser parser = new JsonParser();
            JsonElement obj = parser.parse(s);
            JsonObject jo = obj.getAsJsonObject();
            JsonElement results = jo.getAsJsonArray("results").get(0);
            JsonObject location = results.getAsJsonObject().getAsJsonObject("geometry").getAsJsonObject("location");

            address.setLat(Double.parseDouble(location.get("lat").toString()));
            address.setLng(Double.parseDouble(location.get("lng").toString()));

            AddressEntity.updateGeolocation(address);
        }


    }

    private Response getChildOrganisations(String organisationUuid, Short organisationType) throws Exception {

        List<String> organisationUuids = MasterMappingEntity.getChildMappings(organisationUuid, MapType.ORGANISATION.getMapType(), organisationType);
        List<OrganisationEntity> ret = new ArrayList<>();

        if (organisationUuids.size() > 0)
            ret = OrganisationEntity.getOrganisationsFromList(organisationUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getParentOrganisations(String organisationUuid, Short orgType) throws Exception {

        List<String> organisationUuids = MasterMappingEntity.getParentMappings(organisationUuid, orgType, MapType.ORGANISATION.getMapType());
        List<OrganisationEntity> ret = new ArrayList<>();

        if (organisationUuids.size() > 0)
            ret = OrganisationEntity.getOrganisationsFromList(organisationUuids);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response processCSVFile(String csvData) throws Exception {
        boolean found = false;

        OrganisationEntity.deleteUneditedBulkOrganisations();

        List<OrganisationEntity> updatedBulkOrganisations = OrganisationEntity.getUpdatedBulkOrganisations();

        List<OrganisationEntity> organisationEntities = new ArrayList<>();
        List<AddressEntity> addressEntities = new ArrayList<>();

        Scanner scanner = new Scanner(csvData);

        while (scanner.hasNext()) {
            List<String> org = CsvHelper.parseLine(scanner.nextLine());
            OrganisationEntity importedOrg = createOrganisationEntity(org);

            for (OrganisationEntity oe : updatedBulkOrganisations){
                if (oe.getUuid().equals(importedOrg.getUuid())) {
                    found = true;
                }
            }

            if (found) {
                //already have this org and it has been updated so set the conflicted UUID to the UUID of the original org and generate a new UUID
                importedOrg.setBulkConflictedWith(importedOrg.getUuid());
                importedOrg.setUuid(UUID.nameUUIDFromBytes((importedOrg.getName() + importedOrg.getOdsCode() + "conflict").getBytes()).toString());
            }

            organisationEntities.add(importedOrg);
            addressEntities.add(createAddressEntity(org, importedOrg.getUuid()));

            found = false;
        }

        OrganisationEntity.bulkSaveOrganisation(organisationEntities);
        AddressEntity.bulkSaveAddresses(addressEntities);

        return Response
                .ok()
                .build();
    }

    private OrganisationEntity createOrganisationEntity(List<String> org) throws Exception {

        OrganisationEntity organisationEntity = new OrganisationEntity();
        organisationEntity.setName(org.get(1));
        organisationEntity.setOdsCode(org.get(0));
        organisationEntity.setUuid(UUID.nameUUIDFromBytes((organisationEntity.getName() + organisationEntity.getOdsCode()).getBytes()).toString());

        organisationEntity.setIsService((byte)0);
        organisationEntity.setBulkImported((byte)1);
        organisationEntity.setBulkItemUpdated((byte)0);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date date = format.parse(org.get(10));

        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        organisationEntity.setDateOfRegistration(java.sql.Date.valueOf(localDate));

        return organisationEntity;
    }

    private AddressEntity createAddressEntity(List<String> org, String organisationUuid) throws Exception {

        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setUuid(UUID.randomUUID().toString());
        addressEntity.setOrganisationUuid(organisationUuid);

        addressEntity.setBuildingName(org.get(4));
        addressEntity.setNumberAndStreet(org.get(5));
        addressEntity.setLocality(org.get(6));
        addressEntity.setCity(org.get(7));
        addressEntity.setCounty(org.get(8));
        addressEntity.setPostcode(org.get(9));

        return addressEntity;
    }

}
