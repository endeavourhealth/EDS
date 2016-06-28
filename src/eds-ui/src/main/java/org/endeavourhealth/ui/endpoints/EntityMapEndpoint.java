package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.ui.entitymap.EntityMapHelper;
import org.endeavourhealth.ui.entitymap.models.EntityMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/entity")
public final class EntityMapEndpoint extends AbstractEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(EntityMapEndpoint.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getEntityMap")
    public Response getEntityMap(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        LOG.trace("getEntityMap");

        EntityMap entityMap = EntityMapHelper.loadEntityMap();

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(entityMap)
                .build();
    }
}
