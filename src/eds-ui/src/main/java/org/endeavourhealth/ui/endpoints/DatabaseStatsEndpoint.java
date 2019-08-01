package org.endeavourhealth.ui.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.net.URI;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/databaseStats")
public class DatabaseStatsEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseStatsEndpoint.class);

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Library);
    private static Map<String, List<String>> hmCredentialCache = null;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/databaseServers")
    public Response getDatabaseServers(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Database Servers");

        String ret = getDatabaseServerList();

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private String getDatabaseServerList() throws Exception {

        //always reset the cache
        Map<String, List<String>> cache = new HashMap<>();

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode root = new ArrayNode(mapper.getNodeFactory());

        getDatabaseServers(root, cache, "db_publisher");
        getDatabaseServers(root, cache, "db_common");
        getDatabaseServers(root, cache, "db_subscriber");

        this.hmCredentialCache = cache;

        return mapper.writeValueAsString(root);
    }

    private void getDatabaseServers(ArrayNode root, Map<String, List<String>> cache, String appId) throws Exception {
        Map<String, JsonNode> map  = ConfigManager.getConfigurationsAsJson(appId);
        for (String configId: map.keySet()) {
            JsonNode json = map.get(configId);

            if (json.has("url")) {
                getDatabaseCredentials(root, cache, json, "", "", appId, configId);
            }

            if (json.has("enterprise_url")) {
                getDatabaseCredentials(root, cache, json, "", "enterprise_", appId, configId);
            }

            if (json.has("core")) {
                getDatabaseCredentials(root, cache, json, "core", "", appId, configId);
            }

            if (json.has("transform")) {
                getDatabaseCredentials(root, cache, json, "transform", "", appId, configId);
            }
        }
    }

    private void getDatabaseCredentials(ArrayNode root, Map<String, List<String>> cache, JsonNode json, String nodeChildName, String nodePrefix, String appId, String configId) {

        if (!Strings.isNullOrEmpty(nodeChildName)) {
            json = json.get(nodeChildName);
        }

        String url = json.get(nodePrefix + "url").asText();

        String cleanedUrl = url.substring(5); //need to remove prefix "jdbc:"
        URI uri = URI.create(cleanedUrl);
        String host = uri.getHost();
        String scheme = uri.getScheme();

        String cacheKey = scheme + "@" + host;
        if (cache.containsKey(cacheKey)) {
            return;
        }

        //cache which config record this came from, rather than the credentials themselves so nothing is kept in memory
        List<String> cacheVal = new ArrayList<>();
        cacheVal.add(appId);
        cacheVal.add(configId);
        cacheVal.add(nodeChildName);
        cacheVal.add(nodePrefix);
        cache.put(cacheKey, cacheVal);

        ObjectNode objectNode = root.addObject();
        objectNode.put("type", scheme);
        objectNode.put("host", host);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/databases/{type}/{host}")
    public Response getDatabaseSizes(@Context SecurityContext sc,
                                     @PathParam(value = "type") String type,
                                     @PathParam(value = "host") String host) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Database Sizes",
                "Type", type,
                "Host", host);

        //all the below should really be moved to core, but this is all one-off stuff (hopefully)
        //List<FrailtyStat> ret = getRecentStats(minutesBack, groupBy);
        String ret = getDatabaseSizes(type, host);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private String getDatabaseSizes(String type, String host) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode root = new ArrayNode(mapper.getNodeFactory());

        String cacheKey = type + "@" + host;
        List<String> cacheVal = hmCredentialCache.get(cacheKey);
        if (cacheVal == null) {
            throw new BadRequestException("No credentials found for " + host);
        }
        String appId = cacheVal.get(0);
        String configId = cacheVal.get(1);
        String nodeChildName = cacheVal.get(2);
        String jsonPrefix = cacheVal.get(3);

        JsonNode json = ConfigManager.getConfigurationAsJson(configId, appId);
        if (!Strings.isNullOrEmpty(nodeChildName)) {
            json = json.get(nodeChildName);
        }

        String url = json.get(jsonPrefix + "url").asText();
        String user = json.get(jsonPrefix + "username").asText();
        String pass = json.get(jsonPrefix + "password").asText();

        if (json.has("driverName")) {
            String driverName = json.get("driverName").asText();
            Class.forName(driverName);
        }

        Connection conn = null;
        PreparedStatement ps = null;
        try {

            String sql = null;

            //limit the connection timeout, since some DBs can't be accessed from this server
            if (url.contains("mysql")) {
                if (url.contains("?")) {
                    url += "&";
                } else {
                    url += "?";
                }
                url += "Connection Timeout=5";
                sql = "SELECT table_schema, SUM(data_length + index_length) FROM information_schema.tables GROUP BY table_schema";

            } else if (url.contains("postgresql")) {
                if (url.contains("?")) {
                    url += "&";
                } else {
                    url += "?";
                }
                url += "&Timeout=5";
                sql = "SELECT pg_database.datname, pg_database_size(pg_database.datname) FROM pg_database";

            } else if (url.contains("sqlserver")) {
                if (url.contains("?")) {
                    url += "&";
                } else {
                    url += "?";
                }
                url += "loginTimeout=5";
                sql = "SELECT DB_NAME(database_id), CONVERT(BIGINT, SUM((size * 8))) * 1024 FROM sys.master_files group by DB_NAME(database_id)";

            } else {
                throw new SQLException("Unknown database type for " + host);
            }

            conn = DriverManager.getConnection(url, user, pass);

            if (sql != null) {
                ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int col = 1;
                    String dbName = rs.getString(col++);
                    long dbSize = rs.getLong(col++);
                    //String dbSizeReadable = FileUtils.byteCountToDisplaySize(dbSize);

                    ObjectNode objectNode = root.addObject();
                    objectNode.put("name", dbName);
                    objectNode.put("sizeBytes", dbSize);
                    //objectNode.put("sizeDesc", dbSizeReadable);
                }
            }

        } catch (SQLException ex) {
            String err = ex.getMessage();

            ObjectNode objectNode = root.addObject();
            objectNode.put("error", err);

        } finally {
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
        }


        return mapper.writeValueAsString(root);
    }


}
