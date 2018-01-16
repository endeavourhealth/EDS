package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.core.database.dal.logback.LogbackDalI;
import org.endeavourhealth.core.database.dal.logback.models.LoggingEvent;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Path("/logging")
@Metrics(registry = "EdsRegistry")
public final class LoggingEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingEndpoint.class);
    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Monitoring);
    private static final LogbackDalI logbackDal = DalProvider.factoryLogbackDal();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.LoggingEndpoint.GetLoggingEvents")
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

        List<LoggingEvent> events = logbackDal.getLoggingEvents(page, serviceId, level);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(events)
                .build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.LoggingEndpoint.GetStackTrace")
    @Path("/getStackTrace")
    public Response getStackTrace(@Context SecurityContext sc, @QueryParam("eventId") Long eventId) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Stack Trace",
            "EventId", eventId);

        String stackTrace = logbackDal.getStackTrace(eventId);

        clearLogbackMarkers();

        return Response
            .ok()
            .entity(stackTrace)
            .build();
    }

}
