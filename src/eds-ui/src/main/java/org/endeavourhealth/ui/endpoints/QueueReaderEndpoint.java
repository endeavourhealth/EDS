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
import org.endeavourhealth.ui.json.JsonQueueReaderCommand;
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
