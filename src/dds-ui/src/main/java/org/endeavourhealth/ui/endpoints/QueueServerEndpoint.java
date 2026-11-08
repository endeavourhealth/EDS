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
import org.endeavourhealth.transform.common.TransformConfig;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.ui.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.*;

@Path("/queueServer")
public class QueueServerEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(QueueServerEndpoint.class);
    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Library);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "QueueServerEndpoint.servers")
    @Path("/servers")
    public Response getServers(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Queue Server List");

        ArrayNode result = loadServerList();
        clearLogbackMarkers();
        return Response.ok().entity(result).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "QueueServerEndpoint.ec2Status")
    @Path("/ec2Status")
    public Response getEc2Status(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get EC2 Server Status");

        ArrayNode result = queryEc2Instances();
        clearLogbackMarkers();
        return Response.ok().entity(result).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "QueueServerEndpoint.jarVersions")
    @Path("/jarVersions")
    public Response getJarVersions(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get JAR Versions");

        ArrayNode result = getJarVersionInfo();
        clearLogbackMarkers();
        return Response.ok().entity(result).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "QueueServerEndpoint.startQueue")
    @Path("/startQueue")
    public Response startQueue(@Context SecurityContext sc, JsonQueueAdminAction request) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "Start Queue", "Server", request.getServerNumber(), "Queue", request.getQueueName());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        try {
            String serverInfo = getServerInfo(request.getServerNumber());
            if (serverInfo == null) {
                result.put("success", false);
                result.put("error", "Server not found: " + request.getServerNumber());
                clearLogbackMarkers();
                return Response.status(Response.Status.NOT_FOUND).entity(result).build();
            }

            String userAndIp = parseUserAndIp(serverInfo);
            String memory = Strings.isNullOrEmpty(request.getMemory()) ? getMemoryForQueueFromConfig(request.getQueueName()) : request.getMemory();
            String command = "sudo /home/ubuntu/queueReader/runNohup.sh \"" + request.getQueueName() + " " + memory + "\"";

            executeSshCommand(userAndIp, command, request.getServerHostName());
            result.put("success", true);
            result.put("message", "Start command sent for " + request.getQueueName() + " on QR" + request.getServerNumber());
        } catch (Exception ex) {
            LOG.error("Failed to start queue " + request.getQueueName() + " on QR" + request.getServerNumber(), ex);
            result.put("success", false);
            result.put("error", ex.getMessage());
        }

        clearLogbackMarkers();
        return Response.ok().entity(result).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "QueueServerEndpoint.startQueuesOnMultipleServers")
    @Path("/startQueuesOnMultipleServers")
    public Response startQueuesOnMultipleServers(@Context SecurityContext sc, JsonQueueAdminActionMulti request) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "Start Queue On Multiple Servers", "Queue", request.getQueueName());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        ArrayNode serverResults = mapper.createArrayNode();
        int successCount = 0;
        int failCount = 0;

        try {
            String memory = Strings.isNullOrEmpty(request.getMemory()) ? getMemoryForQueueFromConfig(request.getQueueName()) : request.getMemory();

            for (String serverNumber : request.getServerNumbers()) {
                ObjectNode serverResult = mapper.createObjectNode();
                serverResult.put("serverNumber", serverNumber);

                try {
                    String serverInfo = getServerInfo(serverNumber);
                    if (serverInfo == null) {
                        serverResult.put("success", false);
                        serverResult.put("error", "Server not found: " + serverNumber);
                        failCount++;
                    } else {
                        String userAndIp = parseUserAndIp(serverInfo);
                        String command = "sudo /home/ubuntu/queueReader/runNohup.sh \"" + request.getQueueName() + " " + memory + "\"";
                        executeSshCommand(userAndIp, command, serverInfo.split("\\|")[2]);
                        serverResult.put("success", true);
                        serverResult.put("message", "Start command sent for " + request.getQueueName() + " on QR" + serverNumber);
                        successCount++;
                    }
                } catch (Exception ex) {
                    LOG.error("Failed to start queue " + request.getQueueName() + " on QR" + serverNumber, ex);
                    serverResult.put("success", false);
                    serverResult.put("error", ex.getMessage());
                    failCount++;
                }

                serverResults.add(serverResult);
            }
        } catch (Exception ex) {
            LOG.error("Failed to start queue " + request.getQueueName() + " on multiple servers", ex);
            result.put("success", false);
            result.put("error", ex.getMessage());
            clearLogbackMarkers();
            return Response.ok().entity(result).build();
        }

        result.put("success", true);
        result.put("message", "Started " + request.getQueueName() + " on " + successCount + "/" + request.getServerNumbers().length + " server(s)");
        result.set("serverResults", serverResults);
        result.put("successCount", successCount);
        result.put("failCount", failCount);

        clearLogbackMarkers();
        return Response.ok().entity(result).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "QueueServerEndpoint.stopQueue")
    @Path("/stopQueue")
    public Response stopQueue(@Context SecurityContext sc, JsonQueueAdminAction request) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "Stop Queue", "Server", request.getServerNumber(), "Queue", request.getQueueName());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        try {
            String serverInfo = getServerInfo(request.getServerNumber());
            if (serverInfo == null) {
                result.put("success", false);
                result.put("error", "Server not found: " + request.getServerNumber());
                clearLogbackMarkers();
                return Response.status(Response.Status.NOT_FOUND).entity(result).build();
            }

            String userAndIp = parseUserAndIp(serverInfo);
            String command = "sudo /home/ubuntu/queueReader/kill.sh \"" + request.getQueueName() + "\"";

            executeSshCommand(userAndIp, command, request.getServerHostName());
            result.put("success", true);
            result.put("message", "Stop command sent for " + request.getQueueName() + " on QR" + request.getServerNumber());
        } catch (Exception ex) {
            LOG.error("Failed to stop queue " + request.getQueueName() + " on QR" + request.getServerNumber(), ex);
            result.put("success", false);
            result.put("error", ex.getMessage());
        }

        clearLogbackMarkers();
        return Response.ok().entity(result).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "QueueServerEndpoint.stopAllQueues")
    @Path("/stopAllQueues")
    public Response stopAllQueues(@Context SecurityContext sc, JsonQueueAdminAction request) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "Stop All Queues", "Server", request.getServerNumber());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        try {
            String serverInfo = getServerInfo(request.getServerNumber());
            if (serverInfo == null) {
                result.put("success", false);
                result.put("error", "Server not found: " + request.getServerNumber());
                clearLogbackMarkers();
                return Response.status(Response.Status.NOT_FOUND).entity(result).build();
            }

            String userAndIp = parseUserAndIp(serverInfo);
            String command = "sudo /home/ubuntu/queueReader/killAll.sh";

            executeSshCommand(userAndIp, command, request.getServerHostName());
            result.put("success", true);
            result.put("message", "Stop all queues command sent to QR" + request.getServerNumber());
        } catch (Exception ex) {
            LOG.error("Failed to stop all queues on QR" + request.getServerNumber(), ex);
            result.put("success", false);
            result.put("error", ex.getMessage());
        }

        clearLogbackMarkers();
        return Response.ok().entity(result).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "QueueServerEndpoint.restartQueue")
    @Path("/restartQueue")
    public Response restartQueue(@Context SecurityContext sc, JsonQueueAdminAction request) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "Restart Queue", "Server", request.getServerNumber(), "Queue", request.getQueueName());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        try {
            String serverInfo = getServerInfo(request.getServerNumber());
            if (serverInfo == null) {
                result.put("success", false);
                result.put("error", "Server not found: " + request.getServerNumber());
                clearLogbackMarkers();
                return Response.status(Response.Status.NOT_FOUND).entity(result).build();
            }

            String userAndIp = parseUserAndIp(serverInfo);
            String command = "sudo /home/ubuntu/queueReader/restart.sh \"" + request.getQueueName() + " quiet nolog\"";

            executeSshCommand(userAndIp, command, request.getServerHostName());
            result.put("success", true);
            result.put("message", "Restart command sent for " + request.getQueueName() + " on QR" + request.getServerNumber());
        } catch (Exception ex) {
            LOG.error("Failed to restart queue " + request.getQueueName() + " on QR" + request.getServerNumber(), ex);
            result.put("success", false);
            result.put("error", ex.getMessage());
        }

        clearLogbackMarkers();
        return Response.ok().entity(result).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "QueueServerEndpoint.restartAllQueues")
    @Path("/restartAllQueues")
    public Response restartAllQueues(@Context SecurityContext sc, JsonQueueAdminAction request) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "Restart All Queues", "Server", request.getServerNumber());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        try {
            String serverInfo = getServerInfo(request.getServerNumber());
            if (serverInfo == null) {
                result.put("success", false);
                result.put("error", "Server not found: " + request.getServerNumber());
                clearLogbackMarkers();
                return Response.status(Response.Status.NOT_FOUND).entity(result).build();
            }

            String userAndIp = parseUserAndIp(serverInfo);
            String command = "sudo /home/ubuntu/queueReader/restartAll.sh";

            executeSshCommand(userAndIp, command, request.getServerHostName());
            result.put("success", true);
            result.put("message", "Restart all queues command sent to QR" + request.getServerNumber());
        } catch (Exception ex) {
            LOG.error("Failed to restart all queues on QR" + request.getServerNumber(), ex);
            result.put("success", false);
            result.put("error", ex.getMessage());
        }

        clearLogbackMarkers();
        return Response.ok().entity(result).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "QueueServerEndpoint.shutdownServer")
    @Path("/shutdownServer")
    public Response shutdownServer(@Context SecurityContext sc, JsonServerPowerAction request) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "Shutdown Server", "Server", request.getServerNumber());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        try {
            String serverInfo = getServerInfo(request.getServerNumber());
            if (serverInfo == null) {
                result.put("success", false);
                result.put("error", "Server not found: " + request.getServerNumber());
                clearLogbackMarkers();
                return Response.status(Response.Status.NOT_FOUND).entity(result).build();
            }

            String userAndIp = parseUserAndIp(serverInfo);
            String command = "sudo shutdown -h 1";

            executeSshCommand(userAndIp, command, request.getServerHostName());
            result.put("success", true);
            result.put("message", "Shutdown command sent to QR" + request.getServerNumber());
        } catch (Exception ex) {
            LOG.error("Failed to shutdown QR" + request.getServerNumber(), ex);
            result.put("success", false);
            result.put("error", ex.getMessage());
        }

        clearLogbackMarkers();
        return Response.ok().entity(result).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "QueueServerEndpoint.rebootServer")
    @Path("/rebootServer")
    public Response rebootServer(@Context SecurityContext sc, JsonServerPowerAction request) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "Reboot Server", "Server", request.getServerNumber());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        try {
            String serverInfo = getServerInfo(request.getServerNumber());
            if (serverInfo == null) {
                result.put("success", false);
                result.put("error", "Server not found: " + request.getServerNumber());
                clearLogbackMarkers();
                return Response.status(Response.Status.NOT_FOUND).entity(result).build();
            }

            String userAndIp = parseUserAndIp(serverInfo);
            String command = "sudo reboot";

            executeSshCommand(userAndIp, command, request.getServerHostName());
            result.put("success", true);
            result.put("message", "Reboot command sent to QR" + request.getServerNumber());
        } catch (Exception ex) {
            LOG.error("Failed to reboot QR" + request.getServerNumber(), ex);
            result.put("success", false);
            result.put("error", ex.getMessage());
        }

        clearLogbackMarkers();
        return Response.ok().entity(result).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "QueueServerEndpoint.ec2Start")
    @Path("/ec2Start")
    public Response ec2Start(@Context SecurityContext sc, JsonServerPowerAction request) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "EC2 Start Server", "Server", request.getServerNumber());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        try {
            String awsInstanceId = getAwsInstanceId(request.getServerNumber());
            if (Strings.isNullOrEmpty(awsInstanceId)) {
                result.put("success", false);
                result.put("error", "AWS Instance ID not found for QR" + request.getServerNumber());
                clearLogbackMarkers();
                return Response.status(Response.Status.NOT_FOUND).entity(result).build();
            }

            executeEc2Start(awsInstanceId);
            result.put("success", true);
            result.put("message", "EC2 start command sent for QR" + request.getServerNumber() + " (Instance: " + awsInstanceId + ")");
        } catch (Exception ex) {
            LOG.error("Failed to EC2 start QR" + request.getServerNumber(), ex);
            result.put("success", false);
            result.put("error", ex.getMessage());
        }

        clearLogbackMarkers();
        return Response.ok().entity(result).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "QueueServerEndpoint.ec2Stop")
    @Path("/ec2Stop")
    public Response ec2Stop(@Context SecurityContext sc, JsonServerPowerAction request) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "EC2 Stop Server", "Server", request.getServerNumber());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        try {
            String awsInstanceId = getAwsInstanceId(request.getServerNumber());
            if (Strings.isNullOrEmpty(awsInstanceId)) {
                result.put("success", false);
                result.put("error", "AWS Instance ID not found for QR" + request.getServerNumber());
                clearLogbackMarkers();
                return Response.status(Response.Status.NOT_FOUND).entity(result).build();
            }

            executeEc2Stop(awsInstanceId);
            result.put("success", true);
            result.put("message", "EC2 stop command sent for QR" + request.getServerNumber() + " (Instance: " + awsInstanceId + ")");
        } catch (Exception ex) {
            LOG.error("Failed to EC2 stop QR" + request.getServerNumber(), ex);
            result.put("success", false);
            result.put("error", ex.getMessage());
        }

        clearLogbackMarkers();
        return Response.ok().entity(result).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "QueueServerEndpoint.saveServer")
    @Path("/saveServer")
    public Response saveServer(@Context SecurityContext sc, JsonQueueServer request) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "Save Queue Server", "ServerNumber", request.getServerNumber());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();

        try {
            JsonNode parsedConfig = parseServerIpJson(mapper);
            ArrayNode servers = mapper.createArrayNode();

            if (parsedConfig != null && !parsedConfig.isNull() && parsedConfig.isArray()) {
                for (int i = 0; i < parsedConfig.size(); i++) {
                    servers.add(parsedConfig.get(i));
                }
            }

            boolean found = false;
            String serverNum = request.getServerNumber();
            for (int i = 0; i < servers.size(); i++) {
                JsonNode node = servers.get(i);
                String existingNum = node.get("hostName").asText();
                existingNum = existingNum.startsWith("QR") ? existingNum.substring(2) : existingNum;
                if (existingNum.equals(serverNum)) {
                    ObjectNode updatedNode = mapper.createObjectNode();
                    updatedNode.put("serverNumber", serverNum);
                    updatedNode.put("hostName", "QR" + serverNum);
                    updatedNode.put("ipAddress", request.getIpAddress());
                    updatedNode.put("userAndIp", "ubuntu@" + request.getIpAddress());
                    updatedNode.put("queues", request.getQueues() != null ? request.getQueues() : "");
                    updatedNode.put("awsInstanceId", request.getAwsInstanceId() != null ? request.getAwsInstanceId() : "");
                    servers.set(i, updatedNode);
                    found = true;
                    break;
                }
            }

            if (!found) {
                ObjectNode newNode = mapper.createObjectNode();
                newNode.put("serverNumber", serverNum);
                newNode.put("hostName", "QR" + serverNum);
                newNode.put("ipAddress", request.getIpAddress());
                newNode.put("userAndIp", "ubuntu@" + request.getIpAddress());
                newNode.put("queues", request.getQueues() != null ? request.getQueues() : "");
                newNode.put("awsInstanceId", request.getAwsInstanceId() != null ? request.getAwsInstanceId() : "");
                servers.add(newNode);
            }

            String configXml = mapper.writeValueAsString(servers);
            ConfigManager.setConfiguration("server_ips", "queuereader", configXml);

            result.put("success", true);
            result.put("message", "Server QR" + serverNum + " saved successfully");
        } catch (Exception ex) {
            LOG.error("Failed to save server", ex);
            result.put("success", false);
            result.put("error", ex.getMessage());
        }

        clearLogbackMarkers();
        return Response.ok().entity(result).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "QueueServerEndpoint.deleteServer")
    @Path("/deleteServer/{serverNumber}")
    public Response deleteServer(@Context SecurityContext sc, @PathParam("serverNumber") String serverNumber) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Delete, "Delete Queue Server", "ServerNumber", serverNumber);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();

        try {
            JsonNode parsedConfig = parseServerIpJson(mapper);
            if (parsedConfig == null || parsedConfig.isNull() || !parsedConfig.isArray()) {
                result.put("success", false);
                result.put("error", "No server configuration found");
                clearLogbackMarkers();
                return Response.ok().entity(result).build();
            }

            ArrayNode servers = mapper.createArrayNode();
            String targetNum = serverNumber.startsWith("QR") ? serverNumber.substring(2) : serverNumber;
            boolean found = false;

            for (int i = 0; i < parsedConfig.size(); i++) {
                JsonNode node = parsedConfig.get(i);
                String existingNum = node.get("hostName").asText();
                existingNum = existingNum.startsWith("QR") ? existingNum.substring(2) : existingNum;
                if (existingNum.equals(targetNum)) {
                    found = true;
                } else {
                    servers.add(node);
                }
            }

            if (!found) {
                result.put("success", false);
                result.put("error", "Server QR" + targetNum + " not found");
                clearLogbackMarkers();
                return Response.ok().entity(result).build();
            }

            String configXml = mapper.writeValueAsString(servers);
            ConfigManager.setConfiguration("server_ips", "queuereader", configXml);

            result.put("success", true);
            result.put("message", "Server QR" + targetNum + " deleted successfully");
        } catch (Exception ex) {
            LOG.error("Failed to delete server", ex);
            result.put("success", false);
            result.put("error", ex.getMessage());
        }

        clearLogbackMarkers();
        return Response.ok().entity(result).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "QueueServerEndpoint.uploadJar")
    @Path("/uploadJar")
    public Response uploadJar(@Context SecurityContext sc, JsonFileUpload request) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "Upload JAR", "Server", request.getServerNumber());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        try {
            String serverInfo = getServerInfo(request.getServerNumber());
            if (serverInfo == null) {
                result.put("success", false);
                result.put("error", "Server not found: " + request.getServerNumber());
                clearLogbackMarkers();
                return Response.status(Response.Status.NOT_FOUND).entity(result).build();
            }

            String userAndIp = parseUserAndIp(serverInfo);
            String fileName = Strings.isNullOrEmpty(request.getFile()) ? "queueReader.jar" : request.getFile();
            String fileData = request.getFileData();

            if (Strings.isNullOrEmpty(fileData)) {
                result.put("success", false);
                result.put("error", "No file data provided");
                clearLogbackMarkers();
                return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
            }

            String command = "echo '" + fileData + "' | base64 -d > /home/ubuntu/queueReader/" + fileName + " && chmod 644 /home/ubuntu/queueReader/" + fileName;
            executeSshCommand(userAndIp, command, request.getServerHostName());

            userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "JAR uploaded to server", "Server", request.getServerNumber(), "File", fileName);

            result.put("success", true);
            result.put("message", "JAR file '" + fileName + "' uploaded successfully to QR" + request.getServerNumber());
        } catch (Exception ex) {
            LOG.error("Failed to upload JAR to QR" + request.getServerNumber(), ex);
            result.put("success", false);
            result.put("error", ex.getMessage());
        }

        clearLogbackMarkers();
        return Response.ok().entity(result).build();
    }

    private ArrayNode loadServerList() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode result = mapper.createArrayNode();

        JsonNode serverIpJson = parseServerIpJson(mapper);
        if (serverIpJson == null || serverIpJson.isNull() || !serverIpJson.isArray()) {
            return result;
        }

        for (int i = 0; i < serverIpJson.size(); i++) {
            JsonNode node = serverIpJson.get(i);
            String hostName = node.get("hostName").asText();

            ObjectNode serverNode = result.addObject();
            serverNode.put("serverNumber", hostName.startsWith("QR") ? hostName.substring(2) : hostName);
            serverNode.put("hostName", hostName);
            serverNode.put("ipAddress", node.get("ipAddress").asText());
            serverNode.put("userAndIp", node.get("userAndIp").asText());

            JsonNode queuesNode = node.get("queues");
            if (queuesNode != null && !queuesNode.isNull()) {
                serverNode.put("queues", queuesNode.asText());
            }
        }

        return result;
    }

    private ArrayNode queryEc2Instances() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode result = mapper.createArrayNode();

        JsonNode serverIpJson = parseServerIpJson(mapper);
        if (serverIpJson == null || serverIpJson.isNull() || !serverIpJson.isArray()) {
            return result;
        }

        for (int i = 0; i < serverIpJson.size(); i++) {
            JsonNode node = serverIpJson.get(i);
            String hostName = node.get("hostName").asText();
            String serverNum = hostName.startsWith("QR") ? hostName.substring(2) : hostName;
            String paddedNum = (serverNum.length() < 2) ? "0" + serverNum : serverNum;
            String ec2FilterName = "CR-QR-" + paddedNum;

            ObjectNode ec2Node = result.addObject();
            ec2Node.put("serverNumber", serverNum);
            ec2Node.put("serverHostName", hostName);
            ec2Node.put("ec2FilterName", ec2FilterName);
            ec2Node.put("state", "unknown");
            ec2Node.put("instanceId", "");
        }

        return result;
    }

    private ArrayNode getJarVersionInfo() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode result = mapper.createArrayNode();

        JsonNode serverIpJson = parseServerIpJson(mapper);
        if (serverIpJson == null || serverIpJson.isNull() || !serverIpJson.isArray()) {
            return result;
        }

        for (int i = 0; i < serverIpJson.size(); i++) {
            JsonNode node = serverIpJson.get(i);
            String hostName = node.get("hostName").asText();
            String userAndIp = node.get("userAndIp").asText();

            try {
                String command = "ls -lt /home/ubuntu/queueReader/*.jar 2>/dev/null | head -5";
                String output = executeSshCommandAndGetOutput(userAndIp, command);

                if (!Strings.isNullOrEmpty(output)) {
                    ObjectNode jarNode = result.addObject();
                    jarNode.put("serverHostName", hostName);
                    jarNode.put("jarInfo", output.trim());
                }
            } catch (Exception ex) {
                LOG.warn("Failed to get JAR info from " + hostName, ex);
            }
        }

        return result;
    }

    private String getServerInfo(String serverNumber) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode serverIpJson = parseServerIpJson(mapper);
        if (serverIpJson == null || serverIpJson.isNull() || !serverIpJson.isArray()) {
            return null;
        }

        for (int i = 0; i < serverIpJson.size(); i++) {
            JsonNode node = serverIpJson.get(i);
            String hostName = node.get("hostName").asText();
            String existingNum = hostName.startsWith("QR") ? hostName.substring(2) : hostName;
            if (existingNum.equals(serverNumber)) {
                return node.get("userAndIp").asText() + "|" + node.get("ipAddress").asText() + "|" + hostName;
            }
        }

        return null;
    }

    private String getAwsInstanceId(String serverNumber) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode serverIpJson = parseServerIpJson(mapper);
        if (serverIpJson == null || serverIpJson.isNull() || !serverIpJson.isArray()) {
            return null;
        }

        for (int i = 0; i < serverIpJson.size(); i++) {
            JsonNode node = serverIpJson.get(i);
            String awsId = node.get("awsInstanceId").asText();
            if (!Strings.isNullOrEmpty(awsId)) {
                return awsId;
            }
        }

        return null;
    }

    private JsonNode parseServerIpJson(ObjectMapper mapper) throws Exception {
        String rawConfig = ConfigManager.getConfiguration("server_ips", "queuereader");
        LOG.info("Raw server_ips config: " + (rawConfig == null ? "null" : rawConfig.substring(0, Math.min(200, rawConfig.length()))));

        if (rawConfig == null || rawConfig.trim().isEmpty()) {
            return null;
        }

        try {
            JsonNode parsed = mapper.readTree(rawConfig);
            LOG.info("Parsed server_ips type: " + parsed.getNodeType() + ", size: " + (parsed.isArray() ? parsed.size() : "N/A"));

            if (parsed.isTextual()) {
                return mapper.readTree(parsed.asText());
            }
            return parsed;
        } catch (Exception e) {
            LOG.error("Failed to parse server_ips config", e);
            return null;
        }
    }

    private String parseUserAndIp(String serverInfo) {
        String[] parts = serverInfo.split("\\|");
        return parts[0];
    }

    private String getMemoryForQueue(String queueName) {
        if (Strings.isNullOrEmpty(queueName)) return "2g";
        if (queueName.startsWith("EdsInbound") || queueName.startsWith("EdsTransform")) return "2g";
        if (queueName.startsWith("EdsProtocol") || queueName.startsWith("EdsSubscriber")) return "650m";
        return "2g";
    }

    private String getMemoryForQueueFromConfig(String queueName) {
        String queueType = "inbound";
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
        String configKey = "queuereader-memory-defaults-" + queueType;
        try {
            String xml = ConfigManager.getConfiguration(configKey);
            if (!Strings.isNullOrEmpty(xml)) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(xml);
                if (node.has("maxHeap") && node.has("unit")) {
                    int maxHeap = node.get("maxHeap").asInt();
                    String unit = node.get("unit").asText();
                    if (unit != null && unit.equalsIgnoreCase("G")) {
                        return maxHeap + "g";
                    }
                    return maxHeap + "m";
                }
            }
        } catch (Exception ex) {
            LOG.warn("Failed to load memory defaults for queue: " + queueName, ex);
        }
        return getMemoryForQueue(queueName);
    }

    private void executeSshCommand(String userAndIp, String command, String hostName) throws Exception {
        int ix = userAndIp.indexOf("@");
        String userName = userAndIp.substring(0, ix - 1);
        String ip = userAndIp.substring(ix + 1);

        JSch jsch = new JSch();
        Session session = jsch.getSession(userName, ip, 22);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(10000);

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
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
        LOG.debug("Completed command on " + hostName);
    }

    private String executeSshCommandAndGetOutput(String userAndIp, String command) throws Exception {
        int ix = userAndIp.indexOf("@");
        String userName = userAndIp.substring(0, ix - 1);
        String ip = userAndIp.substring(ix + 1);

        JSch jsch = new JSch();
        Session session = jsch.getSession(userName, ip, 22);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(10000);

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.setInputStream(null);
        ((ChannelExec) channel).setErrStream(System.err);

        InputStream in = channel.getInputStream();
        channel.connect();

        StringBuilder output = new StringBuilder();
        byte[] tmp = new byte[1024];
        while (!channel.isClosed()) {
            if (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) break;
                output.append(new String(tmp, 0, i));
            }
            if (channel.isClosed()) break;
            Thread.sleep(500);
        }
        channel.disconnect();
        session.disconnect();

        return output.toString();
    }

    private void executeEc2Start(String awsInstanceId) throws Exception {
        LOG.info("Starting EC2 instance: " + awsInstanceId);
        executeSshCommand("ubuntu@localhost", "aws ec2 start-instances --instance-ids " + awsInstanceId + " --region eu-west-2", "EC2");
    }

    private void executeEc2Stop(String awsInstanceId) throws Exception {
        LOG.info("Stopping EC2 instance: " + awsInstanceId);
        executeSshCommand("ubuntu@localhost", "aws ec2 stop-instances --instance-ids " + awsInstanceId + " --force --region eu-west-2", "EC2");
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "QueueServerEndpoint.saveQueueAssignments")
    @Path("/saveQueueAssignments")
    public Response saveQueueAssignments(@Context SecurityContext sc, JsonQueueAssignment request) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "Save Queue Assignments", "Queue", request.getQueueName());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        try {
            String[] serverNumbers = request.getServerNumbers();
            if (serverNumbers == null || serverNumbers.length == 0) {
                result.put("success", false);
                result.put("error", "At least one server must be assigned");
                clearLogbackMarkers();
                return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
            }

            String queueName = request.getQueueName();
            if (Strings.isNullOrEmpty(queueName)) {
                result.put("success", false);
                result.put("error", "Queue name is required");
                clearLogbackMarkers();
                return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
            }

            JsonNode serverIpJson = parseServerIpJson(mapper);
            if (serverIpJson == null || serverIpJson.isNull() || !serverIpJson.isArray()) {
                result.put("success", false);
                result.put("error", "No server configuration found");
                clearLogbackMarkers();
                return Response.status(Response.Status.NOT_FOUND).entity(result).build();
            }

            ArrayNode servers = mapper.createArrayNode();
            for (int i = 0; i < serverIpJson.size(); i++) {
                servers.add(serverIpJson.get(i));
            }

            boolean anyUpdated = false;
            for (String serverNum : serverNumbers) {
                String targetNum = serverNum.startsWith("QR") ? serverNum.substring(2) : serverNum;
                for (int i = 0; i < servers.size(); i++) {
                    JsonNode node = servers.get(i);
                    String existingNum = node.get("hostName").asText();
                    existingNum = existingNum.startsWith("QR") ? existingNum.substring(2) : existingNum;
                    if (existingNum.equals(targetNum)) {
                        ObjectNode updatedNode = mapper.createObjectNode();
                        updatedNode.put("serverNumber", node.get("serverNumber").asText());
                        updatedNode.put("hostName", node.get("hostName").asText());
                        updatedNode.put("ipAddress", node.get("ipAddress").asText());
                        updatedNode.put("userAndIp", node.get("userAndIp").asText());
                        updatedNode.put("awsInstanceId", node.has("awsInstanceId") ? node.get("awsInstanceId").asText() : "");

                        String existingQueues = node.has("queues") && !node.get("queues").isNull() ? node.get("queues").asText() : "";
                        java.util.Set<String> queueSet = new java.util.HashSet<>();
                        if (!Strings.isNullOrEmpty(existingQueues)) {
                            for (String q : existingQueues.trim().split("\\s+")) {
                                if (!q.isEmpty()) queueSet.add(q);
                            }
                        }

                        boolean isNew = !queueSet.contains(queueName);
                        queueSet.add(queueName);

                        StringBuilder sb = new StringBuilder();
                        java.util.List<String> sortedQueues = new java.util.ArrayList<>(queueSet);
                        java.util.Collections.sort(sortedQueues);
                        for (int j = 0; j < sortedQueues.size(); j++) {
                            if (j > 0) sb.append(" ");
                            sb.append(sortedQueues.get(j));
                        }

                        updatedNode.put("queues", sb.toString());
                        servers.set(i, updatedNode);
                        anyUpdated = true;
                        LOG.info("Queue '" + queueName + "' " + (isNew ? "added to" : "already on") + " QR" + targetNum);
                    }
                }
            }

            if (!anyUpdated) {
                result.put("success", false);
                result.put("error", "No matching servers found");
                clearLogbackMarkers();
                return Response.status(Response.Status.NOT_FOUND).entity(result).build();
            }

            String configJson = mapper.writeValueAsString(servers);
            ConfigManager.setConfiguration("server_ips", "queuereader", configJson);

            userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "Queue assignment saved", "Queue", queueName, "Servers", String.join(",", serverNumbers));

            result.put("success", true);
            result.put("message", "Queue '" + queueName + "' assigned to " + serverNumbers.length + " server(s)");
        } catch (Exception ex) {
            LOG.error("Failed to save queue assignments for " + request.getQueueName(), ex);
            result.put("success", false);
            result.put("error", ex.getMessage());
        }

        clearLogbackMarkers();
        return Response.ok().entity(result).build();
    }
}
