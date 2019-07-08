package org.endeavourhealth.ui.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.astefanutti.metrics.aspectj.Metrics;
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

@Path("/frailtyApi")
@Metrics(registry = "FrailtyApi")
public class FrailtyApiEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(FrailtyApiEndpoint.class);

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Library);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/channelStatus")
    public Response getChannelStatus(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get HL7 Receiver Status");

        //all the below should really be moved to core, but this is all one-off stuff (hopefully)
        String ret = null;

        EntityManager entityManager = ConnectionManager.getHl7ReceiverEntityManager();
        PreparedStatement ps = null;
        try {
            SessionImpl session = (SessionImpl) entityManager.getDelegate();
            Connection connection = session.connection();

            String sql = null;
            if (ConnectionManager.isPostgreSQL(connection)) {
                sql = "SELECT channel_id, channel_name FROM configuration.channel";
            } else {
                sql = "SELECT channel_id, channel_name FROM configuration_channel";
            }

            ps = connection.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = new ObjectNode(mapper.getNodeFactory());
            ArrayNode arr = root.putArray("channels");

            while (rs.next()) {
                int col = 1;
                int id = rs.getInt(col++);
                String name = rs.getString(col++);

                ObjectNode child = arr.addObject();
                child.put("id", id);
                child.put("name", name);
            }

            ret = mapper.writeValueAsString(root);

        } finally {
            if (ps != null) {
                ps.close();
            }
            entityManager.close();
        }


        //String ret = "{\"status\":\"OK\"}";

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }
}