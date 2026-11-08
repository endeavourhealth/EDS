package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.io.FileUtils;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.utility.ExpiringCache;
import org.endeavourhealth.common.utility.XmlSerializer;
import org.endeavourhealth.core.configuration.ConfigDeserialiser;
import org.endeavourhealth.core.configuration.QueueReaderConfiguration;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ApplicationHeartbeatDalI;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.ApplicationHeartbeat;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.transform.common.TransformConfig;
import org.endeavourhealth.ui.json.JsonQueueReaderCommand;
import org.endeavourhealth.ui.json.JsonQueueReaderConfigRequest;
import org.endeavourhealth.ui.json.JsonMemoryDefaultsRequest;
import org.endeavourhealth.ui.json.JsonMemoryDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Path("/queueReader")
public class QueueReaderEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(QueueReaderEndpoint.class);

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Library);

    private static ExpiringCache<String, String> odsCodeToPublisherCache = new ExpiringCache<>(1000 * 60 * 5); //cache for five mins

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="QueueReaderEndpoint.instances")
    @Path("/instances")
    public Response getInstances(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Queue Reader Instances");

        String ret = getQueueReaderInstancesJson();

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private static String getQueueReaderInstancesJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode root = mapper.createArrayNode();

        ApplicationHeartbeatDalI dal = DalProvider.factoryApplicationHeartbeatDal();
        List<ApplicationHeartbeat> latest = dal.getLatest();

        for (ApplicationHeartbeat h : latest) {
            if (!h.getApplicationName().equals("queuereader")) {
                continue;
            }

            String configId = h.getApplicationInstanceName();
            if (Strings.isNullOrEmpty(configId)) {
                continue;
            }

            ObjectNode instanceNode = root.addObject();
            instanceNode.put("configId", configId);
            instanceNode.put("hostName", h.getHostName());
            instanceNode.put("applicationInstanceName", h.getApplicationInstanceName());
            instanceNode.put("applicationInstanceNumber", h.getApplicationInstanceNumber() != null ? h.getApplicationInstanceNumber() : 1);

            long now = System.currentTimeMillis();
            long lastHeartbeat = h.getTimestmp().getTime();
            boolean isRunning = (now - lastHeartbeat) < (1000 * 60 * 2);
            instanceNode.put("status", isRunning ? "running" : "stopped");
            instanceNode.put("lastHeartbeat", lastHeartbeat);

            if (h.getDtStarted() != null) {
                instanceNode.put("dtStarted", h.getDtStarted().getTime());
            }
            if (h.getDtJar() != null) {
                instanceNode.put("dtJar", h.getDtJar().getTime());
            }
            if (h.getMaxHeapMb() != null) {
                instanceNode.put("maxHeapMb", h.getMaxHeapMb());
                instanceNode.put("maxHeapDesc", FileUtils.byteCountToDisplaySize(h.getMaxHeapMb() * (1024L * 1024L)));
            }
            if (h.getCurrentHeapMb() != null) {
                instanceNode.put("currentHeapMb", h.getCurrentHeapMb());
                instanceNode.put("currentHeapDesc", FileUtils.byteCountToDisplaySize(h.getCurrentHeapMb() * (1024L * 1024L)));
            }
            if (h.getServerMemoryMb() != null) {
                instanceNode.put("physicalMemoryMb", h.getServerMemoryMb());
                instanceNode.put("physicalMemoryDesc", FileUtils.byteCountToDisplaySize(h.getServerMemoryMb() * (1024L * 1024L)));
            }
            if (h.getServerCpuUsagePercent() != null) {
                instanceNode.put("cpuLoad", h.getServerCpuUsagePercent());
            }
            instanceNode.put("isBusy", h.getBusy());

            String queueName = null;
            String queueType = null;
            String queueReaderConfigXml = ConfigManager.getConfiguration(configId, "queuereader");
            if (!Strings.isNullOrEmpty(queueReaderConfigXml)) {
                try {
                    QueueReaderConfiguration configuration = ConfigDeserialiser.deserialise(queueReaderConfigXml);
                    queueName = configuration.getQueue();
                    instanceNode.put("queueName", queueName);

                    if (queueName != null) {
                        if (queueName.startsWith("EdsInbound")) {
                            queueType = "inbound";
                        } else if (queueName.startsWith("EdsProtocol")) {
                            queueType = "protocol";
                        } else if (queueName.startsWith("EdsTransform")) {
                            queueType = "transform";
                        } else if (queueName.startsWith("EdsSubscriber")) {
                            queueType = "subscriber";
                        }
                    }
                } catch (Exception ex) {
                    LOG.warn("Failed to parse queue reader config for " + configId, ex);
                }
            }

            instanceNode.put("queueType", queueType != null ? queueType : "inbound");

            JsonMemoryDefaults memoryDefaults = getMemoryDefaultsForQueueType(queueType != null ? queueType : "inbound");
            if (memoryDefaults != null) {
                ObjectNode defaultsNode = instanceNode.putObject("memoryDefaults");
                defaultsNode.put("maxHeap", memoryDefaults.getMaxHeap());
                defaultsNode.put("unit", memoryDefaults.getUnit());
            }
        }

        return mapper.writeValueAsString(root);
    }

    private static JsonMemoryDefaults getMemoryDefaultsForQueueType(String queueType) {
        String configKey = "queuereader-memory-defaults-" + queueType;
        try {
            String xml = ConfigManager.getConfiguration(configKey, "queuereader");
            if (!Strings.isNullOrEmpty(xml)) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(xml);
                JsonMemoryDefaults defaults = new JsonMemoryDefaults();
                if (node.has("maxHeap")) {
                    defaults.setMaxHeap(node.get("maxHeap").asInt());
                }
                if (node.has("unit")) {
                    defaults.setUnit(node.get("unit").asText());
                }
                return defaults;
            }
        } catch (Exception ex) {
            LOG.warn("Failed to load memory defaults for queue type: " + queueType, ex);
        }
        return null;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="QueueReaderEndpoint.getConfig")
    @Path("/config/{configId}")
    public Response getConfig(@Context SecurityContext sc, @PathParam("configId") String configId) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Queue Reader Config", "ConfigId", configId);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();

        String configXml = ConfigManager.getConfiguration(configId, "queuereader");
        if (!Strings.isNullOrEmpty(configXml)) {
            result.put("configXml", configXml);

            try {
                QueueReaderConfiguration configuration = ConfigDeserialiser.deserialise(configXml);
                result.put("queueName", configuration.getQueue());
                result.put("exclusive", configuration.isExclusive() != null && configuration.isExclusive());

                if (configuration.getPipeline() != null) {
                    result.put("pipelineComponents", configuration.getPipeline().getPipelineComponents().size());
                }
            } catch (Exception ex) {
                LOG.warn("Failed to parse queue reader config for " + configId, ex);
            }
        } else {
            result.put("configXml", "");
        }

        String queueName = null;
        try {
            if (!Strings.isNullOrEmpty(configXml)) {
                QueueReaderConfiguration configuration = ConfigDeserialiser.deserialise(configXml);
                queueName = configuration.getQueue();
            }
        } catch (Exception ex) {
            // ignore
        }

        String queueType = determineQueueType(queueName);
        JsonMemoryDefaults memoryDefaults = getMemoryDefaultsForQueueType(queueType);
        
        ObjectNode memoryNode = result.putObject("memory");
        
        if (memoryDefaults != null) {
            memoryNode.put("defaultMaxHeap", memoryDefaults.getMaxHeap());
            memoryNode.put("defaultUnit", memoryDefaults.getUnit());
        } else {
            memoryNode.putNull("defaultMaxHeap");
            memoryNode.putNull("defaultUnit");
        }

        String overrideKey = "queuereader-memory-override-" + configId;
        String overrideXml = ConfigManager.getConfiguration(overrideKey);
        if (!Strings.isNullOrEmpty(overrideXml)) {
            try {
                JsonNode overrideNode = mapper.readTree(overrideXml);
                if (overrideNode.has("maxHeap") && !overrideNode.get("maxHeap").isNull()) {
                    memoryNode.put("maxHeap", overrideNode.get("maxHeap").asInt());
                } else {
                    memoryNode.putNull("maxHeap");
                }
                if (overrideNode.has("unit") && !overrideNode.get("unit").isNull()) {
                    memoryNode.put("unit", overrideNode.get("unit").asText());
                } else {
                    memoryNode.putNull("unit");
                }
            } catch (Exception ex) {
                LOG.warn("Failed to parse memory override for " + configId, ex);
                memoryNode.putNull("maxHeap");
                memoryNode.putNull("unit");
            }
        } else {
            memoryNode.putNull("maxHeap");
            memoryNode.putNull("unit");
        }

        clearLogbackMarkers();

        return Response.ok().entity(mapper.writeValueAsString(result)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="QueueReaderEndpoint.saveConfig")
    @Path("/config")
    public Response saveConfig(@Context SecurityContext sc, JsonQueueReaderConfigRequest request) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "Save Queue Reader Config", "ConfigId", request.getConfigId());

        String configXml = ConfigManager.getConfiguration(request.getConfigId(), "queuereader");
        
        if (Strings.isNullOrEmpty(configXml)) {
            clearLogbackMarkers();
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Configuration not found\"}")
                    .build();
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode memoryOverride = mapper.createObjectNode();
        
        if (request.getMaxHeap() != null) {
            memoryOverride.put("maxHeap", request.getMaxHeap());
        } else {
            memoryOverride.putNull("maxHeap");
        }
        
        if (request.getUnit() != null) {
            memoryOverride.put("unit", request.getUnit());
        } else {
            memoryOverride.putNull("unit");
        }

        String overrideXml = mapper.writeValueAsString(memoryOverride);
        ConfigManager.setConfiguration("queuereader-memory-override-" + request.getConfigId(), "queuereader", overrideXml);

        clearLogbackMarkers();

        ObjectNode result = mapper.createObjectNode();
        result.put("message", "Configuration saved successfully");
        return Response.ok().entity(mapper.writeValueAsString(result)).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="QueueReaderEndpoint.deleteConfig")
    @Path("/config/{configId}")
    public Response deleteConfig(@Context SecurityContext sc, @PathParam("configId") String configId) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete, "Delete Queue Reader Config", "ConfigId", configId);

        ApplicationHeartbeatDalI dal = DalProvider.factoryApplicationHeartbeatDal();
        List<ApplicationHeartbeat> latest = dal.getLatest();
        
        boolean isRunning = false;
        for (ApplicationHeartbeat h : latest) {
            if (h.getApplicationName().equals("queuereader") 
                    && configId.equals(h.getApplicationInstanceName())) {
                long now = System.currentTimeMillis();
                if ((now - h.getTimestmp().getTime()) < (1000 * 60 * 2)) {
                    isRunning = true;
                    break;
                }
            }
        }

        if (isRunning) {
            clearLogbackMarkers();
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"Cannot delete configuration for running instance. Stop the queue reader first.\"}")
                    .build();
        }

        ConfigManager.setConfiguration(configId, "queuereader", null);

        clearLogbackMarkers();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        result.put("message", "Configuration deleted successfully");
        return Response.ok().entity(mapper.writeValueAsString(result)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="QueueReaderEndpoint.stopInstance")
    @Path("/stop")
    public Response stopInstance(@Context SecurityContext sc, JsonQueueReaderCommand request) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "Stop Queue Reader", "ConfigIds", String.join(",", request.getHostNames() != null ? request.getHostNames() : new ArrayList<>()));

        Map<String, String> servers = getServersByConfigId(request.getHostNames());
        
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode results = mapper.createArrayNode();

        for (String configId : servers.keySet()) {
            ObjectNode result = results.addObject();
            result.put("configId", configId);
            
            try {
                String userAndIp = servers.get(configId);
                int ix = userAndIp.indexOf("@");
                String ip = userAndIp.substring(ix + 1);

                JSch jsch = new JSch();
                Session session = jsch.getSession(userAndIp.substring(0, ix - 1), ip, 22);
                session.connect();

                String killFilePath = TransformConfig.instance().getKillFileLocation() + "/" + configId + ".kill";
                
                ChannelExec channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand("touch " + killFilePath);
                channel.setInputStream(null);
                ((ChannelExec) channel).setErrStream(System.err);
                
                InputStream in = channel.getInputStream();
                channel.connect();
                
                byte[] tmp = new byte[1024];
                while (!channel.isClosed()) {
                    if (in.available() > 0) {
                        int i = in.read(tmp, 0, 1024);
                        if (i < 0) break;
                    }
                    if (channel.isClosed()) break;
                    Thread.sleep(500);
                }
                channel.disconnect();
                session.disconnect();

                result.put("success", true);
                result.put("message", "Stop signal sent");
            } catch (Exception ex) {
                LOG.error("Failed to stop queue reader " + configId, ex);
                result.put("success", false);
                result.put("error", ex.getMessage());
            }
        }

        clearLogbackMarkers();

        return Response.ok().entity(mapper.writeValueAsString(results)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="QueueReaderEndpoint.restartInstance")
    @Path("/restart")
    public Response restartInstance(@Context SecurityContext sc, JsonQueueReaderCommand request) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "Restart Queue Reader", "ConfigIds", String.join(",", request.getHostNames() != null ? request.getHostNames() : new ArrayList<>()));

        Map<String, String> servers = getServersByConfigId(request.getHostNames());
        
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode results = mapper.createArrayNode();

        for (String configId : servers.keySet()) {
            ObjectNode result = results.addObject();
            result.put("configId", configId);
            
            try {
                String userAndIp = servers.get(configId);
                int ix = userAndIp.indexOf("@");
                String ip = userAndIp.substring(ix + 1);

                JSch jsch = new JSch();
                Session session = jsch.getSession(userAndIp.substring(0, ix - 1), ip, 22);
                session.connect();

                String killFilePath = TransformConfig.instance().getKillFileLocation() + "/" + configId + ".restart";
                
                ChannelExec channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand("touch " + killFilePath);
                channel.setInputStream(null);
                ((ChannelExec) channel).setErrStream(System.err);
                
                InputStream in = channel.getInputStream();
                channel.connect();
                
                byte[] tmp = new byte[1024];
                while (!channel.isClosed()) {
                    if (in.available() > 0) {
                        int i = in.read(tmp, 0, 1024);
                        if (i < 0) break;
                    }
                    if (channel.isClosed()) break;
                    Thread.sleep(500);
                }
                channel.disconnect();
                session.disconnect();

                result.put("success", true);
                result.put("message", "Restart signal sent");
            } catch (Exception ex) {
                LOG.error("Failed to restart queue reader " + configId, ex);
                result.put("success", false);
                result.put("error", ex.getMessage());
            }
        }

        clearLogbackMarkers();

        return Response.ok().entity(mapper.writeValueAsString(results)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="QueueReaderEndpoint.getMemoryDefaults")
    @Path("/memoryDefaults")
    public Response getMemoryDefaults(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Queue Reader Memory Defaults");

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode results = mapper.createArrayNode();

        String[] queueTypes = {"inbound", "protocol", "transform", "subscriber"};
        
        for (String queueType : queueTypes) {
            ObjectNode node = results.addObject();
            node.put("queueType", queueType);

            JsonMemoryDefaults defaults = getMemoryDefaultsForQueueType(queueType);
            if (defaults != null) {
                node.put("maxHeap", defaults.getMaxHeap());
                node.put("unit", defaults.getUnit());
            } else {
                node.putNull("maxHeap");
                node.putNull("unit");
            }
        }

        clearLogbackMarkers();

        return Response.ok().entity(mapper.writeValueAsString(results)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="QueueReaderEndpoint.saveMemoryDefaults")
    @Path("/memoryDefaults")
    public Response saveMemoryDefaults(@Context SecurityContext sc, JsonMemoryDefaultsRequest request) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "Save Queue Reader Memory Defaults", "QueueType", request.getQueueType());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode config = mapper.createObjectNode();
        config.put("maxHeap", request.getMaxHeap());
        config.put("unit", request.getUnit());

        String xml = mapper.writeValueAsString(config);
        ConfigManager.setConfiguration("queuereader-memory-defaults-" + request.getQueueType(), "queuereader", xml);

        clearLogbackMarkers();

        ObjectNode result = mapper.createObjectNode();
        result.put("message", "Memory defaults saved successfully");
        return Response.ok().entity(mapper.writeValueAsString(result)).build();
    }

    private Map<String, String> getServersByConfigId(List<String> configIds) throws Exception {
        Set<String> targetConfigIds = new HashSet<>();
        
        ApplicationHeartbeatDalI dal = DalProvider.factoryApplicationHeartbeatDal();
        List<ApplicationHeartbeat> latest = dal.getLatest();
        
        for (ApplicationHeartbeat h : latest) {
            if (!h.getApplicationName().equals("queuereader")) {
                continue;
            }
            String configId = h.getApplicationInstanceName();
            if (configIds == null || configIds.isEmpty() || configIds.contains(configId)) {
                targetConfigIds.add(configId);
            }
        }

        Map<String, String> ret = new HashMap<>();
        JsonNode json = ConfigManager.getConfigurationAsJson("server_ips");
        
        for (int i = 0; i < json.size(); i++) {
            JsonNode child = json.get(i);
            String hostName = child.get("hostName").asText();
            
            if (targetConfigIds.contains(hostName)) {
                String userAndIp = child.get("userAndIp").asText();
                ret.put(hostName, userAndIp);
            }
        }

        return ret;
    }

    private static String determineQueueType(String queueName) {
        if (queueName == null) {
            return "inbound";
        }
        if (queueName.startsWith("EdsInbound")) {
            return "inbound";
        } else if (queueName.startsWith("EdsProtocol")) {
            return "protocol";
        } else if (queueName.startsWith("EdsTransform")) {
            return "transform";
        } else if (queueName.startsWith("EdsSubscriber")) {
            return "subscriber";
        }
        return "inbound";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="QueueReaderEndpoint.status")
    @Path("/status")
    public Response getQueueReaderStatus(@Context SecurityContext sc, @QueryParam("applicationName") String applicationName) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Queue Reader Status");

        String ret = getQueueReaderStatusJson(applicationName);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private static String getQueueReaderStatusJson(String applicationName) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode root = new ArrayNode(mapper.getNodeFactory());

        //get all the latest heartbeat records from the DB
        ApplicationHeartbeatDalI dal = DalProvider.factoryApplicationHeartbeatDal();
        List<ApplicationHeartbeat> latest = dal.getLatest();

        //for each heartbeat record, we need to work out which ones are for queue readers and what RabbitMQ
        //queue each one is servicing, so we need to check the DB for that
        for (ApplicationHeartbeat h: latest) {
            String appId = h.getApplicationName();
            String appSubId = h.getApplicationInstanceName();

            //skip any for other applications
            if (!Strings.isNullOrEmpty(applicationName)
                    && !applicationName.equalsIgnoreCase(appId)) {
                continue;
            }

            //if any records for non-queue reader apps that don't have a sub-ID, skip them
            /*if (Strings.isNullOrEmpty(appSubId)) {
                continue;
            }*/

            //if it's a queue reader, find the queue name from the XML config
            String queueName = null;
            String queueReaderConfigXml = ConfigManager.getConfiguration(appSubId, appId);
            if (!Strings.isNullOrEmpty(queueReaderConfigXml)) {
                QueueReaderConfiguration configuration = ConfigDeserialiser.deserialise(queueReaderConfigXml);
                queueName = configuration.getQueue();
            }

            String maxHeapDesc = null;
            if (h.getMaxHeapMb() != null) {
                maxHeapDesc = FileUtils.byteCountToDisplaySize(h.getMaxHeapMb() * (1024L * 1024L));
            }

            String currentHeapDesc = null;
            if (h.getCurrentHeapMb() != null) {
                currentHeapDesc = FileUtils.byteCountToDisplaySize(h.getCurrentHeapMb() * (1024L * 1024L));
            }

            String physicalMemoryDesc = null;
            if (h.getServerMemoryMb() != null) {
                physicalMemoryDesc = FileUtils.byteCountToDisplaySize(h.getServerMemoryMb() * (1024L * 1024L));
            }

            String busyDetail = h.getIsBusyDetail();

            //extract various bits of info from the busy detail String
            //string format is: <data date> data for <ODS code> since <processing start time>
            Long busySince = null;
            String busyOdsCode = null;
            String busyDataDate = null;
            String busyPublisherConfigName = null;
            if (!Strings.isNullOrEmpty(busyDetail)) {
                String sinceStr = "since";
                String dataForStr = "data for";

                int dataForIndex = busyDetail.indexOf(dataForStr);
                int sinceIndex = busyDetail.indexOf(sinceStr);

                if (dataForIndex > -1) {
                    String s = busyDetail.substring(0, dataForIndex);
                    s = s.trim();
                    if (!Strings.isNullOrEmpty(s)) {
                        busyDataDate = s;
                    }
                }

                if (dataForIndex > -1
                        && sinceIndex > -1) {
                    String s = busyDetail.substring(dataForIndex + dataForStr.length(), sinceIndex);
                    s = s.trim();
                    if (!Strings.isNullOrEmpty(s)) {
                        busyOdsCode = s;
                        busyPublisherConfigName = findPublisherConfigName(busyOdsCode);
                    }
                }

                if (sinceIndex > -1) {
                    String s = busyDetail.substring(sinceIndex + sinceStr.length());
                    s = s.trim();
                    try {
                        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(s);
                        busySince = new Long(d.getTime());

                    } catch (Exception ex) {
                        //if not a valid date, just leave it
                    }
                }
            }

            Long dtStartedTime = null;
            if (h.getDtStarted() != null) {
                dtStartedTime = h.getDtStarted().getTime();
            }

            Long dtJarTime = null;
            if (h.getDtJar() != null) {
                dtJarTime = h.getDtJar().getTime();
            }

            ObjectNode objectNode = root.addObject();
            objectNode.put("applicationName", h.getApplicationName());
            objectNode.put("applicationInstanceName", h.getApplicationInstanceName());
            objectNode.put("applicationInstanceNumber", h.getApplicationInstanceNumber());
            objectNode.put("timestmp", h.getTimestmp().getTime());
            objectNode.put("hostName", h.getHostName());
            objectNode.put("isBusy", h.getBusy());
            objectNode.put("maxHeapMb", h.getMaxHeapMb());
            objectNode.put("maxHeapDesc", maxHeapDesc);
            objectNode.put("currentHeapMb", h.getCurrentHeapMb());
            objectNode.put("currentHeapDesc", currentHeapDesc);
            objectNode.put("physicalMemoryMb", h.getServerMemoryMb());
            objectNode.put("physicalMemoryDesc", physicalMemoryDesc);
            objectNode.put("cpuLoad", h.getServerCpuUsagePercent());
            objectNode.put("isBusyDetail", busyDetail);
            objectNode.put("isBusySince", busySince);
            objectNode.put("isBusyOdsCode", busyOdsCode);
            objectNode.put("isBusyDataDate", busyDataDate);
            objectNode.put("isBusyPublisherConfigName", busyPublisherConfigName);
            objectNode.put("dtStarted", dtStartedTime);
            objectNode.put("dtJar", dtJarTime);
            objectNode.put("queueName", queueName);
        }

        return mapper.writeValueAsString(root);
    }

    /**
     * looks up the publisher config name (e.g. publisher_01) for the given ODS code
     */
    private static String findPublisherConfigName(String odsCode) throws Exception {
        String publisherConfigName = odsCodeToPublisherCache.get(odsCode);
        if (publisherConfigName == null) {
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            Service service = serviceDal.getByLocalIdentifier(odsCode);
            publisherConfigName = service.getPublisherConfigName();
            odsCodeToPublisherCache.put(odsCode, publisherConfigName);
        }
        return publisherConfigName;
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="QueueReaderEndpoint.QueueReaderCommand")
    @Path("/queueReaderCommand")
    public Response queueReaderCommand(@Context SecurityContext sc, JsonQueueReaderCommand command) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Queue Reader Command", "Command", command.getCommand());

        validateCommand(command);
        //TODO - validate command


        Map<String, String> hmServers = getIpAddresses(command);
        for (String hostName: hmServers.keySet()) {
            String userAndIp = hmServers.get(hostName);
            executeCommand(hostName, userAndIp, command.getCommand());
        }

        //TODO - generate response

        //String ret = getQueueReaderStatusJson(applicationName);
        String ret = "Deploy all";

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private void validateCommand(JsonQueueReaderCommand command) {

        if (command.getCommand().equals("sudo ./queueReader/deploy.sh")
                || command.getCommand().equals("sudo ./queueReader/deployScripts.sh")) {
            //valid
            return;
        }

        throw new RuntimeException("Invalid command: " + command.getCommand());
    }

    /**
     * executes the specified SSH command on the Queue Reader server
     */
    private void executeCommand(String hostName, String userAndIp, String command) {
        try {
            JSch jsch = new JSch();

            int ix = userAndIp.indexOf("@");
            String userName = userAndIp.substring(0, ix-1);
            String ip = userAndIp.substring(ix+1);

            Session session = jsch.getSession(userName, ip, 22);

            /*UserInfo ui=new MyUserInfo();
            session.setUserInfo(ui);
            session.connect();*/

            session.connect();

            ChannelExec channel = (ChannelExec)session.openChannel("exec");
            channel.setCommand(command);

            // X Forwarding
            // channel.setXForwarding(true);

            //channel.setInputStream(System.in);
            channel.setInputStream(null);

            //channel.setOutputStream(System.out);

            //FileOutputStream fos=new FileOutputStream("/tmp/stderr");
            //((ChannelExec)channel).setErrStream(fos);
            ((ChannelExec)channel).setErrStream(System.err);

            InputStream in = channel.getInputStream();

            channel.connect();

            byte[] tmp=new byte[1024];
            while(true){
                while(in.available()>0){
                    int i=in.read(tmp, 0, 1024);
                    if(i<0)break;
                    LOG.debug(new String(tmp, 0, i));
                }
                if (channel.isClosed()){
                    if (in.available()>0) {
                        continue;
                    }
                    LOG.debug("exit-status: "+channel.getExitStatus());
                    break;
                }

                Thread.sleep(1000);
            }
            channel.disconnect();
            session.disconnect();
            LOG.debug("Completed command on " + hostName);

        } catch (Exception ex) {
            LOG.error("Error with command on " + hostName + " (" + userAndIp + ")", ex);
        }

    }

    /**
     * finds the IP addresses for the servers listed in the command (or all QRs if not specified)
     */
    private Map<String, String> getIpAddresses(JsonQueueReaderCommand command) throws Exception {

        Set<String> hostNames = new HashSet<>();
        ApplicationHeartbeatDalI dal = DalProvider.factoryApplicationHeartbeatDal();
        List<ApplicationHeartbeat> latest = dal.getLatest();
        for (ApplicationHeartbeat h: latest) {

            //only count servers running Queue Reader apps
            if (!h.getApplicationName().equals("queuereader")) {
                continue;
            }

            //only count servers in the request, if specified
            String hostName = h.getHostName();
            if (command.getHostNames() != null
                    && !command.getHostNames().isEmpty()
                    && !command.getHostNames().contains(hostName)) {
                continue;
            }

            hostNames.add(hostName);
        }

        Map<String, String> ret = new HashMap<>();

        JsonNode json = ConfigManager.getConfigurationAsJson("server_ips");
        for (int i=0; i<json.size(); i++) {
            JsonNode child = json.get(i);
            String hostName = child.get("hostName").asText();
            if (hostNames.contains(hostName)) {
                String userAndIp = child.get("userAndIp").asText();
                ret.put(hostName, userAndIp);
            }
        }

        return ret;
    }


}
