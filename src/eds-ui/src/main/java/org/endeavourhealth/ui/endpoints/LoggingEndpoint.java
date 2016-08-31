package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.admin.models.*;
import org.endeavourhealth.ui.DependencyType;
import org.endeavourhealth.ui.database.DataManager;
import org.endeavourhealth.ui.database.models.LoggingEventEntity;
import org.endeavourhealth.ui.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;

@Path("/logging")
public final class LoggingEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingEndpoint.class);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getLoggingEvents")
    public Response getLoggingEvents(@Context SecurityContext sc, @QueryParam("serviceId") String serviceId, @QueryParam("level") String level) throws Exception {

        DataManager db = new DataManager();

        List<LoggingEventEntity> events = db.getLoggingEvents(serviceId, level);

        return Response
                .ok()
                .entity(events)
                .build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getStackTrace")
    public Response getStackTrace(@Context SecurityContext sc, @QueryParam("eventId") Long eventId) throws Exception {

        DataManager db = new DataManager();

        String stackTrace = db.getStackTrace(eventId);

        return Response
            .ok()
            .entity(stackTrace)
            .build();
    }

}
