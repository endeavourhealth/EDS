
package org.endeavourhealth.ui.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import org.endeavourhealth.core.cache.ObjectMapperPool;
import org.endeavourhealth.core.configuration.*;
import org.endeavourhealth.core.data.audit.AuditRepository;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.*;
import org.endeavourhealth.core.data.config.ConfigManager;
import org.endeavourhealth.core.data.ehr.ExchangeBatchRepository;
import org.endeavourhealth.core.data.ehr.models.ExchangeBatch;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.components.PostMessageToExchange;
import org.endeavourhealth.core.security.SecurityUtils;
import org.endeavourhealth.core.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.utility.StreamExtension;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.queuereader.ConfigDeserialiser;
import org.endeavourhealth.ui.json.JsonExchange;
import org.endeavourhealth.ui.json.JsonExchangeEvent;
import org.endeavourhealth.ui.json.JsonPostToExchangeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;
import java.util.stream.Collectors;

@Path("/exchangeAudit")
public class ExchangeAuditEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(ExchangeAuditEndpoint .class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);
    private static final AuditRepository auditRepository = new AuditRepository();

    /**
     * function to create missing records in the Exchange_by_service table from data in the Exchange table
     */
    /*@POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/createMissingData")
    @RequiresAdmin
    public Response createMissingData(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Create Missing Exchange_by_service");

        Map<UUID, Set<UUID>> existingOnes = new HashMap();

        ExchangeBatchRepository exchangeBatchRepository = new ExchangeBatchRepository();

        List<Exchange> exchanges = auditRepository.getAllExchanges();
        for (Exchange exchange: exchanges) {

            UUID exchangeUuid = exchange.getExchangeId();
            String headerJson = exchange.getHeaders();
            HashMap<String, String> headers = ObjectMapperPool.getInstance().readValue(headerJson, HashMap.class);

            String serviceId = headers.get(HeaderKeys.SenderServiceUuid);
            if (serviceId == null) {
                LOG.warn("No service ID found for exchange " + exchange.getExchangeId());
                continue;
            }
            UUID serviceUuid = UUID.fromString(serviceId);

            Set<UUID> exchangeIdsDone = existingOnes.get(serviceUuid);
            if (exchangeIdsDone == null) {
                exchangeIdsDone = new HashSet<>();

                List<ExchangeByService> exchangeByServices = auditRepository.getExchangesByService(serviceUuid, Integer.MAX_VALUE);
                for (ExchangeByService exchangeByService: exchangeByServices) {
                    exchangeIdsDone.add(exchangeByService.getExchangeId());
                }

                existingOnes.put(serviceUuid, exchangeIdsDone);
            }

            //create the exchange by service entity
            if (!exchangeIdsDone.contains(exchangeUuid)) {

                Date timestamp = exchange.getTimestamp();

                ExchangeByService newOne = new ExchangeByService();
                newOne.setExchangeId(exchangeUuid);
                newOne.setServiceId(serviceUuid);
                newOne.setTimestamp(timestamp);

                auditRepository.save(newOne);
            }

            if (!headers.containsKey(HeaderKeys.BatchIds)) {

                //fix the batch IDs not being in the exchange
                List<ExchangeBatch> batches = exchangeBatchRepository.retrieveForExchangeId(exchangeUuid);
                if (!batches.isEmpty()) {

                    List<UUID> batchUuids = batches
                            .stream()
                            .map(t -> t.getExchangeId())
                            .collect(Collectors.toList());
                    try {
                        String batchUuidsStr = ObjectMapperPool.getInstance().writeValueAsString(batchUuids.toArray());
                        headers.put(HeaderKeys.BatchIds, batchUuidsStr);

                        auditRepository.save(exchange, null);

                    } catch (JsonProcessingException e) {
                        LOG.error("Failed to populate batch IDs for exchange " + exchangeUuid, e);
                    }
                }
            }
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }*/

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getExchangeList")
    public Response getExchangeList(@Context SecurityContext sc, @QueryParam("serviceId") String serviceIdStr, @QueryParam("maxRows") int maxRows) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Exchange List",
                "Service Id", serviceIdStr);


        List<JsonExchange> ret = new ArrayList<>();

        UUID serviceUuid = UUID.fromString(serviceIdStr);

        List<ExchangeByService> exchangeByServices = auditRepository.getExchangesByService(serviceUuid, maxRows);
        for (ExchangeByService exchangeByService: exchangeByServices) {

            UUID exchangeId = exchangeByService.getExchangeId();
            Exchange exchange = auditRepository.getExchange(exchangeId);
            Date timestamp = exchange.getTimestamp();
            String headerJson = exchange.getHeaders();
            HashMap<String, String> headers = ObjectMapperPool.getInstance().readValue(headerJson, HashMap.class);

            JsonExchange jsonExchange = new JsonExchange(exchangeId, serviceUuid, timestamp, headers);
            ret.add(jsonExchange);
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getExchangeEvents")
    public Response getExchangeEvents(@Context SecurityContext sc, @QueryParam("exchangeId") String exchangeIdStr) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Exchange Events",
                "Exchange Id", exchangeIdStr);

        List<JsonExchangeEvent> ret = new ArrayList<>();

        UUID exchangeId = UUID.fromString(exchangeIdStr);

        List<ExchangeEvent> exchangeEvents = auditRepository.getExchangeEvents(exchangeId);
        for (ExchangeEvent exchangeEvent: exchangeEvents) {

            Date timestamp = exchangeEvent.getTimestamp();
            String eventStr = exchangeEvent.getEventDesc();

            JsonExchangeEvent jsonExchangeEvent = new JsonExchangeEvent(timestamp, eventStr);
            ret.add(jsonExchangeEvent);
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/postToExchange")
    @RequiresAdmin
    public Response postToExchange(@Context SecurityContext sc, JsonPostToExchangeRequest request) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "Post to exchange",
                "Exchange ID", request.getExchangeId(),
                "Exchange Name", request.getExchangeName());

        UUID exchangeId = request.getExchangeId();
        String exchangeName = request.getExchangeName();

        PostMessageToExchangeConfig exchangeConfig = findExchangeConfig(exchangeName);
        if (exchangeConfig == null) {
            throw new BadRequestException("Failed to find PostMessageToExchange config details for exchange " + exchangeName);
        }

        org.endeavourhealth.core.messaging.exchange.Exchange exchange = retrieveExchange(exchangeId);

        //work out what multicast header we need
        String multicastHeader = exchangeConfig.getMulticastHeader();
        if (!Strings.isNullOrEmpty(multicastHeader)) {
            if (exchange.getHeader(multicastHeader) == null) {
                populateMulticastHeader(exchange, multicastHeader);
            }
        }

        postToExchange(exchangeConfig, exchange);

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }

    private void populateMulticastHeader(org.endeavourhealth.core.messaging.exchange.Exchange exchange, String multicastHeader) throws Exception {

        if (multicastHeader.equalsIgnoreCase(HeaderKeys.BatchIds)) {

            //fix the batch IDs not being in the exchange
            UUID exchangeUuid = exchange.getExchangeId();

            ExchangeBatchRepository exchangeBatchRepository = new ExchangeBatchRepository();
            List<ExchangeBatch> batches = exchangeBatchRepository.retrieveForExchangeId(exchangeUuid);
            if (!batches.isEmpty()) {

                List<UUID> batchUuids = batches
                        .stream()
                        .map(t -> t.getExchangeId())
                        .collect(Collectors.toList());
                try {
                    String batchUuidsStr = ObjectMapperPool.getInstance().writeValueAsString(batchUuids.toArray());
                    exchange.setHeader(HeaderKeys.BatchIds, batchUuidsStr);

                } catch (JsonProcessingException e) {
                    LOG.error("Failed to populate batch IDs for exchange " + exchangeUuid, e);
                }
            }

        } else if (multicastHeader.equalsIgnoreCase(HeaderKeys.TransformBatch)) {

            throw new BadRequestException("Mutlicasting using transform batch not supported");

        } else if (multicastHeader.equalsIgnoreCase(HeaderKeys.SubscriberBatch)) {

            throw new BadRequestException("Mutlicasting using subscriber batch not supported");

        } else {
            throw new BadRequestException("Unsupported multicast header: " + multicastHeader);
        }

    }

    private PostMessageToExchangeConfig findExchangeConfig(String exchangeName) throws Exception {

        //go through all the known app configs to find config for posting to the Rabbit Exchange we're interested in
        PostMessageToExchangeConfig config = findExchangeConfig("messaging-api", "api-configuration", exchangeName);
        if (config != null) {
            return config;
        }

        config = findExchangeConfig("queuereader", "inbound", exchangeName);
        if (config != null) {
            return config;
        }

        config = findExchangeConfig("queuereader", "protocol", exchangeName);
        if (config != null) {
            return config;
        }

        config = findExchangeConfig("queuereader", "response", exchangeName);
        if (config != null) {
            return config;
        }

        config = findExchangeConfig("queuereader", "subscriber", exchangeName);
        if (config != null) {
            return config;
        }

        config = findExchangeConfig("queuereader", "transform", exchangeName);
        if (config != null) {
            return config;
        }

        return null;
    }

    private PostMessageToExchangeConfig findExchangeConfig(String appId, String configId, String exchangeName) throws Exception {

        //we access the messaging API config directly, to find out how it posts new incoming exchanges to rabbit
        String configXml = ConfigManager.getConfiguration(configId, appId);

        Pipeline pipeline = null;

        //the config XML may be one of two serialised classes, so we use a try/catch to safely try both if necessary
        try {
            ApiConfiguration config = ConfigWrapper.deserialise(configXml);
            ApiConfiguration.PostMessageAsync postConfig = config.getPostMessageAsync();
            pipeline = postConfig.getPipeline();

        } catch (Exception e) {

            QueueReaderConfiguration configuration = ConfigDeserialiser.deserialise(configXml);
            pipeline = configuration.getPipeline();
        }

        return pipeline
                .getPipelineComponents()
                .stream()
                .filter(t -> t instanceof PostMessageToExchangeConfig)
                .map(t -> (PostMessageToExchangeConfig)t)
                .filter(t -> t.getExchange().equalsIgnoreCase(exchangeName))
                .collect(StreamExtension.singleOrNullCollector());
    }

    private org.endeavourhealth.core.messaging.exchange.Exchange retrieveExchange(UUID exchangeId) throws Exception {

        Exchange exchangeAudit = new AuditRepository().getExchange(exchangeId);
        String body = exchangeAudit.getBody();
        String headerJson = exchangeAudit.getHeaders();
        HashMap<String, String> headers = ObjectMapperPool.getInstance().readValue(headerJson, HashMap.class);

        org.endeavourhealth.core.messaging.exchange.Exchange exchange = new org.endeavourhealth.core.messaging.exchange.Exchange(exchangeId, body);
        for (String header: headers.keySet()) {
            exchange.setHeader(header, headers.get(header));
        }

        return exchange;
    }

    private void postToExchange(PostMessageToExchangeConfig exchangeConfig, org.endeavourhealth.core.messaging.exchange.Exchange exchange) throws Exception {

        //re-post back into Rabbit using the same pipeline component as used by the messaging API
        PostMessageToExchange component = new PostMessageToExchange(exchangeConfig);
        component.process(exchange);

    }
}

