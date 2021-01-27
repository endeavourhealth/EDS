package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
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
import java.util.*;

@Path("/sftpReader")
public class SftpReaderEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(SftpReaderEndpoint.class);

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Library);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="SftpReaderEndpoint.instances")
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
    @Timed(absolute = true, name="SftpReaderEndpoint.status")
    @Path("/status")
    public Response getChannelStatus(@Context SecurityContext sc, @QueryParam("configurationId") String configurationId) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get SFTP Reader Instance Status", "ConfigurationId", configurationId);

        String ret = getSftpReaderStatus(configurationId);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="SftpReaderEndpoint.history")
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
                sql = "SELECT b.batch_id, s.batch_split_id, s.organisation_id, s.have_notified, s.is_bulk, m.inbound, m.error_text"
                        + " FROM log.batch b"
                        + " INNER JOIN log.batch_split s"
                        + " ON s.batch_id = b.batch_id"
                        + " LEFT OUTER JOIN log.notification_message m"
                        + " on m.batch_id = s.batch_id"
                        + " and m.batch_split_id = s.batch_split_id"
                        + " and not exists ("
                        + " select 1"
                        + " from log.notification_message m2"
                        + " where m2.batch_id = m.batch_id"
                        + " and m2.batch_split_id = m.batch_split_id"
                        + " and m2.timestamp > m.timestamp"
                        + " )"
                        + " WHERE b.configuration_id = ?"
                        + " AND b.insert_date >= ?"
                        + " AND b.insert_date <= ?"
                        + " ORDER BY s.organisation_id";

            } else {
                sql = "SELECT b.batch_id, s.batch_split_id, s.organisation_id, s.have_notified, s.is_bulk, m.inbound, m.error_text"
                        + " FROM batch b"
                        + " INNER JOIN batch_split s"
                        + " ON s.batch_id = b.batch_id"
                        + " LEFT OUTER JOIN notification_message m"
                        + " on m.batch_id = s.batch_id"
                        + " and m.batch_split_id = s.batch_split_id"
                        + " and not exists ("
                        + " select 1"
                        + " from notification_message m2"
                        + " where m2.batch_id = m.batch_id"
                        + " and m2.batch_split_id = m.batch_split_id"
                        + " and m2.timestamp > m.timestamp"
                        + " )"
                        + " WHERE b.configuration_id = ?"
                        + " AND b.insert_date >= ?"
                        + " AND b.insert_date <= ?"
                        + " ORDER BY s.organisation_id";
            }

            ps = connection.prepareStatement(sql);

            int col = 1;
            ps.setString(col++, configurationId);
            ps.setTimestamp(col++, new Timestamp(dFrom.getTime()));
            ps.setTimestamp(col++, new Timestamp(dTo.getTime()));

            Map<Integer, List<BatchSplit>> hmOrgsByBatch = new HashMap<>(); //bit nasty having a map of maps, but quick

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                col = 1;
                int batchId = rs.getInt(col++);
                int batchSplitId = rs.getInt(col++);
                String orgId = rs.getString(col++);
                boolean notified = rs.getBoolean(col++);
                boolean isBulk = rs.getBoolean(col++);
                String notificationResult = rs.getString(col++);
                String errorText = rs.getString(col++);

                BatchSplit s = new BatchSplit();
                s.setBatchSplitId(batchSplitId);
                s.setOrgId(orgId);
                s.setNotified(notified);
                s.setBulk(isBulk);
                s.setNotificationResult(notificationResult);
                s.setErrorText(errorText);

                List<BatchSplit> l = hmOrgsByBatch.get(new Integer(batchId));
                if (l == null) {
                    l = new ArrayList<>();
                    hmOrgsByBatch.put(new Integer(batchId), l);
                }
                l.add(s);
            }

            ps.close();
            ps = null;

            //get the details of the batches and files
            sql = null;
            if (ConnectionManager.isPostgreSQL(connection)) {
                sql = "SELECT b.batch_id, b.insert_date, b.batch_identifier, b.sequence_number, b.is_complete, b.extract_date, b.extract_cutoff, count(f.batch_file_id), sum(remote_size_bytes)"
                        + " FROM log.batch b"
                        + " INNER JOIN log.batch_file f"
                        + " ON f.batch_id = b.batch_id"
                        + " WHERE b.configuration_id = ?"
                        + " AND b.insert_date >= ?"
                        + " AND b.insert_date <= ?"
                        + " GROUP BY b.batch_id, b.insert_date, b.batch_identifier, b.sequence_number, b.is_complete, b.extract_date, b.extract_cutoff"
                        + " ORDER BY b.insert_date desc";
            } else {
                sql = "SELECT b.batch_id, b.insert_date, b.batch_identifier, b.sequence_number, b.is_complete, b.extract_date, b.extract_cutoff, count(f.batch_file_id), sum(remote_size_bytes)"
                        + " FROM batch b"
                        + " INNER JOIN batch_file f"
                        + " ON f.batch_id = b.batch_id"
                        + " WHERE b.configuration_id = ?"
                        + " AND b.insert_date >= ?"
                        + " AND b.insert_date <= ?"
                        + " GROUP BY b.batch_id, b.insert_date, b.batch_identifier, b.sequence_number, b.is_complete, b.extract_date, b.extract_cutoff"
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

                java.sql.Timestamp ts = rs.getTimestamp(col++);
                Date extractDate = null;
                if (ts != null) {
                    extractDate = new Date(ts.getTime());
                }

                ts = rs.getTimestamp(col++);
                Date extractCutoff = null;
                if (ts != null) {
                    extractCutoff = new Date(ts.getTime());
                }

                int numFiles = rs.getInt(col++);
                long sizeBytes = rs.getLong(col++);
                String sizeDesc = FileUtils.byteCountToDisplaySize(sizeBytes);

                ObjectNode obj = root.addObject();
                obj.put("id", batchId);
                obj.put("received", dReceived.getTime());
                obj.put("identifier", batchIdentifier);
                obj.put("sequenceNumber", sequenceNumber);
                obj.put("complete", isComplete);
                if (extractDate != null) {
                    obj.put("extractDate", extractDate.getTime());
                }
                if (extractCutoff != null) {
                    obj.put("extractCutoff", extractCutoff.getTime());
                }
                obj.put("fileCount", numFiles);
                obj.put("sizeBytes", sizeBytes);
                obj.put("sizeDesc", sizeDesc);
                ArrayNode orgsArr = obj.putArray("batchContents");

                List<BatchSplit> batchSplits = hmOrgsByBatch.get(new Integer(batchId));
                if (batchSplits != null) {
                    for (BatchSplit s: batchSplits) {

                        ObjectNode orgNode = orgsArr.addObject();
                        orgNode.put("batchSplitId", s.getBatchSplitId());
                        orgNode.put("orgId", s.getOrgId());
                        orgNode.put("notified", s.isNotified());
                        orgNode.put("isBulk", s.isBulk);
                        orgNode.put("result", s.getNotificationResult());
                        orgNode.put("error", s.getErrorText());
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
                sql = "SELECT c.configuration_id, c.configuration_friendly_name, i.instance_name, p.dt_paused "
                    + "FROM configuration.configuration c "
                    + "INNER JOIN configuration.instance_configuration i "
                    + "ON i.configuration_id = c.configuration_id "
                    + "LEFT OUTER JOIN configuration.configuration_paused_notifying p "
                    + "ON p.configuration_id = c.configuration_id";
            } else {
                sql = "SELECT c.configuration_id, c.configuration_friendly_name, i.instance_name, p.dt_paused "
                    + "FROM configuration c "
                    + "INNER JOIN instance_configuration i "
                    + "ON i.configuration_id = c.configuration_id "
                    + "LEFT OUTER JOIN configuration_paused_notifying p "
                    + "ON p.configuration_id = c.configuration_id";
            }
            ps = connection.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int col = 1;
                String configurationId = rs.getString(col++);
                String friendlyName = rs.getString(col++);
                String instanceName = rs.getString(col++);
                Timestamp tsPaused = rs.getTimestamp(col++);

                //configurations that are no longer in use are assigned to a special instance name
                if (instanceName == null
                    || instanceName.equals("NOT_USED")) {
                    continue;
                }

                ObjectNode obj = root.addObject();
                obj.put("instanceName", instanceName);
                obj.put("configurationId", configurationId);
                obj.put("friendlyName", friendlyName);
                if (tsPaused != null) {
                    obj.put("dtPaused", tsPaused.getTime());
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


    /**
     * all the below should be moved to core or similar if anything like it is needed elsewhere
     */
    private String getSftpReaderStatus(String configurationId) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = new ObjectNode(mapper.getNodeFactory());

        EntityManager entityManager = ConnectionManager.getSftpReaderEntityManager();
        PreparedStatement ps = null;
        try {
            SessionImpl session = (SessionImpl) entityManager.getDelegate();
            Connection connection = session.connection();

            String sql = null;
            if (ConnectionManager.isPostgreSQL(connection)) {
                sql = "SELECT c.poll_frequency_seconds, t.data_frequency_days"
                    + " FROM configuration.configuration c"
                    + " LEFT OUTER JOIN configuration.interface_type t"
                    + " ON c.interface_type_id = t.interface_type_id"
                    + " WHERE c.configuration_id = ?";
            } else {
                sql = "SELECT c.poll_frequency_seconds, t.data_frequency_days"
                        + " FROM configuration c"
                        + " LEFT OUTER JOIN interface_type t"
                        + " ON c.interface_type_id = t.interface_type_id"
                        + " WHERE c.configuration_id = ?";
            }

            ps = connection.prepareStatement(sql);
            ps.setString(1, configurationId);

            ResultSet rs = ps.executeQuery();
            rs.next();

            int col = 1;
            int freq = rs.getInt(col++);
            int dataFrequencyDays = rs.getInt(col++);

            root.put("id", configurationId);
            root.put("pollFrequencySeconds", freq);
            root.put("dataFrequencyDays", dataFrequencyDays);

            ps.close();


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
            ps.setString(1, configurationId);

            rs = ps.executeQuery();
            if (rs.next()) {
                col = 1;
                Date attemptStartDate = new Date(rs.getTimestamp(col++).getTime());
                Date attemptEndDate = new Date(rs.getTimestamp(col++).getTime());
                String exceptionText = rs.getString(col++);
                int filesDownloaded = rs.getInt(col++);
                int batchesCompleted = rs.getInt(col++);
                int batchSplitsNotifiedOk = rs.getInt(col++);
                int batchSplitsNotifiedFailure = rs.getInt(col++);

                root.put("latestPollingStart", attemptStartDate.getTime());
                root.put("latestPollingEnd", attemptEndDate.getTime());
                root.put("latestPollingException", exceptionText);
                root.put("latestPollingFilesDownloaded", filesDownloaded);
                root.put("latestPollingBatchesCompleted", batchesCompleted);
                root.put("latestPollingBatchSplitsNotifiedOk", batchSplitsNotifiedOk);
                root.put("latestPollingBatchSplitsNotifiedFailure", batchSplitsNotifiedFailure);
            }

            ps.close();


            //get the latest batch received
            if (ConnectionManager.isPostgreSQL(connection)) {
                sql = "select b.batch_id, b.batch_identifier, b.insert_date, b.sequence_number, b.is_complete, b.extract_date, b.extract_cutoff, count(1), sum(f.remote_size_bytes)"
                        + " from log.batch b"
                        + " left outer join log.batch_file f"
                        + " on f.batch_id = b.batch_id"
                        + " where b.configuration_id = ?"
                        + " group by b.batch_id, b.batch_identifier, b.insert_date, b.sequence_number, b.is_complete, b.extract_date, b.extract_cutoff"
                        + " order by b.insert_date desc"
                        + " limit 1";
            } else {
                sql = "select b.batch_id, b.batch_identifier, b.insert_date, b.sequence_number, b.is_complete, b.extract_date, b.extract_cutoff, count(1), sum(f.remote_size_bytes)"
                        + " from batch b"
                        + " left outer join batch_file f"
                        + " on f.batch_id = b.batch_id"
                        + " where b.configuration_id = ?"
                        + " group by b.batch_id, b.batch_identifier, b.insert_date, b.sequence_number, b.is_complete, b.extract_date, b.extract_cutoff"
                        + " order by b.insert_date desc"
                        + " limit 1";
            }
            ps = connection.prepareStatement(sql);
            ps.setString(1, configurationId);

            rs = ps.executeQuery();
            if (rs.next()) {
                col = 1;
                int batchId = rs.getInt(col++);
                String batchIdentifier = rs.getString(col++);
                Date insertDate = new Date(rs.getTimestamp(col++).getTime());
                int sequenceNumber = rs.getInt(col++);
                boolean isComplete = rs.getBoolean(col++);

                java.sql.Timestamp ts = rs.getTimestamp(col++);
                Date extractDate = null;
                if (ts != null) {
                    extractDate = new Date(ts.getTime());
                }

                ts = rs.getTimestamp(col++);
                Date extractCutoff = null;
                if (ts != null) {
                    extractCutoff = new Date(ts.getTime());
                }

                int fileCount = rs.getInt(col++);
                long extractSize = rs.getLong(col++);

                String totalSizeReadable = FileUtils.byteCountToDisplaySize(extractSize);

                root.put("latestBatchId", batchId);
                root.put("latestBatchIdentifier", batchIdentifier);
                root.put("latestBatchReceived", insertDate.getTime());
                if (extractDate != null) {
                    root.put("latestBatchExtractDate", extractDate.getTime());
                }
                if (extractCutoff != null) {
                    root.put("latestBatchExtractCutoff", extractCutoff.getTime());
                }
                root.put("latestBatchSequenceNumber", sequenceNumber);
                root.put("latestBatchComplete", isComplete);
                root.put("latestBatchFileCount", fileCount);
                root.put("latestBatchSizeBytes", totalSizeReadable);
            }

            ps.close();

            //find the latest complete batch
            if (ConnectionManager.isPostgreSQL(connection)) {
                sql = "SELECT batch_id, batch_identifier, insert_date, sequence_number, complete_date, extract_date, extract_cutoff"
                        + " FROM log.batch"
                        + " WHERE configuration_id = ? AND is_complete = true"
                        + " ORDER BY sequence_number desc"
                        + " LIMIT 1";
            } else {
                sql = "SELECT batch_id, batch_identifier, insert_date, sequence_number, complete_date, extract_date, extract_cutoff"
                        + " FROM batch"
                        + " WHERE configuration_id = ? AND is_complete = true"
                        + " ORDER BY sequence_number desc"
                        + " LIMIT 1";
            }
            ps = connection.prepareStatement(sql);
            ps.setString(1, configurationId);

            Integer latestCompleteBatchId = null;

            rs = ps.executeQuery();
            if (rs.next()) {
                col = 1;
                latestCompleteBatchId = rs.getInt(col++);
                String batchIdentifier = rs.getString(col++);
                Date insertDate = new Date(rs.getTimestamp(col++).getTime());
                int sequenceNumber = rs.getInt(col++);

                java.sql.Timestamp ts = rs.getTimestamp(col++);
                Date completeDate = null;
                if (ts != null) {
                    completeDate = new Date(ts.getTime());
                }

                ts = rs.getTimestamp(col++);
                Date extractDate = null;
                if (ts != null) {
                    extractDate = new Date(ts.getTime());
                }

                ts = rs.getTimestamp(col++);
                Date extractCutoff = null;
                if (ts != null) {
                    extractCutoff = new Date(ts.getTime());
                }

                root.put("completeBatchId", latestCompleteBatchId);
                if (completeDate != null) {
                    root.put("completeBatchCompletionDate", completeDate.getTime());
                }
                root.put("completeBatchIdentifier", batchIdentifier);
                root.put("completeBatchReceived", insertDate.getTime());
                if (extractDate != null) {
                    root.put("completeBatchExtractDate", extractDate.getTime());
                }
                if (extractCutoff != null) {
                    root.put("completeBatchExtractCutoff", extractCutoff.getTime());
                }
                root.put("completeBatchSequenceNumber", sequenceNumber);
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
                ps.setString(1, configurationId);

                rs = ps.executeQuery();

                while (rs.next()) {

                    col = 1;
                    String orgId = rs.getString(col++);
                    String notificationError = rs.getString(col++);

                    hmErrorsPerOrg.put(orgId, notificationError);
                }

                ps.close();

                //get the batch splits for the complete batch
                if (ConnectionManager.isPostgreSQL(connection)) {
                    sql = "select s.batch_split_id, s.organisation_id, s.have_notified, s.is_bulk, m.inbound, m.error_text"
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
                            + " where s.batch_id = ?"
                            + " ORDER BY s.organisation_id";
                } else {
                    sql = "select s.batch_split_id, s.organisation_id, s.have_notified, s.is_bulk, m.inbound, m.error_text"
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
                            + " where s.batch_id = ?"
                            + " ORDER BY s.organisation_id";
                }
                ps = connection.prepareStatement(sql);

                ps.setInt(1, latestCompleteBatchId);

                rs = ps.executeQuery();

                ArrayNode arr = root.putArray("completeBatchContents");

                while (rs.next()) {

                    col = 1;
                    int batchSplitId = rs.getInt(col++);
                    String orgId = rs.getString(col++);
                    boolean haveNotified = rs.getBoolean(col++);
                    boolean isBulk = rs.getBoolean(col++);
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
                    orgNode.put("isBulk", isBulk);
                    orgNode.put("result", notificationResult);
                    orgNode.put("error", notificationError);
                }

                ps.close();
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

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/togglePause")
    public Response togglePause(@Context SecurityContext sc,
                                     JsonSftpReaderIgnoreBatchSplitParameters parameters) throws Exception { //just re-using this object since it's already got the field we need
        super.setLogbackMarkers(sc);

        String configurationId = parameters.getConfigurationId();
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "TogglePause", "Configuration ID", configurationId);

        Map<String, Date> pausedState = getPausedState();
        boolean isPaused = pausedState.get(configurationId) != null;

        Connection connection = ConnectionManager.getSftpReaderConnection();
        PreparedStatement ps = null;
        try {
            String sql = null;
            if (isPaused) {
                if (ConnectionManager.isPostgreSQL(connection)) {
                    sql = "DELETE FROM configuration.configuration_paused_notifying WHERE configuration_id = ?";
                } else {
                    sql = "DELETE FROM configuration_paused_notifying WHERE configuration_id = ?";
                }
            } else {
                if (ConnectionManager.isPostgreSQL(connection)) {
                    sql = "INSERT INTO configuration.configuration_paused_notifying (configuration_id) VALUES (?)";
                } else {
                    sql = "INSERT INTO configuration_paused_notifying (configuration_id) VALUES (?)";
                }
            }
            ps = connection.prepareStatement(sql);
            ps.setString(1, configurationId);
            ps.executeUpdate();
            connection.commit();

        } catch (Exception ex) {
            connection.rollback();
            throw ex;

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/togglePauseAll")
    public Response togglePauseAll(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "TogglePauseAll");

        //work out whether to pause or not based on which is most
        List<String> paused = new ArrayList<>();
        List<String> notPaused = new ArrayList<>();

        Map<String, Date> pausedState = getPausedState();
        for (String s: pausedState.keySet()) {
            boolean isPaused = pausedState.get(s) != null;
            if (isPaused) {
                paused.add(s);
            } else {
                notPaused.add(s);
            }
        }

        Connection connection = ConnectionManager.getSftpReaderConnection();
        PreparedStatement ps = null;
        try {
            String sql = null;
            List<String> configs = null;

            if (paused.size() > notPaused.size()) {
                configs = paused;

                if (ConnectionManager.isPostgreSQL(connection)) {
                    sql = "DELETE FROM configuration.configuration_paused_notifying WHERE configuration_id = ?";
                } else {
                    sql = "DELETE FROM configuration_paused_notifying WHERE configuration_id = ?";
                }
            } else {
                configs = notPaused;

                if (ConnectionManager.isPostgreSQL(connection)) {
                    sql = "INSERT INTO configuration.configuration_paused_notifying (configuration_id) VALUES (?)";
                } else {
                    sql = "INSERT INTO configuration_paused_notifying (configuration_id) VALUES (?)";
                }
            }
            ps = connection.prepareStatement(sql);

            for (String configurationId: configs) {
                ps.setString(1, configurationId);
                ps.addBatch();
            }
            ps.executeBatch();

            connection.commit();

        } catch (Exception ex) {
            connection.rollback();
            throw ex;

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }

    private Map<String, Date> getPausedState() throws Exception {

        Connection connection = ConnectionManager.getSftpReaderConnection();
        PreparedStatement ps = null;
        try {
            String sql = null;
            if (ConnectionManager.isPostgreSQL(connection)) {
                sql = "SELECT c.configuration_id, p.dt_paused FROM configuration.configuration c LEFT OUTER JOIN configuration.configuration_paused_notifying p ON p.configuration_id = c.configuration_id;";
            } else {
                sql = "SELECT c.configuration_id, p.dt_paused FROM configuration c LEFT OUTER JOIN configuration_paused_notifying p ON p.configuration_id = c.configuration_id;";
            }
            ps = connection.prepareStatement(sql);

            Map<String, Date> ret = new HashMap<>();

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String configurationId = rs.getString(1);
                Date dtPaused = null;

                Timestamp ts = rs.getTimestamp(2);
                if (ts != null) {
                    dtPaused = new java.util.Date(ts.getTime());
                }

                ret.put(configurationId, dtPaused);
            }

            return ret;

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }

    /**
     * temporary helper class
     */
    static class BatchSplit {
        private int batchSplitId;
        private String orgId;
        private boolean notified;
        private boolean isBulk;
        private String notificationResult;
        private String errorText;

        public int getBatchSplitId() {
            return batchSplitId;
        }

        public void setBatchSplitId(int batchSplitId) {
            this.batchSplitId = batchSplitId;
        }

        public String getOrgId() {
            return orgId;
        }

        public void setOrgId(String orgId) {
            this.orgId = orgId;
        }

        public boolean isNotified() {
            return notified;
        }

        public void setNotified(boolean notified) {
            this.notified = notified;
        }

        public boolean isBulk() {
            return isBulk;
        }

        public void setBulk(boolean bulk) {
            isBulk = bulk;
        }

        public String getNotificationResult() {
            return notificationResult;
        }

        public void setNotificationResult(String notificationResult) {
            this.notificationResult = notificationResult;
        }

        public String getErrorText() {
            return errorText;
        }

        public void setErrorText(String errorText) {
            this.errorText = errorText;
        }
    }
}
