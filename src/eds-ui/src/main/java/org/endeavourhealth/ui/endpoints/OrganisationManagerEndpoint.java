package org.endeavourhealth.ui.endpoints;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;
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
        } else if (uuid != null){
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
    @Path("/")
    @RequiresAdmin
    public Response post(@Context SecurityContext sc, JsonOrganisationManager organisationManager) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Organisation",
                "Organisation", organisationManager);

        if (organisationManager.getUuid() != null) {
            MastermappingEntity.deleteAllMappings(organisationManager.getUuid());
            OrganisationEntity.updateOrganisation(organisationManager);
        } else {
            organisationManager.setUuid(UUID.randomUUID().toString());
            OrganisationEntity.saveOrganisation(organisationManager);
        }


        //Process Mappings
        MastermappingEntity.saveOrganisationMappings(organisationManager);

        List<JsonAddress> addresses = organisationManager.getAddresses();
        if (addresses.size() > 0) {
            for (JsonAddress address : addresses) {
                if (address.getUuid() == null)
                    AddressEntity.saveOrganisation(address);
                else
                    AddressEntity.updateOrganisation(address);

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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/childOrganisations")
    public Response getChildOrganisations(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Child Organisations(s)",
                "Organisation Id", uuid);

        return getChildOrganisations(uuid, (short)0);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/services")
    public Response getServices(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "services(s)",
                "Organisation Id", uuid);

        return getChildOrganisations(uuid, (short)1);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/parentOrganisations")
    public Response getParentOrganisations(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Parent Organisations(s)",
                "Organisation Id", uuid);

        return getParentOrganisations(uuid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
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
    @Path("/markers")
    public Response getMarkers(@Context SecurityContext sc, @QueryParam("uuid") String uuid) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Marker(s)",
                "Region Id", uuid);

        return getOrganisationMarkers(uuid);
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

        List<Object[]> regions = RegionEntity.getParentRegionsFromMappings(organisationUuid);

        List<JsonRegion> ret = EndpointHelper.JsonRegion(regions);

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
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=";
        String apiKey = "AIzaSyBuLpebNb3ZNXFYpya0s5_ZXBXhMNjENik";
        Client client = ClientBuilder.newClient();

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

        Short type = 0;
        List<Object[]> organisations = OrganisationEntity.getChildOrganisationsFromMappings(organisationUuid, type, organisationType);

        List<JsonOrganisationManager> ret = EndpointHelper.JsonOrganisation(organisations);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Response getParentOrganisations(String organisationUuid) throws Exception {

        Short type = 0;
        List<Object[]> organisations = OrganisationEntity.getParentOrganisationsFromMappings(organisationUuid, type);

        List<JsonOrganisationManager> ret = EndpointHelper.JsonOrganisation(organisations);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }



}
