package org.endeavourhealth.ui.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.ui.json.JsonSftpReaderIgnoreBatchSplitParameters;
import org.hibernate.internal.SessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Path("/sftpReader")
public class SftpReaderEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(SftpReaderEndpoint.class);

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Library);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/instances")
    public Response getInstances(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get SFTP Reader Instance Names");

        String ret = getSftpReaderInstances();

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/status")
    public Response getChannelStatus(@Context SecurityContext sc, @QueryParam("instance") String instanceName) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get SFTP Reader Instance Status");

        String ret = getSftpReaderStatus(instanceName);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/history")
    public Response getChannelHistory(@Context SecurityContext sc,
                                      @QueryParam("from") Long fromDateMillis,
                                      @QueryParam("to") Long toDateMillis,
                                      @QueryParam("configurationId") String configurationId) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get SFTP Reader Instance History");

        Date dFrom = new Date(fromDateMillis);
        Date dTo = new Date(toDateMillis);

        String ret = getSftpReaderHistory(configurationId, dFrom, dTo);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }



    private String getSftpReaderHistory(String configurationId, Date dFrom, Date dTo) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode root = new ArrayNode(mapper.getNodeFactory());

        EntityManager entityManager = ConnectionManager.getSftpReaderEntityManager();
        PreparedStatement ps = null;
        try {
            SessionImpl session = (SessionImpl) entityManager.getDelegate();
            Connection connection = session.connection();

            //get the individual orgs within each batch and stick in a map
            String sql = null;
            if (ConnectionManager.isPostgreSQL(connection)) {
                sql = "SELECT b.batch_id, s.organisation_id, s.have_notified"
                        + " FROM log.batch b"
                        + " INNER JOIN log.batch_split s"
                        + " ON s.batch_id = b.batch_id"
                        + " WHERE b.configuration_id = ?"
                        + " AND b.insert_date >= ?"
                        + " AND b.insert_date <= ?";

            } else {
                sql = "SELECT b.batch_id, s.organisation_id, s.have_notified"
                        + " FROM batch b"
                        + " INNER JOIN batch_split s"
                        + " ON s.batch_id = b.batch_id"
                        + " WHERE b.configuration_id = ?"
                        + " AND b.insert_date >= ?"
                        + " AND b.insert_date <= ?";
            }

            ps = connection.prepareStatement(sql);

            int col = 1;
            ps.setString(col++, configurationId);
            ps.setTimestamp(col++, new Timestamp(dFrom.getTime()));
            ps.setTimestamp(col++, new Timestamp(dTo.getTime()));

            Map<Integer, Map<String, Boolean>> hmOrgsByBatch = new HashMap<>(); //bit nasty having a map of maps, but quick

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                col = 1;
                int batchId = rs.getInt(col++);
                String orgId = rs.getString(col++);
                boolean notified = rs.getBoolean(col++);

                Map<String, Boolean> innerMap = hmOrgsByBatch.get(new Integer(batchId));
                if (innerMap == null) {
                    innerMap = new HashMap<>();
                    hmOrgsByBatch.put(new Integer(batchId), innerMap);
                }
                innerMap.put(orgId, new Boolean(notified));
            }
            ps.close();

            //get the details of the batches and files
            sql = null;
            if (ConnectionManager.isPostgreSQL(connection)) {
                sql = "SELECT b.batch_id, b.insert_date, b.batch_identifier, b.sequence_number, b.is_complete, count(f.batch_file_id), sum(remote_size_bytes)"
                        + " FROM log.batch b"
                        + " INNER JOIN log.batch_file f"
                        + " ON f.batch_id = b.batch_id"
                        + " WHERE b.configuration_id = ?"
                        + " AND b.insert_date >= ?"
                        + " AND b.insert_date <= ?"
                        + " GROUP BY b.batch_id, b.insert_date, b.batch_identifier, b.sequence_number, b.is_complete"
                        + " ORDER BY b.insert_date desc";
            } else {
                sql = "SELECT b.batch_id, b.insert_date, b.batch_identifier, b.sequence_number, b.is_complete, count(f.batch_file_id), sum(remote_size_bytes)"
                        + " FROM batch b"
                        + " INNER JOIN batch_file f"
                        + " ON f.batch_id = b.batch_id"
                        + " WHERE b.configuration_id = ?"
                        + " AND b.insert_date >= ?"
                        + " AND b.insert_date <= ?"
                        + " GROUP BY b.batch_id, b.insert_date, b.batch_identifier, b.sequence_number, b.is_complete"
                        + " ORDER BY b.insert_date desc";
            }

            ps = connection.prepareStatement(sql);

            col = 1;
            ps.setString(col++, configurationId);
            ps.setTimestamp(col++, new Timestamp(dFrom.getTime()));
            ps.setTimestamp(col++, new Timestamp(dTo.getTime()));

            rs = ps.executeQuery();
            while (rs.next()) {
                col = 1;
                int batchId = rs.getInt(col++);
                Date dReceived = new Date(rs.getTimestamp(col++).getTime());
                String batchIdentifier = rs.getString(col++);
                int sequenceNumber = rs.getInt(col++);
                boolean isComplete = rs.getBoolean(col++);
                int numFiles = rs.getInt(col++);
                long sizeBytes = rs.getLong(col++);
                String sizeDesc = FileUtils.byteCountToDisplaySize(sizeBytes);

                ObjectNode obj = root.addObject();
                obj.put("id", batchId);
                obj.put("received", dReceived.getTime());
                obj.put("identifier", batchIdentifier);
                obj.put("sequenceNumber", sequenceNumber);
                obj.put("complete", isComplete);
                obj.put("fileCount", numFiles);
                obj.put("sizeBytes", sizeBytes);
                obj.put("sizeDesc", sizeDesc);
                ArrayNode orgsArr = obj.putArray("batchContents");

                Map<String, Boolean> hmOrgs = hmOrgsByBatch.get(new Integer(batchId));
                if (hmOrgs != null) {
                    for (String orgId: hmOrgs.keySet()) {
                        Boolean complete = hmOrgs.get(orgId);

                        ObjectNode orgObj = orgsArr.addObject();
                        orgObj.put("orgId", orgId);
                        orgObj.put("notified", complete.booleanValue());
                    }
                }
            }
            ps.close();

        } finally {
            if (ps != null) {
                ps.close();
            }
            entityManager.close();
        }

        return mapper.writeValueAsString(root);
    }


    private String getSftpReaderInstances() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode root = new ArrayNode(mapper.getNodeFactory());

        EntityManager entityManager = ConnectionManager.getSftpReaderEntityManager();
        PreparedStatement ps = null;
        try {
            SessionImpl session = (SessionImpl) entityManager.getDelegate();
            Connection connection = session.connection();

            String sql = null;
            if (ConnectionManager.isPostgreSQL(connection)) {
                sql = "SELECT instance_name FROM configuration.instance ORDER BY instance_name";
            } else {
                sql = "SELECT instance_name FROM instance ORDER BY instance_name";
            }

            ps = connection.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int col = 1;
                String instanceName = rs.getString(col++);

                ObjectNode obj = root.addObject();
                obj.put("name", instanceName);
            }
            ps.close();

        } finally {
            if (ps != null) {
                ps.close();
            }
            entityManager.close();
        }

        return mapper.writeValueAsString(root);
    }


    /**
     * all the below should be moved to core or similar if anything like it is needed elsewhere
     */
    private String getSftpReaderStatus(String filterInstanceName) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode root = new ArrayNode(mapper.getNodeFactory());

        EntityManager entityManager = ConnectionManager.getSftpReaderEntityManager();
        PreparedStatement ps = null;
        try {
            SessionImpl session = (SessionImpl) entityManager.getDelegate();
            Connection connection = session.connection();

            String sql = null;
            if (ConnectionManager.isPostgreSQL(connection)) {
                sql = "SELECT c.configuration_id, c.poll_frequency_seconds, c.configuration_friendly_name, i.instance_name, t.data_frequency_days"
                    + " FROM configuration.configuration c"
                    + " LEFT OUTER JOIN configuration.instance_configuration i"
                    + " ON c.configuration_id = i.configuration_id"
                    + " LEFT OUTER JOIN configuration.interface_type t"
                    + " ON c.interface_type_id = t.interface_type_id"
                    + " ORDER BY c.configuration_friendly_name";
            } else {
                sql = "SELECT c.configuration_id, c.poll_frequency_seconds, c.configuration_friendly_name, i.instance_name, t.data_frequency_days"
                        + " FROM configuration c"
                        + " LEFT OUTER JOIN instance_configuration i"
                        + " ON c.configuration_id = i.configuration_id"
                        + " LEFT OUTER JOIN interface_type t"
                        + " ON c.interface_type_id = t.interface_type_id"
                        + " ORDER BY c.configuration_friendly_name";
            }

            ps = connection.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int col = 1;
                String id = rs.getString(col++);
                int freq = rs.getInt(col++);
                String name = rs.getString(col++);
                String instanceName = rs.getString(col++);
                int dataFrequencyDays = rs.getInt(col++);

                if (filterInstanceName.equalsIgnoreCase("all")) {
                    //include all

                } else if (filterInstanceName.equals("active")) {
                    if (Strings.isNullOrEmpty(instanceName)) {
                        continue;
                    }

                } else if (filterInstanceName.equals("inactive")) {
                    if (!Strings.isNullOrEmpty(instanceName)) {
                        continue;
                    }

                } else {
                    if (instanceName == null
                            || !instanceName.equalsIgnoreCase(filterInstanceName)) {
                        continue;
                    }
                }

                ObjectNode child = root.addObject();
                child.put("id", id);
                child.put("name", name);
                child.put("pollFrequencySeconds", freq);
                child.put("dataFrequencyDays", dataFrequencyDays);
                child.put("instanceName", instanceName);
            }
            ps.close();

            //for each channel found, get more info
            for (int i=0; i<root.size(); i++) {
                ObjectNode channelNode = (ObjectNode)root.get(i);
                String id = channelNode.get("id").asText();

                //get the latest polling attempt
                if (ConnectionManager.isPostgreSQL(connection)) {
                    sql = "SELECT attempt_started, attempt_finished, exception_text, files_downloaded,"
                            + " batches_completed, batch_splits_notified_ok, batch_splits_notified_failure"
                            + " FROM log.configuration_polling_attempt"
                            + " WHERE configuration_id = ?"
                            + " ORDER BY attempt_started DESC"
                            + " LIMIT 1";
                } else {
                    sql = "SELECT attempt_started, attempt_finished, exception_text, files_downloaded,"
                            + " batches_completed, batch_splits_notified_ok, batch_splits_notified_failure"
                            + " FROM configuration_polling_attempt"
                            + " WHERE configuration_id = ?"
                            + " ORDER BY attempt_started DESC"
                            + " LIMIT 1";
                }
                ps = connection.prepareStatement(sql);

                ps.setString(1, id);

                rs = ps.executeQuery();
                if (rs.next()) {
                    int col = 1;
                    Date attemptStartDate = new Date(rs.getTimestamp(col++).getTime());
                    Date attemptEndDate = new Date(rs.getTimestamp(col++).getTime());
                    String exceptionText = rs.getString(col++);
                    int filesDownloaded = rs.getInt(col++);
                    int batchesCompleted = rs.getInt(col++);
                    int batchSplitsNotifiedOk = rs.getInt(col++);
                    int batchSplitsNotifiedFailure = rs.getInt(col++);

                    channelNode.put("latestPollingStart", attemptStartDate.getTime());
                    channelNode.put("latestPollingEnd", attemptEndDate.getTime());
                    channelNode.put("latestPollingException", exceptionText);
                    channelNode.put("latestPollingFilesDownloaded", filesDownloaded);
                    channelNode.put("latestPollingBatchesCompleted", batchesCompleted);
                    channelNode.put("latestPollingBatchSplitsNotifiedOk", batchSplitsNotifiedOk);
                    channelNode.put("latestPollingBatchSplitsNotifiedFailure", batchSplitsNotifiedFailure);
                }

                ps.close();


                //get the latest batch received
                if (ConnectionManager.isPostgreSQL(connection)) {
                    sql = "select b.batch_id, b.batch_identifier, b.insert_date, b.sequence_number, b.is_complete, count(1), sum(f.remote_size_bytes)"
                            + " from log.batch b"
                            + " left outer join log.batch_file f"
                            + " on f.batch_id = b.batch_id"
                            + " where b.configuration_id = ?"
                            + " group by b.batch_id, b.batch_identifier, b.insert_date, b.sequence_number, b.is_complete"
                            + " order by b.insert_date desc"
                            + " limit 1";
                } else {
                    sql = "select b.batch_id, b.batch_identifier, b.insert_date, b.sequence_number, b.is_complete, count(1), sum(f.remote_size_bytes)"
                            + " from batch b"
                            + " left outer join batch_file f"
                            + " on f.batch_id = b.batch_id"
                            + " where b.configuration_id = ?"
                            + " group by b.batch_id, b.batch_identifier, b.insert_date, b.sequence_number, b.is_complete"
                            + " order by b.insert_date desc"
                            + " limit 1";
                }
                ps = connection.prepareStatement(sql);

                ps.setString(1, id);

                rs = ps.executeQuery();
                if (rs.next()) {
                    int col = 1;
                    int batchId = rs.getInt(col++);
                    String batchIdentifier = rs.getString(col++);
                    Date insertDate = new Date(rs.getTimestamp(col++).getTime());
                    int sequenceNumber = rs.getInt(col++);
                    boolean isComplete = rs.getBoolean(col++);
                    int fileCount = rs.getInt(col++);
                    long extractSize = rs.getLong(col++);

                    String totalSizeReadable = FileUtils.byteCountToDisplaySize(extractSize);

                    channelNode.put("latestBatchId", batchId);
                    channelNode.put("latestBatchIdentifier", batchIdentifier);
                    channelNode.put("latestBatchReceived", insertDate.getTime());
                    channelNode.put("latestBatchSequenceNumber", sequenceNumber);
                    channelNode.put("latestBatchComplete", isComplete);
                    channelNode.put("latestBatchFileCount", fileCount);
                    channelNode.put("latestBatchSizeBytes", totalSizeReadable);
                }

                ps.close();

                //find the latest complete batch
                if (ConnectionManager.isPostgreSQL(connection)) {
                    sql = "SELECT batch_id, batch_identifier, insert_date, sequence_number"
                            + " FROM log.batch"
                            + " WHERE configuration_id = ? AND is_complete = true"
                            + " ORDER BY sequence_number desc"
                            + " LIMIT 1";
                } else {
                    sql = "SELECT batch_id, batch_identifier, insert_date, sequence_number"
                            + " FROM batch"
                            + " WHERE configuration_id = ? AND is_complete = true"
                            + " ORDER BY sequence_number desc"
                            + " LIMIT 1";
                }
                ps = connection.prepareStatement(sql);

                ps.setString(1, id);

                Integer latestCompleteBatchId = null;

                rs = ps.executeQuery();
                if (rs.next()) {
                    int col = 1;
                    latestCompleteBatchId = rs.getInt(col++);
                    String batchIdentifier = rs.getString(col++);
                    Date insertDate = new Date(rs.getTimestamp(col++).getTime());
                    int sequenceNumber = rs.getInt(col++);

                    channelNode.put("completeBatchId", latestCompleteBatchId);
                    channelNode.put("completeBatchIdentifier", batchIdentifier);
                    channelNode.put("completeBatchReceived", insertDate.getTime());
                    channelNode.put("completeBatchSequenceNumber", sequenceNumber);
                }

                ps.close();

                if (latestCompleteBatchId != null) {

                    //get any errors for orgs in this configuration - since errors posting to the Messaging API
                    //will most likely be on past exchanges and be blocking the latest exchange
                    Map<String, String> hmErrorsPerOrg = new HashMap<>();

                    if (ConnectionManager.isPostgreSQL(connection)) {
                        sql = "SELECT organisation_id, error_text"
                                + " FROM log.batch_split bs"
                                + " INNER JOIN log.batch b"
                                + " ON b.batch_id = bs.batch_id"
                                + " INNER JOIN log.notification_message m"
                                + " on m.batch_id = bs.batch_id"
                                + " and m.batch_split_id = bs.batch_split_id"
                                + " and not exists ("
                                + " select 1"
                                + " from log.notification_message m2"
                                + " where m2.batch_id = m.batch_id"
                                + " and m2.batch_split_id = m.batch_split_id"
                                + " and m2.timestamp < m.timestamp"
                                + " )"
                                + " WHERE b.configuration_id = ?"
                                + " AND b.is_complete = true"
                                + " AND bs.have_notified = false"
                                + " and m.error_text is not null";
                    } else {
                        sql = "SELECT organisation_id, error_text"
                                + " FROM batch_split bs"
                                + " INNER JOIN batch b"
                                + " ON b.batch_id = bs.batch_id"
                                + " INNER JOIN notification_message m"
                                + " on m.batch_id = bs.batch_id"
                                + " and m.batch_split_id = bs.batch_split_id"
                                + " and not exists ("
                                + " select 1"
                                + " from notification_message m2"
                                + " where m2.batch_id = m.batch_id"
                                + " and m2.batch_split_id = m.batch_split_id"
                                + " and m2.timestamp < m.timestamp"
                                + " )"
                                + " WHERE b.configuration_id = ?"
                                + " AND b.is_complete = true"
                                + " AND bs.have_notified = false"
                                + " and m.error_text is not null";
                    }
                    ps = connection.prepareStatement(sql);

                    ps.setString(1, id);

                    rs = ps.executeQuery();

                    while (rs.next()) {

                        int col = 1;
                        String orgId = rs.getString(col++);
                        String notificationError = rs.getString(col++);

                        hmErrorsPerOrg.put(orgId, notificationError);
                    }

                    ps.close();

                    //get the batch splits for the complete batch
                    if (ConnectionManager.isPostgreSQL(connection)) {
                        sql = "select s.batch_split_id, s.organisation_id, s.have_notified, m.inbound, m.error_text"
                                + " from log.batch_split s"
                                + " left outer join log.notification_message m"
                                + " on m.batch_id = s.batch_id"
                                + " and m.batch_split_id = s.batch_split_id"
                                + " and not exists ("
                                + " select 1"
                                + " from log.notification_message m2"
                                + " where m2.batch_id = m.batch_id"
                                + " and m2.batch_split_id = m.batch_split_id"
                                + " and m2.timestamp > m.timestamp"
                                + " )"
                                + " where s.batch_id = ?";
                    } else {
                        sql = "select s.batch_split_id, s.organisation_id, s.have_notified, m.inbound, m.error_text"
                                + " from batch_split s"
                                + " left outer join notification_message m"
                                + " on m.batch_id = s.batch_id"
                                + " and m.batch_split_id = s.batch_split_id"
                                + " and not exists ("
                                + " select 1"
                                + " from notification_message m2"
                                + " where m2.batch_id = m.batch_id"
                                + " and m2.batch_split_id = m.batch_split_id"
                                + " and m2.timestamp > m.timestamp"
                                + " )"
                                + " where s.batch_id = ?";
                    }
                    ps = connection.prepareStatement(sql);

                    ps.setInt(1, latestCompleteBatchId);

                    rs = ps.executeQuery();

                    ArrayNode arr = channelNode.putArray("completeBatchContents");

                    while (rs.next()) {

                        int col = 1;
                        int batchSplitId = rs.getInt(col++);
                        String orgId = rs.getString(col++);
                        boolean haveNotified = rs.getBoolean(col++);
                        String notificationResult = rs.getString(col++);
                        String notificationError = rs.getString(col++);

                        //if no error message for this record, then look in the map because there should be one
                        //for this org, but for an earlier batch, that's blocking this one for the org
                        if (!haveNotified
                                && Strings.isNullOrEmpty(notificationError)) {
                            notificationError = hmErrorsPerOrg.get(orgId);
                        }

                        ObjectNode orgNode = arr.addObject();
                        orgNode.put("batchSplitId", batchSplitId);
                        orgNode.put("orgId", orgId);
                        orgNode.put("notified", haveNotified);
                        orgNode.put("result", notificationResult);
                        orgNode.put("error", notificationError);
                    }

                    ps.close();
                }
            }

        } finally {
            if (ps != null) {
                ps.close();
            }
            entityManager.close();
        }

        return mapper.writeValueAsString(root);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/ignore")
    public Response ignoreBatchSplit(@Context SecurityContext sc,
                                     JsonSftpReaderIgnoreBatchSplitParameters parameters) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "Ignore", "Configuration ID", parameters.getConfigurationId(), "Batch ID", parameters.getBatchId(), "Batch Split ID", parameters.getBatchSplitId());

        ignoreBatchSplitImpl(parameters.getConfigurationId(), parameters.getBatchId(), parameters.getBatchSplitId(), parameters.getReason());

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }

    private void ignoreBatchSplitImpl(String configurationId, int batchId, int batchSplitId, String reason) throws Exception {
        Connection connection = ConnectionManager.getSftpReaderConnection();
        PreparedStatement psInsert = null;
        PreparedStatement psUpdate = null;
        try {
            String sql = null;
            if (ConnectionManager.isPostgreSQL(connection)) {
                sql = "INSERT INTO log.notification_message (batch_id, batch_split_id, configuration_id, message_uuid, timestamp, inbound, was_success)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?)";
            } else {
                sql = "INSERT INTO notification_message (batch_id, batch_split_id, configuration_id, message_uuid, timestamp, inbound, was_success)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?)";
            }
            psInsert = connection.prepareStatement(sql);

            Date d = new Date();

            int col = 1;
            psInsert.setInt(col++, batchId);
            psInsert.setInt(col++, batchSplitId);
            psInsert.setString(col++, configurationId);
            if (ConnectionManager.isPostgreSQL(connection)) {
                psInsert.setObject(col++, UUID.randomUUID());
            } else {
                psInsert.setString(col++, UUID.randomUUID().toString());
            }
            psInsert.setTimestamp(col++, new java.sql.Timestamp(d.getTime()));
            psInsert.setString(col++, reason);
            psInsert.setBoolean(col++, true);
            psInsert.executeUpdate();

            if (ConnectionManager.isPostgreSQL(connection)) {
                sql = "UPDATE log.batch_split"
                    + " SET have_notified = ?, notification_date = ?"
                    + " WHERE batch_split_id = ?";
            } else {
                sql = "UPDATE batch_split"
                        + " SET have_notified = ?, notification_date = ?"
                        + " WHERE batch_split_id = ?";
            }
            psUpdate = connection.prepareStatement(sql);

            col = 1;
            psUpdate.setBoolean(col++, true);
            psUpdate.setTimestamp(col++, new java.sql.Timestamp(d.getTime()));
            psUpdate.setInt(col++, batchSplitId);
            psUpdate.executeUpdate();

            connection.commit();

        } finally {
            if (psInsert != null) {
                psInsert.close();
            }
            if (psUpdate != null) {
                psUpdate.close();
            }
            connection.close();
        }
    }
}
