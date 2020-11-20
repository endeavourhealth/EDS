package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.core.database.dal.datagenerator.SubscriberZipFileUUIDsDalI;
import org.endeavourhealth.core.database.dal.datagenerator.models.RemoteFilingStatistics;
import org.endeavourhealth.core.database.dal.datagenerator.models.RemoteFilingSubscriber;
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
public class RemoteFilingEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(RemoteFilingEndpoint.class);

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.RemoteFiling);

    private static final SubscriberZipFileUUIDsDalI remoteRepository = DalProvider.factorySubscriberZipFileUUIDs();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="RemoteFilingEndpoint.getAllRemoteFilingStatus")
    @Path("/getPagedRemoteFilingStatus")
    public Response getAllRemoteFilingStatus(@Context SecurityContext sc,
                                             @ApiParam(value = "page number (defaults to 1 if not provided)") @QueryParam("pageNumber") Integer pageNumber,
                                             @ApiParam(value = "page size (defaults to 20 if not provided)")@QueryParam("pageSize") Integer pageSize) throws Exception {

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Remote Filing Statistics");

        List<RdbmsSubscriberZipFileUUIDs> fileUUIDs = remoteRepository.getPagedSubscriberZipFileUUIDsEntities(pageNumber, pageSize);

        return Response
                .ok()
                .entity(fileUUIDs)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="DataSharingManager.RemoteFilingEndpoint.getRemoteFilingCount")
    @Path("/getRemoteFilingCount")
    @ApiOperation(value = "When using server side pagination, this returns the total count of the results of the query")
    public Response getRemoteFilingCount(@Context SecurityContext sc) throws Exception {

        Long count = remoteRepository.getTotalNumberOfSubscriberFiles();

        return Response
                .ok()
                .entity(count)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="RemoteFilingEndpoint.getAllRemoteFilingStats")
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="RemoteFilingEndpoint.getAllSubscribers")
    @Path("/getSubscribers")
    public Response getSubscribers(@Context SecurityContext sc) throws Exception {

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Remote Filing Subscribers");

        List<RemoteFilingSubscriber> subscribers = remoteRepository.getSubscribers();

        return Response
                .ok()
                .entity(subscribers)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="RemoteFilingEndpoint.getRemoteFilingSubscriberStats")
    @Path("/getSubscriberStatistics")
    public Response getRemoteFilingSubscriberStats(@Context SecurityContext sc,
                                                    @QueryParam("subscriberId") Integer subscriberId,
                                                    @QueryParam("timeFrame") String timeFrame) throws Exception {

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Remote Filing Subscriber Statistics");

        List<RemoteFilingStatistics> fileUUIDs = remoteRepository.getSubscriberStatistics(subscriberId, timeFrame);

        return Response
                .ok()
                .entity(fileUUIDs)
                .build();
    }
}
