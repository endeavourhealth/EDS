package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
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
@Metrics(registry = "EdsRegistry")
public final class EntityMapEndpoint extends AbstractEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(EntityMapEndpoint.class);
    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.EntityMap);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.EntityMapEndpoint.GetEntityMap")
    @Path("/getEntityMap")
    public Response getEntityMap(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load);

        LOG.trace("getEntityMap");

        EntityMap entityMap = EntityMapHelper.loadEntityMap();

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(entityMap)
                .build();
    }
}
