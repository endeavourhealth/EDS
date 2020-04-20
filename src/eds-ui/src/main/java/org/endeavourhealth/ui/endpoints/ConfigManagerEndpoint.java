package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigHistory;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/configManager")
public class ConfigManagerEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigManagerEndpoint.class);

    private static final UserAuditDalI userAuditRepository = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Service);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "ConfigManager.getRecords")
    @Path("/records")
    public Response getRecords(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        //all the below should really be moved to core, but this is all one-off stuff (hopefully)
        String ret = getConfigRecords();

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }


    public String getConfigRecords() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode root = new ArrayNode(mapper.getNodeFactory());

        Set<String> appIds = ConfigManager.getAppIds();
        for (String appId: appIds) {
            Map<String, String> map = ConfigManager.getConfigurations(appId);

            for (String configId: map.keySet()) {
                String configData = map.get(configId);

                ObjectNode node = root.addObject();
                node.put("appId", appId);
                node.put("configId", configId);
                node.put("configData", configData);

            }

        }


        return mapper.writeValueAsString(root);
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "ConfigManager.getHistory")
    @Path("/history")
    public Response getHistory(@Context SecurityContext sc, @QueryParam("appId") String appId, @QueryParam("configId") String configId) throws Exception {
        super.setLogbackMarkers(sc);

        //all the below should really be moved to core, but this is all one-off stuff (hopefully)
        String ret = getConfigHistory(appId, configId);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }


    public String getConfigHistory(String appId, String configId) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode root = new ArrayNode(mapper.getNodeFactory());

        List<ConfigHistory> history = ConfigManager.getConfigurationHistory(configId, appId);
        for (ConfigHistory item: history) {

            ObjectNode node = root.addObject();
            node.put("appId", appId);
            node.put("configId", configId);
            node.put("dtChanged", item.getDtChanged().getTime());
            node.put("changedFrom", item.getChangedFrom());
            node.put("changedTo", item.getChangedTo());
        }

        return mapper.writeValueAsString(root);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="ConfigManager.saveRecord")
    @Path("/saveRecord")
    @RequiresAdmin
    public Response saveRecord(@Context SecurityContext sc, String json) throws Exception {
        super.setLogbackMarkers(sc);

        userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Config",
                "Json", json);

        JsonNode jsonNode = ObjectMapperPool.getInstance().readTree(json);
        String appId = jsonNode.get("appId").asText();
        String configId = jsonNode.get("configId").asText();
        String configData = jsonNode.get("configData").asText();
        ConfigManager.setConfiguration(configId, appId, configData); //note the first two parameters are in a different order to usual

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="ConfigManager.deleteRecord")
    @Path("/deleteRecord")
    @RequiresAdmin
    public Response deleteRecord(@Context SecurityContext sc, String json) throws Exception {
        super.setLogbackMarkers(sc);

        userAuditRepository.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete,
                "Config",
                "Json", json);

        JsonNode jsonNode = ObjectMapperPool.getInstance().readTree(json);
        String appId = jsonNode.get("appId").asText();
        String configId = jsonNode.get("configId").asText();
        ConfigManager.deleteConfiguration(configId, appId); //note the first two parameters are in a different order to usual

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }


}

