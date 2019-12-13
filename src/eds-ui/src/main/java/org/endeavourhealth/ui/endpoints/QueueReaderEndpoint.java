package org.endeavourhealth.ui.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.configuration.ConfigDeserialiser;
import org.endeavourhealth.core.configuration.QueueReaderConfiguration;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.ApplicationHeartbeatDalI;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.ApplicationHeartbeat;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
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
import java.util.List;

@Path("/queueReader")
public class QueueReaderEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(QueueReaderEndpoint.class);

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Library);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/status")
    public Response getQueueReaderStatus(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Queue Reader Status");

        String ret = getQueueReaderStatusJson();

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private static String getQueueReaderStatusJson() throws Exception {

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

            //if any records for non-queue reader apps that don't have a sub-ID, skip them
            if (Strings.isNullOrEmpty(appSubId)) {
                continue;
            }

            String queueReaderConfigXml = ConfigManager.getConfiguration(appSubId, appId);

            //if not a queue reader app, then skip
            if (Strings.isNullOrEmpty(queueReaderConfigXml)) {
                continue;
            }
            QueueReaderConfiguration configuration = ConfigDeserialiser.deserialise(queueReaderConfigXml);
            String queueName = configuration.getQueue();

            Integer maxHeap = h.getMaxHeapMb();
            String maxHeapDesc = null;
            if (maxHeap != null) {
                maxHeapDesc = FileUtils.byteCountToDisplaySize(maxHeap * (1024L * 1024L));
            }


            ObjectNode objectNode = root.addObject();
            objectNode.put("applicationName", h.getApplicationName());
            objectNode.put("applicationInstanceName", h.getApplicationInstanceName());
            objectNode.put("timestmp", h.getTimestmp().getTime());
            objectNode.put("hostName", h.getHostName());
            objectNode.put("isBusy", h.getBusy());
            objectNode.put("maxHeapMb", h.getMaxHeapMb());
            objectNode.put("currentHeapMb", h.getCurrentHeapMb());
            objectNode.put("maxHeapDesc", maxHeapDesc);
            objectNode.put("queueName", queueName);
        }

        return mapper.writeValueAsString(root);
    }

}