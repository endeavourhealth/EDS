package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.core.database.dal.logback.LogbackDalI;
import org.endeavourhealth.core.database.dal.logback.models.LoggingEvent;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/logging")
public final class LoggingEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingEndpoint.class);
    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Monitoring);
    private static final LogbackDalI logbackDal = DalProvider.factoryLogbackDal();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="LoggingEndpoint.GetLoggingEvents")
    @Path("/getLoggingEvents")
    public Response getLoggingEvents(
        @Context SecurityContext sc,
        @QueryParam("page") Integer page,
        @QueryParam("serviceId") String serviceId,
        @QueryParam("level") String level) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Logging Events",
            "Page", page.toString(),
            "Service", serviceId,
            "Level",level);

        List events;
        try {
            events = logbackDal.getLoggingEvents(page, serviceId, level);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("multi-instance DB Logback")) {
                LOG.warn("Multi-instance logback DB detected, falling back to direct JDBC query");
                events = getLoggingEventsDirect(page, serviceId, level);
            } else {
                throw e;
            }
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(events)
                .build();
    }

    private List<Object> getLoggingEventsDirect(Integer page, String serviceId, String level) throws Exception {
        String configName = ConnectionManager.Db.Logback.getConfigNameIncludingInstance("DDS");
        Object config = ConfigManager.getConfiguration(configName);
        if (config == null) {
            throw new NotFoundException("Logback DB config not found: " + configName);
        }

        String url = null;
        String user = null;
        String password = null;

        if (config instanceof String) {
            url = (String) config;
        } else if (config instanceof com.google.gson.JsonObject) {
            com.google.gson.JsonObject json = (com.google.gson.JsonObject) config;
            if (json.has("url")) url = json.get("url").getAsString();
            if (json.has("user")) user = json.get("user").getAsString();
            if (json.has("password")) password = json.get("password").getAsString();
        }

        if (url == null) {
            throw new NotFoundException("No URL found in logback DB config: " + configName);
        }

        if (user == null) user = "sa";
        if (password == null) password = "";

        int offset = (page - 1) * 20;
        int limit = 20;

        String searchClause = "";
        List<String> params = new ArrayList<>();

        if (level != null && !level.isEmpty()) {
            searchClause += " AND level = ?";
            params.add(level);
        }
        if (serviceId != null && !serviceId.isEmpty()) {
            searchClause += " AND (service_name LIKE ? OR service_id LIKE ?)";
            params.add("%" + serviceId + "%");
            params.add("%" + serviceId + "%");
        }

        List<Object> events = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String countSql = "SELECT COUNT(1) FROM logback_event" + searchClause;
            try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setString(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int total = rs.getInt(1);
                        if (total == 0) {
                            return events;
                        }
                    }
                }
            }

            String sql = "SELECT id, timestamp, level, service_name, service_id, message, stack_trace_available FROM logback_event"
                    + searchClause
                    + " ORDER BY timestamp DESC LIMIT ? OFFSET ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int paramIdx = 1;
                for (String param : params) {
                    ps.setString(paramIdx++, param);
                }
                ps.setInt(paramIdx++, limit);
                ps.setInt(paramIdx++, offset);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> event = new HashMap<>();
                        event.put("id", rs.getLong("id"));
                        Timestamp ts = rs.getTimestamp("timestamp");
                        event.put("timestamp", ts != null ? ts.getTime() : 0L);
                        event.put("level", rs.getString("level"));
                        event.put("service", rs.getString("service_name"));
                        event.put("message", rs.getString("message"));
                        event.put("stackTraceAvailable", rs.getBoolean("stack_trace_available"));
                        events.add(event);
                    }
                }
            }
        }

        return events;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="LoggingEndpoint.GetStackTrace")
    @Path("/getStackTrace")
    public Response getStackTrace(@Context SecurityContext sc, @QueryParam("eventId") Long eventId) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Stack Trace",
            "EventId", eventId);

        String stackTrace = logbackDal.getStackTrace(eventId);

        clearLogbackMarkers();

        return Response
            .ok()
            .entity(stackTrace)
            .build();
    }

}
