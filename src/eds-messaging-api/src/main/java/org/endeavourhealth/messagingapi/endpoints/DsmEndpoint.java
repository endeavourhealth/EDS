package org.endeavourhealth.messagingapi.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.AuditAction;
import org.endeavourhealth.core.database.dal.audit.models.AuditModule;
import org.endeavourhealth.core.subscribers.PublisherHelper;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/dsm")
public class DsmEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(DsmEndpoint.class);

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Library);

    @GET
    @Path("/hasDPA/{odsCode}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="TestEndpoint.HasDPA")
    /*@RolesAllowed({"dds_api_read_only"})*/ //allow anyone to use this endpoint
    public Response hasDpa(@Context SecurityContext sc, @ApiParam(value="ODS Code") @PathParam(value = "odsCode") String odsCode) throws Exception{
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Test HasDPA");

        LOG.info("Test Endpoint - HasDPA for odsCode" + odsCode);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = new ObjectNode(mapper.getNodeFactory());

        boolean hasDpa = PublisherHelper.hasDpa(null, null, odsCode);
        root.put("odsCode", odsCode);
        root.put("hasDPA", hasDpa);

        String json = mapper.writeValueAsString(root);
        LOG.info(json);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(json)
                .build();
    }

    /*@GET
    @Path("/getSubscriberConfigNames/{odsCode}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="TestEndpoint.GetSubscriberConfigNames")
    *//*@RolesAllowed({"dds_api_read_only"})*//* //allow anyone to use this endpoint
    public Response getSubscriberConfigNames(@Context SecurityContext sc, @ApiParam(value="ODS Code") @PathParam(value = "odsCode") String odsCode) throws Exception{
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Test GetSubscriberConfigNames");

        LOG.info("Test Endpoint - GetSubscriberConfigNames for " + odsCode);

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode root = new ArrayNode(mapper.getNodeFactory());

        ServiceDalI serviceDal = DalProvider.factoryServiceDal();
        Service service = serviceDal.getByLocalIdentifier(odsCode);
        if (service == null) {
            ObjectNode objectNode = root.addObject();
            objectNode.put("error", "no service found for " + odsCode);
        } else {

            UUID serviceId = service.getId();
            List<String> subscriberConfigNames = SubscriberHelper.getSubscriberConfigNamesForPublisher(null, serviceId);

            ObjectNode objectNode = root.addObject();
            ArrayNode arrayNode = objectNode.putArray("subscriberConfigNames");
            for (String subscriberConfigName: subscriberConfigNames) {
                arrayNode.add(subscriberConfigName);
            }
        }

        String json = mapper.writeValueAsString(root);
        LOG.info(json);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(json)
                .build();
    }*/


}
