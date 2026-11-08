package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.utility.ExpiringCache;
import org.endeavourhealth.common.utility.ExpiringObject;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.core.database.dal.datagenerator.SubscriberZipFileUUIDsDalI;
import org.endeavourhealth.core.database.dal.datagenerator.models.RemoteFilingRecord;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.transform.subscriber.SubscriberConfig;
import org.endeavourhealth.ui.json.JsonRemoteFiler;
import org.endeavourhealth.ui.json.JsonRemoteFilerError;
import org.endeavourhealth.ui.json.JsonRemoteFilerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

@Path("/remoteFiling")
public class RemoteFilerEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(RemoteFilerEndpoint.class);

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.RemoteFiling);

    private static final ExpiringObject<Map<Integer, JsonRemoteFiler.JsonTechnicalDetails>> remoteFilerTechnicalDetailCache = new ExpiringObject<>(1000 * 60 * 5); //cache for five mins
    private static final ExpiringCache<UUID, Service> hmServiceCache = new ExpiringCache<>(1000 * 60 * 5); //cache for five mins

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="RemoteFilingEndpoint.GetRemoteFilers")
    @Path("/getRemoteFilers")
    public Response getRemoteFilers(@Context SecurityContext sc) throws Exception {

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Remote Filers");

        List<JsonRemoteFiler> ret = getRemoteFilers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private static Map<Integer, JsonRemoteFiler.JsonTechnicalDetails> getRemoteFilerTechnicalConfiguration() throws Exception {

        Map<Integer, JsonRemoteFiler.JsonTechnicalDetails> ret = remoteFilerTechnicalDetailCache.get();
        if (ret == null) {

            ret = new HashMap<>();

            //get the config from the data_generator database itself
            Connection connection = ConnectionManager.getDataGeneratorConnection();
            PreparedStatement ps = null;
            try {
                String sql = "SELECT subscriber_id, definition"
                        + " FROM subscriber_file_sender";
                ps = connection.prepareStatement(sql);

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String json = rs.getString(2);

                    JsonRemoteFiler.JsonTechnicalDetails config = ObjectMapperPool.getInstance().readValue(json, JsonRemoteFiler.JsonTechnicalDetails.class);
                    ret.put(new Integer(id), config);
                }

            } finally {
                if (ps != null) {
                    ps.close();
                }
                connection.close();
            }

            remoteFilerTechnicalDetailCache.set(ret);
        }

        return ret;
    }

    private static List<JsonRemoteFiler> getRemoteFilers() throws Exception {

        Map<Integer, JsonRemoteFiler.JsonTechnicalDetails> hmTechnicalConfig = getRemoteFilerTechnicalConfiguration();

        List<JsonRemoteFiler> ret = new ArrayList<>();

        Map<String, String> subscriberMap = ConfigManager.getConfigurations("db_subscriber");
        for (String subscriberName: subscriberMap.keySet()) {

            String configJson = subscriberMap.get(subscriberName);
            JsonNode json = ObjectMapperPool.getInstance().readTree(configJson);
            SubscriberConfig config = SubscriberConfig.readFromJson(subscriberName, json);

            Integer remoteId = config.getRemoteSubscriberId();
            if (remoteId == null) {
                continue;
            }

            JsonRemoteFiler.JsonTechnicalDetails technicalConfig = hmTechnicalConfig.get(remoteId);

            JsonRemoteFiler f = new JsonRemoteFiler();
            f.setName(subscriberName);
            f.setDescription(config.getDescription());
            f.setSubscriberId(remoteId.intValue());
            f.setTechnicalDetails(technicalConfig);
            ret.add(f);
        }

        return ret;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="RemoteFilingEndpoint.GetRemoteFilerStatus")
    @Path("/getRemoteFilerStatus")
    public Response getRemoteFilerStatus(@Context SecurityContext sc, @QueryParam("subscriberId") int subscriberId) throws Exception {

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Remote Filer Status", "Subscriber ID", subscriberId);

        JsonRemoteFilerStatus ret = getRemoteFilerStatus(subscriberId);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private static JsonRemoteFilerStatus getRemoteFilerStatus(int subscriberId) throws Exception {

        JsonRemoteFilerStatus ret = new JsonRemoteFilerStatus();
        ret.setSubscriberId(subscriberId);

        Connection connection = ConnectionManager.getDataGeneratorConnection();
        PreparedStatement ps = null;
        try {

            //get count staged and date range
            String sql = "SELECT count(1), min(dt_inserted), max(dt_inserted)"
                    + " FROM subscriber_zip_file_uuids"
                    + " WHERE subscriber_id = ?"
                    + " AND file_sent is null";
            ps = connection.prepareStatement(sql);

            int col = 1;
            ps.setInt(col++, subscriberId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                col = 1;
                int count = rs.getInt(col++);
                Date dEarliest = ConnectionManager.timestampToDate(rs.getTimestamp(col++));
                Date dLatest = ConnectionManager.timestampToDate(rs.getTimestamp(col++));

                ret.setCountQueuedMessages(count);
                ret.setOldestQueuedMessage(dEarliest);
                ret.setNewestQueuedMessage(dLatest);
            }
            ps.close();

            //count of sent messages sent but not ACKed
            sql = "SELECT count(1), min(file_sent), max(file_sent)"
                    + " FROM subscriber_zip_file_uuids"
                    + " WHERE subscriber_id = ?"
                    + " AND file_filing_success is null" //no feed back yet
                    + " AND file_sent is not null";
            ps = connection.prepareStatement(sql);

            col = 1;
            ps.setInt(col++, subscriberId);

            rs = ps.executeQuery();
            if (rs.next()) {
                col = 1;
                int count = rs.getInt(col++);
                Date dEarliest = ConnectionManager.timestampToDate(rs.getTimestamp(col++));
                Date dLatest = ConnectionManager.timestampToDate(rs.getTimestamp(col++));

                ret.setCountMessagesWrittenToSftp(count);
                ret.setOldestMessageWrittenToSftp(dEarliest);
                ret.setNewestMessageWrittenToSftp(dLatest);
            }
            ps.close();

            //count of messages in error
            sql = "SELECT count(1), min(file_filing_attempted), max(file_filing_attempted)"
                    + " FROM subscriber_zip_file_uuids"
                    + " WHERE subscriber_id = ?"
                    + " AND file_filing_success = 0";
            ps = connection.prepareStatement(sql);

            col = 1;
            ps.setInt(col++, subscriberId);

            rs = ps.executeQuery();
            if (rs.next()) {
                col = 1;
                int count = rs.getInt(col++);
                Date dEarliest = ConnectionManager.timestampToDate(rs.getTimestamp(col++));
                Date dLatest = ConnectionManager.timestampToDate(rs.getTimestamp(col++));

                ret.setCountMessagesInError(count);
                ret.setOldestMessageInError(dEarliest);
                ret.setNewestMessageInError(dLatest);
            }
            ps.close();

        } finally {
            connection.close();
        }


        SubscriberZipFileUUIDsDalI dal = DalProvider.factorySubscriberZipFileUUIDs();
        List<RemoteFilingRecord> failures = dal.getFailures(subscriberId, 101); //only get a limited number
        for (RemoteFilingRecord failure: failures) {

            UUID serviceId = failure.getServiceId();
            Service service = hmServiceCache.get(serviceId);
            if (service == null) {
                ServiceDalI serviceDal = DalProvider.factoryServiceDal();
                service = serviceDal.getById(serviceId);
                hmServiceCache.put(serviceId, service);
            }

            JsonRemoteFilerError error = new JsonRemoteFilerError();
            error.setServiceName(service.getName());
            error.setServiceOdsCode(service.getLocalId());
            error.setServiceId(failure.getServiceId());
            error.setSystemId(failure.getSystemId());
            error.setExchangeId(failure.getExchangeId());
            error.setBatchId(failure.getBatchId());
            error.setQueuedMessageId(failure.getQueuedMessageId());
            error.setDtFileSent(failure.getDtFileSent());
            error.setDtFileFilingAttempted(failure.getDtFileFilingAttempted());
            error.setFilingFailureMessage(failure.getFilingFailureMessage());
            error.setDtInserted(failure.getDtInserted());

            ret.getErrors().add(error);
        }

        return ret;
    }


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    //@Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="RemoteFilingEndpoint.GetQueuedMessage")
    @Path("/getQueuedMessage")
    public Response getRemoteFilerStatus(@Context SecurityContext sc, @QueryParam("queuedMessageId") String queuedMessageId) throws Exception {

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Remote Filer Queued Message", "QueuedMessage ID", queuedMessageId);

        UUID uuid = UUID.fromString(queuedMessageId);
        String ret = getQueuedMessageData(uuid);

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private String getQueuedMessageData(UUID uuid) throws Exception {
        Connection connection = ConnectionManager.getDataGeneratorConnection();
        PreparedStatement ps = null;
        try {
            String sql = "SELECT queued_message_body"
                    + " FROM subscriber_zip_file_uuids"
                    + " WHERE queued_message_uuid = ?";
            ps = connection.prepareStatement(sql);

            ps.setString(1, uuid.toString());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            } else {
                return null;
            }

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }


    /*private static final SubscriberZipFileUUIDsDalI remoteRepository = DalProvider.factorySubscriberZipFileUUIDs();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="RemoteFilingEndpoint.getAllRemoteFilingHistoryPaged")
    @Path("/getAllRemoteFilingHistoryPaged")
    public Response getAllRemoteFilingStatus(@Context SecurityContext sc,
                                             @ApiParam(value = "page number (defaults to 1 if not provided)") @QueryParam("pageNumber") Integer pageNumber,
                                             @ApiParam(value = "page size (defaults to 20 if not provided)")@QueryParam("pageSize") Integer pageSize) throws Exception {

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Remote Filing History");

        List<RdbmsSubscriberZipFileUUIDs> fileUUIDs = remoteRepository.getPagedSubscriberZipFileUUIDsEntities(pageNumber, pageSize);

        return Response
                .ok()
                .entity(fileUUIDs)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="RemoteFilingEndpoint.getSubscriberRemoteFilingHistoryPaged")
    @Path("/getSubscriberRemoteFilingHistoryPaged")
    public Response getSubscriberRemoteFilingStatus(@Context SecurityContext sc,
                                                    @QueryParam("subscriberId") Integer subscriberId,
                                             @ApiParam(value = "page number (defaults to 1 if not provided)") @QueryParam("pageNumber") Integer pageNumber,
                                             @ApiParam(value = "page size (defaults to 20 if not provided)")@QueryParam("pageSize") Integer pageSize) throws Exception {

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Remote Filing Statistics");

        List<RdbmsSubscriberZipFileUUIDs> fileUUIDs = remoteRepository.getPagedSubscriberZipFileUUIDsEntities(subscriberId, pageNumber, pageSize);

        return Response
                .ok()
                .entity(fileUUIDs)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="RemoteFilingEndpoint.getFailedFilingUUIDEntities")
    @Path("/getFailedFilingUUIDEntities")
    public Response getAllRemoteFilingStatus(@Context SecurityContext sc,
                                             @QueryParam("subscriberId") Integer subscriberId,
                                             @QueryParam("timeFrame") String timeFrame) throws Exception {

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Remote Filing Statistics");

        List<RdbmsSubscriberZipFileUUIDs> fileUUIDs = remoteRepository.getFailedFilingUUIDEntities(timeFrame, subscriberId);

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
    @Timed(absolute = true, name="DataSharingManager.RemoteFilingEndpoint.getRemoteSubscriberFilingCount")
    @Path("/getRemoteSubscriberFilingCount")
    @ApiOperation(value = "When using server side pagination, this returns the total count of the results of the query")
    public Response getRemoteSubscriberFilingCount(@Context SecurityContext sc,
                                                   @QueryParam("subscriberId") Integer subscriberId) throws Exception {

        Long count = remoteRepository.getTotalNumberOfSubscriberFiles(subscriberId);

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
                                            @QueryParam("timeFrame") String timeFrame) throws Exception {

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Remote Filing Statistics");

        List<RemoteFilingStatistics> fileUUIDs = remoteRepository.getStatistics(timeFrame);

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

        List<RemoteFilingStatistics> subscriberStatistics = remoteRepository.getSubscriberStatistics(subscriberId, timeFrame);

        return Response
                .ok()
                .entity(subscriberStatistics)
                .build();
    }*/
}