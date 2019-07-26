package org.endeavourhealth.ui.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

@Path("/hl7Receiver")
public class Hl7ReceiverEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(Hl7ReceiverEndpoint.class);

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Library);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/channelStatus")
    public Response getChannelStatus(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get HL7 Receiver Status");

        String ret = getHl7ReceiverStatus();
        //String ret = "{\"status\":\"OK\"}";

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/pause/{channelId}/{pause}")
    public Response pauseChannel(@Context SecurityContext sc,
                                 @PathParam(value = "channelId") int channelId,
                                 @PathParam(value = "pause") boolean pause) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Pause", "Channel ID", channelId, "Pause", pause);
        pauseHl7Channel(channelId, pause);

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }

    /**
     * all the below should really be moved to core, but this is all one-off stuff (hopefully)
     * if anyone ends up copying this, then it should be moved to core!
     */
    private static String getHl7ReceiverStatus() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode root = new ArrayNode(mapper.getNodeFactory());

        EntityManager entityManager = ConnectionManager.getHl7ReceiverEntityManager();
        PreparedStatement ps = null;
        try {
            SessionImpl session = (SessionImpl) entityManager.getDelegate();
            Connection connection = session.connection();

            String sql = null;
            if (ConnectionManager.isPostgreSQL(connection)) {
                sql = "SELECT channel_id, channel_name FROM configuration.channel ORDER BY channel_id";
            } else {
                sql = "SELECT channel_id, channel_name FROM configuration_channel ORDER BY channel_id";
            }

            ps = connection.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int col = 1;
                int id = rs.getInt(col++);
                String name = rs.getString(col++);

                ObjectNode child = root.addObject();
                child.put("id", id);
                child.put("name", name);
            }

            ps.close();

            //for each channel found, get more info
            for (int i=0; i<root.size(); i++) {
                ObjectNode channelNode = (ObjectNode)root.get(i);
                int channelId = channelNode.get("id").intValue();

                //get the paused status
                if (ConnectionManager.isPostgreSQL(connection)) {
                    sql = "SELECT channel_option_value FROM configuration.channel_option WHERE channel_id = ? AND channel_option_type = 'PauseProcessor'";
                } else {
                    sql = "SELECT channel_option_value FROM configuration_channel_option WHERE channel_id = ? AND channel_option_type = 'PauseProcessor'";
                }
                ps = connection.prepareStatement(sql);

                ps.setInt(1, channelId);
                rs = ps.executeQuery();
                if (rs.next()) {
                    String val = rs.getString(1);
                    boolean paused = Boolean.parseBoolean(val);
                    if (paused) {
                        channelNode.put("paused", paused);
                    }
                }

                ps.close();

                //get the last message received
                if (ConnectionManager.isPostgreSQL(connection)) {
                    sql = "SELECT message_id, log_date FROM log.last_message WHERE channel_id = ?";
                } else {
                    sql = "SELECT message_id, log_date FROM last_message WHERE channel_id = ?";
                }
                ps = connection.prepareStatement(sql);

                ps.setInt(1, channelId);
                rs = ps.executeQuery();
                if (rs.next()) {
                    int messageId = rs.getInt(1);
                    Date logDate = new Date(rs.getTimestamp(2).getTime());

                    channelNode.put("lastMessageId", messageId);
                    channelNode.put("lastMessageReceived", logDate.getTime());
                }

                ps.close();

                //get any errors in the ADT->FHIR transform
                if (ConnectionManager.isPostgreSQL(connection)) {
                    sql = "SELECT message_id, log_date, inbound_message_type, error_message FROM log.message WHERE error_message is not null AND channel_id = ?";
                } else {
                    sql = "SELECT message_id, log_date, inbound_message_type, error_message FROM message WHERE error_message is not null AND channel_id = ?";
                }
                ps = connection.prepareStatement(sql);

                ps.setInt(1, channelId);
                rs = ps.executeQuery();
                if (rs.next()) {
                    int col = 1;
                    int messageId = rs.getInt(col++);
                    Date logDate = new Date(rs.getTimestamp(col++).getTime());
                    String messageType = rs.getString(col++);
                    String messageError = rs.getString(col++);

                    channelNode.put("errorMessageId", messageId);
                    channelNode.put("errorMessageReceived", logDate.getTime());
                    channelNode.put("errorMessageType", messageType);
                    channelNode.put("errorMessage", messageError);
                }

                ps.close();

                //get the size of the ADT->FHIR transform queue
                if (ConnectionManager.isPostgreSQL(connection)) {
                    sql = "SELECT count(1) FROM log.message_queue WHERE channel_id = ?";
                } else {
                    sql = "SELECT count(1) FROM message_queue WHERE channel_id = ?";
                }
                ps = connection.prepareStatement(sql);

                ps.setInt(1, channelId);
                rs = ps.executeQuery();
                if (rs.next()) {
                    int queueSize = rs.getInt(1);

                    channelNode.put("transformQueueSize", queueSize);
                }

                ps.close();

                //get the details of the oldest thing in the transform queue
                if (ConnectionManager.isPostgreSQL(connection)) {
                    sql = "SELECT q.message_id, q.log_date, m.inbound_message_type"
                            + " FROM log.message_queue q"
                            + " INNER JOIN log.message m"
                            + " ON m.message_id = q.message_id"
                            + " WHERE q.channel_id = ?"
                            + " ORDER BY q.log_date asc"
                            + " LIMIT 1";
                } else {
                    sql = "SELECT q.message_id, q.log_date, m.inbound_message_type"
                            + " FROM message_queue q"
                            + " INNER JOIN message m"
                            + " ON m.message_id = q.message_id"
                            + " WHERE q.channel_id = ?"
                            + " ORDER BY q.log_date asc"
                            + " LIMIT 1";
                }
                ps = connection.prepareStatement(sql);

                Integer oldestMessageId = null;

                ps.setInt(1, channelId);
                rs = ps.executeQuery();
                if (rs.next()) {
                    int col = 1;
                    oldestMessageId = rs.getInt(col++);
                    Date oldestMessageDate = new Date(rs.getTimestamp(col++).getTime());
                    String oldestMessageType = rs.getString(col++);

                    channelNode.put("transformQueueFirstMessageId", oldestMessageId);
                    channelNode.put("transformQueueFirstMessageDate", oldestMessageDate.getTime());
                    channelNode.put("transformQueueFirstMessageType", oldestMessageType);
                }

                ps.close();

                //get extra details on the oldest thing in the queue
                if (oldestMessageId != null) {

                    if (ConnectionManager.isPostgreSQL(connection)) {
                        sql = "SELECT processing_attempt_id, message_status_date, error_message, description"
                                + " FROM log.message_status_history h"
                                + " INNER JOIN log.message_status s"
                                + " ON s.message_status_id = h.message_status_id"
                                + " WHERE h.message_id = ?"
                                + " ORDER BY h.message_status_date DESC";
                    } else {
                        sql = "SELECT processing_attempt_id, message_status_date, error_message, description"
                                + " FROM message_status_history h"
                                + " INNER JOIN message_status s"
                                + " ON s.message_status_id = h.message_status_id"
                                + " WHERE h.message_id = ?"
                                + " ORDER BY h.message_status_date DESC";
                    }
                    ps = connection.prepareStatement(sql);

                    ArrayNode arr = channelNode.putArray("transformQueueFirstStatus");

                    ps.setInt(1, oldestMessageId);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        int col = 1;
                        int transformAttempt = rs.getInt(col++);
                        Date statusDate = new Date(rs.getTimestamp(col++).getTime());
                        String errorMessage = rs.getString(col++);
                        String statusDesc = rs.getString(col++);

                        ObjectNode obj = arr.addObject();
                        obj.put("date", statusDate.getTime());
                        obj.put("status", statusDesc);
                        obj.put("transformAttempt", transformAttempt);
                        obj.put("error", errorMessage);
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

    /**
     * all the below should really be moved to core, but this is all one-off stuff (hopefully)
     * if anyone ends up copying this, then it should be moved to core!
     */
    private static void pauseHl7Channel(int channelId, boolean pause) throws Exception {

        EntityManager entityManager = ConnectionManager.getHl7ReceiverEntityManager();
        PreparedStatement psUpdate = null;
        try {
            SessionImpl session = (SessionImpl) entityManager.getDelegate();
            Connection connection = session.connection();

            String sql = null;
            if (ConnectionManager.isPostgreSQL(connection)) {
                sql = "INSERT INTO configuration.channel_option (channel_id, channel_option_type, channel_option_value)"
                        + " VALUES (?, ?, ?)"
                        + " ON CONFLICT (channel_id, channel_option_type) DO UPDATE SET"
                        + " channel_option_value = EXCLUDED.channel_option_value";

            } else {
                sql = "INSERT INTO configuration_channel_option (channel_id, channel_option_type, channel_option_value)"
                        + " VALUES (?, ?, ?)"
                        + " ON DUPLICATE KEY UPDATE"
                        + " channel_option_value = VALUES(channel_option_value)";
            }

            psUpdate = connection.prepareStatement(sql);

            entityManager.getTransaction().begin();

            int col = 1;
            psUpdate.setInt(col++, channelId);
            psUpdate.setString(col++, "PauseProcessor");
            psUpdate.setString(col++, "" + pause);

            psUpdate.executeUpdate();

            entityManager.getTransaction().commit();

        } catch (Exception ex) {
            entityManager.getTransaction().rollback();
            throw ex;

        } finally {
            if (psUpdate != null) {
                psUpdate.close();
            }

            entityManager.close();
        }

    }
}
