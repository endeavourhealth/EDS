package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.endeavourhealth.common.ods.OdsOrganisation;
import org.endeavourhealth.common.ods.OdsWebService;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.utility.ExpiringCache;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

@Path("/hl7v2Api")
public class Hl7v2ApiEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(Hl7v2ApiEndpoint.class);

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Library);

    private static ExpiringCache<String, String> hmSourceNames = new ExpiringCache<String, String>(ExpiringCache.Duration.OneHour);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "Hl7v2ApiEndpoint.getStatus")
    @Path("/status")
    public Response getStatus(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get HL7v2 Status");

        String ret = getStatusJson();

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private static List<String> getSourceOdsCodes() throws Exception {

        List<String> ret = new ArrayList<>();

        Connection connection = ConnectionManager.getHL7v2InboundConnection();
        PreparedStatement ps = null;
        try {
            String sql = "SELECT source_system FROM last_message";
            ps = connection.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String s = rs.getString(1);
                ret.add(s);
            }

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }

        ret.sort(((o1, o2) -> o1.toLowerCase().compareToIgnoreCase(o2.toLowerCase())));

        return ret;
    }


    private String getStatusJson() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode root = new ArrayNode(mapper.getNodeFactory());

        Connection connection = ConnectionManager.getHL7v2InboundConnection();
        PreparedStatement ps = null;
        try {

            //start by get the sending queue size for each publisher
            Map<String, Integer> hmQueueSize = new HashMap<>();
            String sql = "SELECT source_system, count(1)"
                    + " FROM acute"
                    + " WHERE send_to_mq != 'Y'" //not sure why this isn't a boolean, but it's a varchar
                    + " GROUP BY source_system";
            ps = connection.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int col = 1;
                String odsCode = rs.getString(col++);
                int count = rs.getInt(col++);
                hmQueueSize.put(odsCode, count);
            }

            ps.close();


            //and for each queue, find some details about the oldest queued thing
            Map<String, Date> hmQueueDate = new HashMap<>();
            Map<String, String> hmQueueMessageId = new HashMap<>();
            Map<String, Long> hmQueueId = new HashMap<>();
            sql = "SELECT date_received, message_id, id"
                    + " FROM acute"
                    + " WHERE send_to_mq != 'Y'"
                    + " AND source_system = ?"
                    + " ORDER BY date_received ASC"
                    + " LIMIT 1";
            ps = connection.prepareStatement(sql);

            for (String odsCode: hmQueueSize.keySet()) {

                ps.setString(1, odsCode);

                rs = ps.executeQuery();

                //this is all moving in real-time, so even though there may have been a queue a fraction of a second ago, it might
                //be gone now, so this may not return anything
                if (rs.next()) {
                    int col = 1;
                    java.util.Date dtReceived = new java.util.Date(rs.getTimestamp(col++).getTime());
                    String messageId = rs.getString(col++);
                    long id = rs.getLong(col++);

                    hmQueueDate.put(odsCode, dtReceived);
                    hmQueueMessageId.put(odsCode, messageId);
                    hmQueueId.put(odsCode, new Long(id));
                }
            }

            ps.close();

            //get the last received message for each publishing trust
            sql = "SELECT source_system, date_received, message_id, id FROM last_message ORDER BY source_system";
            ps = connection.prepareStatement(sql);

            rs = ps.executeQuery();
            while (rs.next()) {
                int col = 1;
                String odsCode = rs.getString(col++);
                java.util.Date dtReceived = new java.util.Date(rs.getTimestamp(col++).getTime());
                String messageId = rs.getString(col++);
                long id = rs.getLong(col++);

                String name = findNameForOdsCode(odsCode);

                ObjectNode child = root.addObject();
                child.put("odsCode", odsCode);
                child.put("name", name);
                child.put("lastReceivedTimestamp", dtReceived.getTime());
                child.put("lastReceivedMessageId", messageId);
                child.put("lastReceivedId", id);

                //get the previously cached queue size and add that
                Integer queueSize = hmQueueSize.get(odsCode);
                if (queueSize == null) {
                    queueSize = new Integer(0);
                }
                child.put("queueSize", queueSize.intValue());

                Date dtQueueOldest = hmQueueDate.get(odsCode);
                if (dtQueueOldest != null) {
                    child.put("queueOldestTimestamp", dtQueueOldest.getTime());
                }
                String queueMessageId = hmQueueMessageId.get(odsCode);
                if (queueMessageId != null) {
                    child.put("queueMessageId", queueMessageId);
                }
                Long queueId = hmQueueId.get(odsCode);
                if (queueId != null) {
                    child.put("queueId", queueId.longValue());
                }
            }

            ps.close();

        } finally {
            connection.close();
        }

        return mapper.writeValueAsString(root);
    }


    /**
     * uses ODS web service to find a name for the publishing ODS code
     */
    private String findNameForOdsCode(String odsCode) throws Exception {

        String ret = hmSourceNames.get(odsCode);
        if (ret == null) {
            OdsOrganisation org = OdsWebService.lookupOrganisationViaRest(odsCode);
            if (org == null) {
                ret = "Unknown Publisher";
            } else {
                ret = org.getOrganisationName();
            }
            hmSourceNames.put(odsCode, ret);
        }
        return ret;
    }
}