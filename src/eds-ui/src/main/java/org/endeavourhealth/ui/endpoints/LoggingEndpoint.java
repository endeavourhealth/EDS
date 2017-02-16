package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.ui.database.LoggingManager;
import org.endeavourhealth.ui.database.models.LoggingEventEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;

@Path("/logging")
public final class LoggingEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingEndpoint.class);
    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Monitoring);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getLoggingEvents")
    public Response getLoggingEvents(
        @Context SecurityContext sc,
        @QueryParam("page") Integer page,
        @QueryParam("serviceId") String serviceId,
        @QueryParam("level") String level) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Logging Events",
            "Page", page.toString(),
            "Service", serviceId,
            "Level",level);

        LoggingManager db = new LoggingManager();

        List<LoggingEventEntity> events = db.getLoggingEvents(page, serviceId, level);

        clearLogbackMarkers();

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
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Stack Trace",
            "EventId", eventId);

        LoggingManager db = new LoggingManager();

        String stackTrace = db.getStackTrace(eventId);

        clearLogbackMarkers();

        return Response
            .ok()
            .entity(stackTrace)
            .build();
    }

}
