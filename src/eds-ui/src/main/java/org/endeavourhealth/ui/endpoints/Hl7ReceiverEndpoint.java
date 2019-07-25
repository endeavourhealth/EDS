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
        PreparedStatement psChannels = null;
        PreparedStatement psPaused = null;
        PreparedStatement psLastMessage = null;
        PreparedStatement psErrors = null;
        try {
            SessionImpl session = (SessionImpl) entityManager.getDelegate();
            Connection connection = session.connection();

            String sql = null;
            if (ConnectionManager.isPostgreSQL(connection)) {
                sql = "SELECT channel_id, channel_name FROM configuration.channel";
            } else {
                sql = "SELECT channel_id, channel_name FROM configuration_channel";
            }

            psChannels = connection.prepareStatement(sql);

            ResultSet rs = psChannels.executeQuery();
            while (rs.next()) {
                int col = 1;
                int id = rs.getInt(col++);
                String name = rs.getString(col++);

                ObjectNode child = root.addObject();
                child.put("id", id);
                child.put("name", name);
            }

            if (ConnectionManager.isPostgreSQL(connection)) {
                sql = "SELECT channel_option_value FROM configuration.channel_option WHERE channel_id = ? AND channel_option_type = 'PauseProcessor'";
            } else {
                sql = "SELECT channel_option_value FROM configuration_channel_option WHERE channel_id = ? AND channel_option_type = 'PauseProcessor'";
            }
            psPaused = connection.prepareStatement(sql);

            if (ConnectionManager.isPostgreSQL(connection)) {
                sql = "SELECT message_id, log_date FROM log.last_message WHERE channel_id = ?";
            } else {
                sql = "SELECT message_id, log_date FROM last_message WHERE channel_id = ?";
            }
            psLastMessage = connection.prepareStatement(sql);

            if (ConnectionManager.isPostgreSQL(connection)) {
                sql = "SELECT message_id, log_date, error_message FROM log.message WHERE error_message is not null AND channel_id = ?";
            } else {
                sql = "SELECT message_id, log_date, error_message FROM message WHERE error_message is not null AND channel_id = ?";
            }
            psErrors = connection.prepareStatement(sql);

            //for each channel found, get more info
            for (int i=0; i<root.size(); i++) {
                ObjectNode channelNode = (ObjectNode)root.get(i);
                int channelId = channelNode.get("id").intValue();

                //get the paused status
                psPaused.setInt(1, channelId);
                rs = psPaused.executeQuery();
                if (rs.next()) {
                    String val = rs.getString(1);
                    boolean paused = Boolean.parseBoolean(val);
                    if (paused) {
                        channelNode.put("paused", paused);
                    }
                }

                //get the last message received
                psLastMessage.setInt(1, channelId);
                rs = psLastMessage.executeQuery();
                if (rs.next()) {
                    int messageId = rs.getInt(1);
                    Date logDate = new Date(rs.getTimestamp(2).getTime());

                    channelNode.put("lastMessageId", messageId);
                    channelNode.put("lastMessageReceived", logDate.getTime());
                }

                //get any errors in the ADT->FHIR transform
                psErrors.setInt(1, channelId);
                rs = psErrors.executeQuery();
                if (rs.next()) {
                    int messageId = rs.getInt(1);
                    Date logDate = new Date(rs.getTimestamp(2).getTime());
                    String messageError = rs.getString(3);

                    channelNode.put("errorMessageId", messageId);
                    channelNode.put("errorMessageReceived", logDate.getTime());
                    channelNode.put("errorMessage", messageError);
                }
            }

        } finally {
            if (psChannels != null) {
                psChannels.close();
            }
            if (psPaused != null) {
                psPaused.close();
            }
            if (psLastMessage != null) {
                psLastMessage.close();
            }
            if (psErrors != null) {
                psErrors.close();
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
