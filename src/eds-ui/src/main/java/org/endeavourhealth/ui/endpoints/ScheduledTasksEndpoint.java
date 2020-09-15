package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.ScheduledTaskAuditDalI;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.core.database.dal.audit.models.ScheduledTaskAudit;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Path("/scheduledTask")
public class ScheduledTasksEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduledTasksEndpoint.class);

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.RemoteFiling);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="ScheduledTaskEndpoint.summary")
    @Path("/summary")
    public Response getRecentStats(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Scheduled Task Summary");

        ScheduledTaskAuditDalI dal = DalProvider.factoryScheduledTaskAuditDal();
        List<ScheduledTaskAudit> ret = dal.getLatestAudits();

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="ScheduledTaskEndpoint.history")
    @Path("/histiry")
    public Response getRecentStats(@Context SecurityContext sc,
                                   @QueryParam("applicationName") String applicationName,
                                   @QueryParam("taskName") String taskName) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Scheduled Task History",
                "ApplicationName", applicationName,
                "TaskName", taskName);

        ScheduledTaskAuditDalI dal = DalProvider.factoryScheduledTaskAuditDal();
        List<ScheduledTaskAudit> ret = dal.getHistory(applicationName, taskName);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }
}
