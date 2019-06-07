package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.astefanutti.metrics.aspectj.Metrics;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.core.database.dal.datagenerator.SubscriberZipFileUUIDsDalI;
import org.endeavourhealth.core.database.dal.datagenerator.models.RemoteFilingStatistics;
import org.endeavourhealth.core.database.rdbms.datagenerator.models.RdbmsSubscriberZipFileUUIDs;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Path("/remoteFiling")
@Metrics(registry = "EdsRegistry")
public class RemoteFilingEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(RemoteFilingEndpoint.class);

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.RemoteFiling);

    private static final SubscriberZipFileUUIDsDalI remoteRepository = DalProvider.factorySubscriberZipFileUUIDs();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.RemoteFilingEndpoint.getAllRemoteFilingStatus")
    @Path("/getAllRemoteFilingStatus")
    public Response getAllRemoteFilingStatus(@Context SecurityContext sc) throws Exception {

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Remote Filing Statistics");

        List<RdbmsSubscriberZipFileUUIDs> fileUUIDs = remoteRepository.getAllSubscriberZipFileUUIDsEntities();

        return Response
                .ok()
                .entity(fileUUIDs)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.RemoteFilingEndpoint.getAllRemoteFilingStats")
    @Path("/getStatistics")
    public Response getAllRemoteFilingStats(@Context SecurityContext sc,
                                            @QueryParam("timeframe") String timeframe) throws Exception {

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Remote Filing Statistics");

        List<RemoteFilingStatistics> fileUUIDs = remoteRepository.getStatistics(timeframe);

        return Response
                .ok()
                .entity(fileUUIDs)
                .build();
    }
}
