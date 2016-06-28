package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.config.ConfigurationRepository;
import org.endeavourhealth.core.data.config.models.ConfigurationResource;
import org.endeavourhealth.ui.database.TableSaveMode;
import org.endeavourhealth.ui.database.lookups.DbSourceOrganisation;
import org.endeavourhealth.ui.database.lookups.DbSourceOrganisationSet;
import org.endeavourhealth.ui.json.JsonSourceOrganisation;
import org.endeavourhealth.ui.json.JsonSourceOrganisationSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;

@Path("/config")
public final class ConfigEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigEndpoint.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getConfig")
    public Response getConfig(@Context SecurityContext sc, @QueryParam("configurationId") String configurationId) throws Exception {
        super.setLogbackMarkers(sc);

        LOG.trace("getConfig for configurationId {}", configurationId);

				ConfigurationResource configurationResource = new ConfigurationRepository().getByKey(UUID.fromString(configurationId));

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(configurationResource)
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/saveConfig")
    public Response saveConfig(@Context SecurityContext sc, ConfigurationResource configurationResource) throws Exception {
        super.setLogbackMarkers(sc);

        LOG.trace("saveConfig {} Name {}", configurationResource.getConfigurationId(), configurationResource.getConfigurationData());

        new ConfigurationRepository().update(configurationResource);

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(configurationResource)
                .build();
    }
}
