package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.AuditAction;
import org.endeavourhealth.core.data.audit.models.AuditModule;
import org.endeavourhealth.core.data.audit.models.UserEvent;
import org.endeavourhealth.core.security.SecurityUtils;
import org.endeavourhealth.ui.json.JsonUserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/audit")
public final class AuditEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(AuditEndpoint.class);
    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Audit);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response getAudit(
        @Context SecurityContext sc,
        @QueryParam("userId") UUID userId,
        @QueryParam("serviceId") UUID serviceId,
        @QueryParam("module") String module,
        @QueryParam("subModule") String subModule,
        @QueryParam("action") String action) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "User Id", userId,
            "Service Id", serviceId,
            "Module", module,
            "Sub Module", subModule,
            "Action", action);

        LOG.trace("getAudit");

				Iterable<UserEvent> audit = userAudit.load(userId, serviceId, module, subModule, action);

        List<JsonUserEvent> jsonAudit = new ArrayList<>();
        for (UserEvent event : audit) {
            jsonAudit.add(new JsonUserEvent(event));
        }

        clearLogbackMarkers();
        return Response
                .ok()
                .entity(jsonAudit)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/modules")
    public Response getAuditModules(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Data", "Modules");

        LOG.trace("getAuditModules");

        clearLogbackMarkers();
        return Response
            .ok()
            .entity(userAudit.getModuleList())
            .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/submodules")
    public Response getAuditSubModules(@Context SecurityContext sc, @QueryParam("module") String module) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Data", "SubModules",
            "Module", module);

        LOG.trace("getAuditSubodules");

        clearLogbackMarkers();
        return Response
            .ok()
            .entity(userAudit.getSubModuleList(module))
            .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/actions")
    public Response getAuditActions(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Data", "Actions");

        LOG.trace("getAuditActions");

        clearLogbackMarkers();
        return Response
            .ok()
            .entity(userAudit.getActionList())
            .build();
    }
}
