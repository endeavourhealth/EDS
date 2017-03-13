
package org.endeavourhealth.ui.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.core.audit.AuditWriter;
import org.endeavourhealth.core.configuration.*;
import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.ActiveItem;
import org.endeavourhealth.core.data.admin.models.Item;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.data.audit.AuditRepository;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.*;
import org.endeavourhealth.core.data.ehr.ExchangeBatchRepository;
import org.endeavourhealth.core.data.ehr.models.ExchangeBatch;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.TransformBatch;
import org.endeavourhealth.core.messaging.pipeline.components.DetermineRelevantProtocolIds;
import org.endeavourhealth.core.messaging.pipeline.components.PostMessageToExchange;
import org.endeavourhealth.core.messaging.pipeline.components.RunDataDistributionProtocols;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.core.xml.QueryDocument.ServiceContract;
import org.endeavourhealth.core.xml.QueryDocument.ServiceContractType;
import org.endeavourhealth.core.xml.TransformErrorSerializer;
import org.endeavourhealth.core.xml.transformError.Arg;
import org.endeavourhealth.core.xml.transformError.Error;
import org.endeavourhealth.core.xml.transformError.ExceptionLine;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.queuereader.ConfigDeserialiser;
import org.endeavourhealth.ui.json.*;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Path("/exchangeAudit")
public class ExchangeAuditEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(ExchangeAuditEndpoint .class);

    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);
    private static final AuditRepository auditRepository = new AuditRepository();
    private static final ServiceRepository serviceRepository = new ServiceRepository();
    private static final LibraryRepository libraryRepository = new LibraryRepository();


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getExchangeList")
    public Response getExchangeList(@Context SecurityContext sc, @QueryParam("serviceId") String serviceIdStr,
                                                                 @QueryParam("maxRows") int maxRows,
                                                                 @QueryParam("dateFrom") Long dateFromMillis,
                                                                 @QueryParam("dateTo") Long dateToMillis) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Exchange List",
                "Service Id", serviceIdStr);


        List<JsonExchange> ret = new ArrayList<>();

        UUID serviceUuid = UUID.fromString(serviceIdStr);
        Date dateFrom = new Date(0);
        Date dateTo = new Date();

        if (dateFromMillis != null) {
            dateFrom = new Date(dateFromMillis.longValue());
        }

        if (dateToMillis != null) {
            dateTo = new Date(dateToMillis.longValue());

            //if the date to didn't have any TIME specified, then we want to include everything done on this
            //date, so set the time to be the last minute of the date
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateTo);
            if (cal.get(Calendar.HOUR_OF_DAY) == 0
                    && cal.get(Calendar.MINUTE) == 0) {
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                dateTo = cal.getTime();
            }
        }

        List<ExchangeByService> exchangeByServices = auditRepository.getExchangesByService(serviceUuid, maxRows, dateFrom, dateTo);
        for (ExchangeByService exchangeByService: exchangeByServices) {

            UUID exchangeId = exchangeByService.getExchangeId();
            Exchange exchange = auditRepository.getExchange(exchangeId);
            Date timestamp = exchange.getTimestamp();
            String headerJson = exchange.getHeaders();
            HashMap<String, String> headers = ObjectMapperPool.getInstance().readValue(headerJson, HashMap.class);
            List<String> bodyLines = getExchangeBodyLines(exchange);

            JsonExchange jsonExchange = new JsonExchange(exchangeId, serviceUuid, timestamp, headers, bodyLines);
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
    @Path("/getExchangeById")
    public Response getExchangeById(@Context SecurityContext sc, @QueryParam("serviceId") String serviceIdStr, @QueryParam("exchangeId") String exchangeIdStr) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Exchange For ID",
                "Service Id", serviceIdStr,
                "Exchange Id", exchangeIdStr);

        List<JsonExchange> ret = new ArrayList<>();

        UUID exchangeUuid = null;
        try {
            exchangeUuid = UUID.fromString(exchangeIdStr);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid Exchange ID");
        }

        UUID serviceUuid = UUID.fromString(serviceIdStr);

        Exchange exchange = auditRepository.getExchange(exchangeUuid);
        if (exchange == null) {
            throw new BadRequestException("No exchange found for " + exchangeIdStr);
        }

        Date timestamp = exchange.getTimestamp();
        String headerJson = exchange.getHeaders();
        HashMap<String, String> headers = ObjectMapperPool.getInstance().readValue(headerJson, HashMap.class);
        List<String> bodyLines = getExchangeBodyLines(exchange);

        //validate the exchange is for our service
        String exchangeServiceIdStr = headers.get(HeaderKeys.SenderServiceUuid);
        if (exchangeServiceIdStr == null
                || !exchangeServiceIdStr.equals(serviceIdStr)) {
            throw new BadRequestException("Exchange isn't for this service");
        }

        JsonExchange jsonExchange = new JsonExchange(exchangeUuid, serviceUuid, timestamp, headers, bodyLines);
        ret.add(jsonExchange);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private static List<String> getExchangeBodyLines(Exchange exchange) {
        List<String> ret = new ArrayList<>();

        String body = exchange.getBody();
        if (Strings.isNullOrEmpty(body)) {
            ret.add("<no body>");
        } else {

            String[] lines = body.split("\n");
            for (String line: lines) {
                line = line.trim(); //make sure to get rid of any \r characters
                ret.add(line);
            }
        }
        return ret;
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

        postToExchange(exchangeId, exchangeName);

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }

    private void postToExchange(UUID exchangeId, String exchangeName) throws Exception {

        PostMessageToExchangeConfig exchangeConfig = findExchangeConfig(exchangeName);
        if (exchangeConfig == null) {
            throw new BadRequestException("Failed to find PostMessageToExchange config details for exchange " + exchangeName);
        }

        org.endeavourhealth.core.messaging.exchange.Exchange exchange = retrieveExchange(exchangeId);

        //to make sure the latest setup applies, re-calculate the protocols that apply to this exchange
        String serviceUuid = exchange.getHeader(HeaderKeys.SenderServiceUuid);
        String newProtocolIdsJson = DetermineRelevantProtocolIds.getProtocolIdsForPublisherService(serviceUuid);
        String oldProtocolIdsJson = exchange.getHeader(HeaderKeys.ProtocolIds);
        if (!newProtocolIdsJson.equals(oldProtocolIdsJson)) {
            exchange.setHeader(HeaderKeys.ProtocolIds, newProtocolIdsJson);
            AuditWriter.writeExchange(exchange);
        }

        //work out what multicast header we need
        String multicastHeader = exchangeConfig.getMulticastHeader();
        if (!Strings.isNullOrEmpty(multicastHeader)) {
            if (exchange.getHeader(multicastHeader) == null) {
                populateMulticastHeader(exchange, multicastHeader);
            }
        }

        postToExchange(exchangeConfig, exchange);

        //write an event for the exchange, so we can see this happened
        AuditWriter.writeExchangeEvent(exchange, "Manually pushed into " + exchangeName + " exchange");
    }

    private void populateMulticastHeader(org.endeavourhealth.core.messaging.exchange.Exchange exchange, String multicastHeader) throws Exception {

        UUID exchangeUuid = exchange.getExchangeId();

        if (multicastHeader.equalsIgnoreCase(HeaderKeys.BatchIdsJson)) {

            ExchangeBatchRepository exchangeBatchRepository = new ExchangeBatchRepository();
            List<ExchangeBatch> batches = exchangeBatchRepository.retrieveForExchangeId(exchangeUuid);

            List<UUID> batchUuids = batches
                    .stream()
                    .map(t -> t.getBatchId())
                    .collect(Collectors.toList());
            try {
                String batchUuidsStr = ObjectMapperPool.getInstance().writeValueAsString(batchUuids.toArray());
                exchange.setHeader(HeaderKeys.BatchIdsJson, batchUuidsStr);

            } catch (JsonProcessingException e) {
                LOG.error("Failed to populate batch IDs for exchange " + exchangeUuid, e);
            }

        } else if (multicastHeader.equalsIgnoreCase(HeaderKeys.TransformBatch)) {

            List<TransformBatch> transformBatches = new ArrayList<>();

            String[] protocolIds = exchange.getHeaderAsStringArray(HeaderKeys.ProtocolIds);

            ExchangeBatchRepository exchangeBatchRepository = new ExchangeBatchRepository();
            List<ExchangeBatch> batches = exchangeBatchRepository.retrieveForExchangeId(exchangeUuid);

            for (String protocolId: protocolIds) {

                UUID protocolUuid = UUID.fromString(protocolId);
                LibraryItem libraryItem = LibraryRepositoryHelper.getLibraryItem(protocolUuid);
                List<ServiceContract> subscribers = libraryItem
                        .getProtocol()
                        .getServiceContract()
                        .stream()
                        .filter(sc -> sc.getType().equals(ServiceContractType.SUBSCRIBER))
                        .collect(Collectors.toList());

                //if there's no subscribers on this protocol, just skip it
                if (subscribers.isEmpty()) {
                    continue;
                }

                for (ExchangeBatch batch: batches) {

                    String batchId = batch.getBatchId().toString();
                    Map<ResourceType, List<UUID>> filteredResources = RunDataDistributionProtocols.filterResources(libraryItem.getProtocol(), batchId);
                    if (filteredResources.isEmpty()) {
                        continue;
                    }

                    //the only bit of data we can populate is the batch ID
                    TransformBatch transformBatch = new TransformBatch();
                    transformBatch.setBatchId(batch.getBatchId());
                    transformBatch.setProtocolId(UUID.fromString(protocolId));
                    transformBatch.setSubscribers(subscribers);
                    transformBatch.setResourceIds(filteredResources);

                    transformBatches.add(transformBatch);
                }
            }

            try {
                String transformBatchesJson = ObjectMapperPool.getInstance().writeValueAsString(transformBatches);
                exchange.setHeader(HeaderKeys.TransformBatch, transformBatchesJson);

            } catch (JsonProcessingException e) {
                LOG.error("Failed to populate batch IDs for exchange " + exchangeUuid, e);
            }

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

        for (Map.Entry<String, String> entry : headers.entrySet())
            exchange.setHeader(entry.getKey(), entry.getValue());

        return exchange;
    }

    private void postToExchange(PostMessageToExchangeConfig exchangeConfig, org.endeavourhealth.core.messaging.exchange.Exchange exchange) throws Exception {

        //re-post back into Rabbit using the same pipeline component as used by the messaging API
        PostMessageToExchange component = new PostMessageToExchange(exchangeConfig);
        component.process(exchange);

    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getTransformErrorSummaries")
    public Response getTransformErrorSummaries(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        LOG.trace("getErrors");

        List<JsonTransformServiceErrorSummary> ret = new ArrayList<>();

        for (ExchangeTransformErrorState errorState: auditRepository.getAllErrorStates()) {
            JsonTransformServiceErrorSummary summary = convertErrorStateToJson(errorState);
            if (summary != null) {
                ret.add(summary);
            }
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }


    private static JsonTransformServiceErrorSummary convertErrorStateToJson(ExchangeTransformErrorState errorState) {

        if (errorState == null) {
            return null;
        }

        //the error state has a list of all exchange IDs that are currently in error, but we only
        //want to return to EDS UI those that haven't yet been resubmitted
        List<UUID> exchangeIdsInError = new ArrayList<>();
        for (UUID exchangeId: errorState.getExchangeIdsInError()) {
            ExchangeTransformAudit audit = auditRepository.getMostRecentExchangeTransform(errorState.getServiceId(), errorState.getSystemId(), exchangeId);
            if (!audit.isResubmitted()) {
                exchangeIdsInError.add(exchangeId);
            }
        }

        //if all of the exchanges have been resubmitted, then the error state is effectively cleared for now
        if (exchangeIdsInError.isEmpty()) {
            return null;
        }

        JsonTransformServiceErrorSummary summary = new JsonTransformServiceErrorSummary();
        summary.setServiceId(errorState.getServiceId());
        summary.setServiceName(getServiceNameForId(errorState.getServiceId()));
        summary.setSystemId(errorState.getSystemId());
        summary.setSystemName(getSystemNameForId(errorState.getSystemId()));
        summary.setCountExchanges(exchangeIdsInError.size());
        summary.setExchangeIds(exchangeIdsInError);
        return summary;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getTransformErrorDetails")
    public Response getTransformErrorDetails(@Context SecurityContext sc,
                                    @QueryParam("serviceId") String serviceIdStr,
                                    @QueryParam("systemId") String systemIdStr,
                                    @QueryParam("exchangeId") String exchangeIdStr,
                                     @QueryParam("getMostRecent") boolean getMostRecent,
                                     @QueryParam("getErrorLines") boolean getErrorLines) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Error Details",
                "Exchange Id", exchangeIdStr);

        LOG.trace("getErrorDetails for exchange ID " + exchangeIdStr);

        UUID exchangeId = UUID.fromString(exchangeIdStr);
        UUID serviceId = UUID.fromString(serviceIdStr);
        UUID systemId = UUID.fromString(systemIdStr);

        List<JsonTransformExchangeError> ret = new ArrayList<>();

        List<ExchangeTransformAudit> transformAudits = null;

        if (getMostRecent) {
            ExchangeTransformAudit transformAudit = auditRepository.getMostRecentExchangeTransform(serviceId, systemId, exchangeId);
            transformAudits = new ArrayList<>();
            transformAudits.add(transformAudit);

        } else {
            transformAudits = auditRepository.getAllExchangeTransformAudits(serviceId, systemId, exchangeId);
        }

        for (ExchangeTransformAudit transformAudit: transformAudits) {

            JsonTransformExchangeError jsonObj = new JsonTransformExchangeError();
            jsonObj.setExchangeId(exchangeId);
            jsonObj.setVersion(transformAudit.getVersion());
            jsonObj.setTransformStart(transformAudit.getStarted());
            jsonObj.setTransformEnd(transformAudit.getEnded());
            jsonObj.setNumberBatchIdsCreated(transformAudit.getNumberBatchesCreated());
            jsonObj.setHadErrors(transformAudit.getErrorXml() != null);
            jsonObj.setResubmitted(transformAudit.isResubmitted());
            jsonObj.setDeleted(transformAudit.getDeleted());
            ret.add(jsonObj);

            if (getErrorLines) {
                List<String> lines = formatTransformAuditError(transformAudit);
                jsonObj.setLines(lines);
            }
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private List<String> formatTransformAuditError(ExchangeTransformAudit transformAudit) throws Exception {

        //until we need something more powerful, I'm displaying the errors just as a string, to
        //save sending complex JSON objects back to the client
        List<String> lines = new ArrayList<>();

        TransformError errors = TransformErrorSerializer.readFromXml(transformAudit.getErrorXml());

        for (Error error : errors.getError()) {

            //the error will only be null for older errors, from before the field was introduced
            if (error.getDatetime() != null) {
                Calendar calendar = error.getDatetime().toGregorianCalendar();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd/ HH:mm");
                formatter.setTimeZone(calendar.getTimeZone());
                String dateString = formatter.format(calendar.getTime());
                lines.add(dateString);
            }

            for (Arg arg : error.getArg()) {
                String argName = arg.getName();
                String argValue = arg.getValue();
                if (argValue != null) {
                    lines.add(argName + " = " + argValue);
                } else {
                    lines.add(argName);
                }
            }
            lines.add("");

            org.endeavourhealth.core.xml.transformError.Exception exception = error.getException();
            while (exception != null) {

                if (exception.getMessage() != null) {
                    lines.add(exception.getMessage());
                }

                for (ExceptionLine line : exception.getLine()) {
                    String cls = line.getClazz();
                    String method = line.getMethod();
                    Integer lineNumber = line.getLine();

                    lines.add("\u00a0\u00a0\u00a0\u00a0at " + cls + "." + method + ":" + lineNumber);

                    //lines.add("&nbsp;&nbsp;&nbsp;&nbsp;at " + cls + "." + method + ":" + lineNumber);
                }

                exception = exception.getCause();
                if (exception != null) {
                    lines.add("Caused by:");
                }
            }

            //add some space between the separate errors in the audit
            lines.add("");
            lines.add("------------------------------------------------------------------------");
        }

        return lines;
    }

    private static String getServiceNameForId(UUID serviceId) {
        try {
            Service service = serviceRepository.getById(serviceId);
            return service.getName();
        } catch (NullPointerException ex ) {
            LOG.error("Failed to find service for ID " + serviceId, ex);
            return "UNKNOWN";
        }
    }

    private static String getSystemNameForId(UUID systemId) {
        try {
            ActiveItem activeItem = libraryRepository.getActiveItemByItemId(systemId);
            Item item = libraryRepository.getItemByKey(systemId, activeItem.getAuditId());
            return item.getTitle();
        } catch (NullPointerException ex) {
            LOG.error("Failed to find system for ID " + systemId, ex);
            return "UNKNOWN";
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/rerunFirstExchangeInError")
    @RequiresAdmin
    public Response rerunFirstExchangeInError(@Context SecurityContext sc, JsonTransformRerunRequest request) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Rerun First Exchange",
                "Request", request);

        LOG.info("Rerun first");
        rerunExchanges(request, true);

        //we return the updated error state, so the UI can replace its old content
        ExchangeTransformErrorState errorState = auditRepository.getErrorState(request.getServiceId(), request.getSystemId());
        JsonTransformServiceErrorSummary ret = convertErrorStateToJson(errorState);

        clearLogbackMarkers();

        return Response
                .ok(ret)
                .build();
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/rerunAllExchangesInError")
    @RequiresAdmin
    public Response rerunAllExchangesInError(@Context SecurityContext sc, JsonTransformRerunRequest request) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Rerun All Exchanges",
                "Request", request);

        LOG.info("rerunAllExchanges");
        rerunExchanges(request, false);

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }


    private void rerunExchanges(JsonTransformRerunRequest request, boolean firstOnly) throws Exception {

        UUID serviceId = request.getServiceId();
        UUID systemId = request.getSystemId();

        ExchangeTransformErrorState errorState = auditRepository.getErrorState(serviceId, systemId);

        for (UUID exchangeId: errorState.getExchangeIdsInError()) {

            //update the transform audit, so EDS UI knows we've re-queued this exchange
            ExchangeTransformAudit audit = auditRepository.getMostRecentExchangeTransform(serviceId, systemId, exchangeId);

            //skip any exchange IDs we've already re-queued up to be processed again
            if (audit.isResubmitted()) {
                continue;
            }

            audit.setResubmitted(true);
            auditRepository.save(audit);

            //then re-submit the exchange to Rabbit MQ for the queue reader to pick up
            postToExchange(exchangeId, "EdsInbound");

            //if we only want to re-queue the first exchange, then break out
            if (firstOnly) {
                break;
            }
        }

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getTransformErrorLines")
    public Response getTransformErrorLines(@Context SecurityContext sc,
                                             @QueryParam("serviceId") String serviceIdStr,
                                             @QueryParam("systemId") String systemIdStr,
                                             @QueryParam("exchangeId") String exchangeIdStr,
                                             @QueryParam("version") String versionStr) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Get Transform Error Lines",
                "Exchange Id", exchangeIdStr,
                "Version", versionStr);

        LOG.trace("getTransformErrorLines for exchange ID " + exchangeIdStr + " and version " + versionStr);

        UUID serviceId = UUID.fromString(serviceIdStr);
        UUID systemId = UUID.fromString(systemIdStr);
        UUID exchangeId = UUID.fromString(exchangeIdStr);
        UUID version = UUID.fromString(versionStr);

        ExchangeTransformAudit transformAudit = auditRepository.getExchangeTransformAudit(serviceId, systemId, exchangeId, version);

        List<String> lines = formatTransformAuditError(transformAudit);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(lines)
                .build();
    }
}

