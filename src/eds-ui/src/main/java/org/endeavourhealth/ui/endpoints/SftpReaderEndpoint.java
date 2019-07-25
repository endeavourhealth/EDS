package org.endeavourhealth.ui.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.hibernate.internal.SessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

@Path("/sftpReader")
public class SftpReaderEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(SftpReaderEndpoint.class);

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Library);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/status")
    public Response getChannelStatus(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get HL7 Receiver Status");

        String ret = getSftpReaderStatus();

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }


    /**
     * all the below should be moved to core or similar if anything like it is needed elsewhere
     */
    private String getSftpReaderStatus() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode root = new ArrayNode(mapper.getNodeFactory());

        EntityManager entityManager = ConnectionManager.getSftpReaderEntityManager();
        PreparedStatement ps = null;
        try {
            SessionImpl session = (SessionImpl) entityManager.getDelegate();
            Connection connection = session.connection();

            String sql = null;
            if (ConnectionManager.isPostgreSQL(connection)) {
                sql = "select configuration_id, poll_frequency_seconds, configuration_friendly_name from configuration.configuration";
            } else {
                sql = "select configuration_id, poll_frequency_seconds, configuration_friendly_name from configuration c";
            }

            ps = connection.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int col = 1;
                String id = rs.getString(col++);
                int freq = rs.getInt(col++);
                String name = rs.getString(col++);

                ObjectNode child = root.addObject();
                child.put("id", id);
                child.put("name", name);
                child.put("pollFrequency", freq);
            }
            ps.close();

            //for each channel found, get more info
            for (int i=0; i<root.size(); i++) {
                ObjectNode channelNode = (ObjectNode)root.get(i);
                String id = channelNode.get("id").asText();

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
                    int extractSize = rs.getInt(col++);

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

                    //get the batch splits for the complete batch
                    if (ConnectionManager.isPostgreSQL(connection)) {
                        sql = "select s.organisation_id, s.have_notified, m.inbound, m.error_text"
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
                        sql = "select s.organisation_id, s.have_notified, m.inbound, m.error_text"
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
                        String orgId = rs.getString(col++);
                        boolean haveNotified = rs.getBoolean(col++);
                        String notificationResult = rs.getString(col++);
                        String notificationError = rs.getString(col++);

                        ObjectNode child = arr.addObject();
                        child.put("orgId", orgId);
                        child.put("notified", haveNotified);
                        child.put("result", notificationResult);
                        child.put("error", notificationError);
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
}
