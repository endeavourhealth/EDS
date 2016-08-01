package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.security.annotations.RequiresAdmin;
import org.endeavourhealth.ui.json.JsonSourceOrganisation;
import org.endeavourhealth.ui.json.JsonSourceOrganisationSet;
import org.endeavourhealth.ui.database.TableSaveMode;
import org.endeavourhealth.ui.database.lookups.DbSourceOrganisation;
import org.endeavourhealth.ui.database.lookups.DbSourceOrganisationSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;

@Path("/lookup")
public final class LookupEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(LookupEndpoint.class);

    private static final String ODS_CODE_DELIMITER = "|";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getOrganisationSets")
    public Response getOrganisationSets(@Context SecurityContext sc, @QueryParam("searchTerm") String searchTerm) throws Exception {
        super.setLogbackMarkers(sc);

        LOG.trace("getOrganisationSets for search term {}", searchTerm);

        UUID orgUuid = getOrganisationUuidFromToken(sc);

        List<DbSourceOrganisationSet> sets = null;
        if (searchTerm == null) {
            sets = DbSourceOrganisationSet.retrieveAllSets(orgUuid);
        } else {
            sets = DbSourceOrganisationSet.retrieveSets(orgUuid, searchTerm);
        }

        List<JsonSourceOrganisationSet> ret = new ArrayList<>();

        for (DbSourceOrganisationSet set: sets) {
            ret.add(new JsonSourceOrganisationSet(set));
        }

        //sort by org name
        Collections.sort(ret);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getOrganisationSetMembers")
    public Response getOrganisationSetMembers(@Context SecurityContext sc, @QueryParam("uuid") String setUuidStr) throws Exception {
        super.setLogbackMarkers(sc);

        LOG.trace("getOrganisationSetMembers for UUID {}", setUuidStr);

        UUID setUuid = UUID.fromString(setUuidStr);
        DbSourceOrganisationSet set = DbSourceOrganisationSet.retrieveSetForUuid(setUuid);

        UUID orgUuid = getOrganisationUuidFromToken(sc);
        if (!set.getOrganisationUuid().equals(orgUuid)) {
            throw new BadRequestException("Trying to get organisation set members for a different organisation");
        }

        String odsCodeStr = set.getOdsCodes();
        List<String> odsCodeList = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(odsCodeStr, ODS_CODE_DELIMITER, false);
        while (st.hasMoreTokens()) {
            String odsCode = st.nextToken();
            odsCodeList.add(odsCode);
        }

        List<JsonSourceOrganisation> ret = new ArrayList<>();

        List<DbSourceOrganisation> orgs = DbSourceOrganisation.retrieveForOdsCodes(odsCodeList);
        for (DbSourceOrganisation org: orgs) {
            ret.add(new JsonSourceOrganisation(org));
        }

        //sort by org name
        Collections.sort(ret);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/saveOrganisationSet")
    @RequiresAdmin
    public Response saveOrganisationSet(@Context SecurityContext sc, JsonSourceOrganisationSet orgSet) throws Exception {
        super.setLogbackMarkers(sc);

        UUID orgUuid = getOrganisationUuidFromToken(sc);
        UUID uuid = orgSet.getUuid();
        String name = orgSet.getName();
        List<JsonSourceOrganisation> orgs = orgSet.getOrganisations();

        LOG.trace("saveOrganisationSet UUID {} Name {}", uuid, name);

        DbSourceOrganisationSet set = null;

        if (uuid == null) {
            //creating a new set
            set = new DbSourceOrganisationSet();
            set.setOrganisationUuid(orgUuid);
        } else {
            //updating an existing
            set = DbSourceOrganisationSet.retrieveSetForUuid(uuid);
            if (!set.getOrganisationUuid().equals(orgUuid)) {
                throw new BadRequestException("Trying to amend an organisation set for another organisation");
            }
        }

        if (name != null) {
            set.setName(name);
        }
        if (orgs != null) {
            StringJoiner joiner = new StringJoiner(ODS_CODE_DELIMITER);
            for (JsonSourceOrganisation org: orgs) {
                joiner.add(org.getOdsCode());
            }
            String odsCodeStr = joiner.toString();
            set.setOdsCodes(odsCodeStr);
        }

        set.writeToDb();

        //return the UUID to the client, so it known what was assigned
        uuid = set.getSourceOrganisationSetUuid();
        JsonSourceOrganisationSet ret = new JsonSourceOrganisationSet();
        ret.setUuid(uuid);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/deleteOrganisationSet")
    @RequiresAdmin
    public Response deleteOrganisationSet(@Context SecurityContext sc, JsonSourceOrganisationSet orgSet) throws Exception {
        super.setLogbackMarkers(sc);

        UUID orgUuid = getOrganisationUuidFromToken(sc);
        UUID uuid = orgSet.getUuid();

        LOG.trace("deleteOrganisationSet UUID {}", uuid);

        DbSourceOrganisationSet set = DbSourceOrganisationSet.retrieveSetForUuid(uuid);
        if (!set.getOrganisationUuid().equals(orgUuid)) {
            throw new BadRequestException("Organisation set " + uuid + " belongs to a different organisation");
        }

        set.setSaveMode(TableSaveMode.DELETE);
        set.writeToDb();

        clearLogbackMarkers();
        return Response
                .ok()
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/searchOrganisations")
    public Response searchOrganisations(@Context SecurityContext sc, @QueryParam("searchTerm") String searchTerm) throws Exception {
        super.setLogbackMarkers(sc);

        LOG.trace("searchOrganisations for searchTerm {}", searchTerm);

        UUID orgUuid = getOrganisationUuidFromToken(sc);
        List<DbSourceOrganisation> orgs = null;
        if (searchTerm == null) {
            orgs = DbSourceOrganisation.retrieveAll(false);
        } else {
            orgs = DbSourceOrganisation.retrieveForSearch(searchTerm);
        }

        List<JsonSourceOrganisation> ret = new ArrayList<>();

        for (DbSourceOrganisation org: orgs) {
            ret.add(new JsonSourceOrganisation(org));
        }

        //sort by org name
        Collections.sort(ret);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getOrganisations")
    public Response getOrganisations(@Context SecurityContext sc, @QueryParam("odsCodes") String odsCodeStr) throws Exception {
        super.setLogbackMarkers(sc);

        if (!getEndUserFromSession(sc).getIsSuperUser()) {
            throw new BadRequestException();
        }

        LOG.trace("getOrganisations for odsCodes {}", odsCodeStr);

        UUID orgUuid = getOrganisationUuidFromToken(sc);

        List<String> odsCodeList = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(odsCodeStr, ODS_CODE_DELIMITER, false);
        while (st.hasMoreTokens()) {
            String odsCode = st.nextToken();
            odsCodeList.add(odsCode);
        }

        List<DbSourceOrganisation> orgs = DbSourceOrganisation.retrieveForOdsCodes(odsCodeList);

        List<JsonSourceOrganisation> ret = new ArrayList<>();

        for (DbSourceOrganisation org: orgs) {
            ret.add(new JsonSourceOrganisation(org));
        }

        //sort by org name
        Collections.sort(ret);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(ret)
                .build();
    }
}
