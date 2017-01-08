package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.cache.ObjectMapperPool;
import org.endeavourhealth.core.configuration.ApiConfiguration;
import org.endeavourhealth.core.configuration.ConfigWrapper;
import org.endeavourhealth.core.configuration.Pipeline;
import org.endeavourhealth.core.configuration.PostMessageToExchangeConfig;
import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.ActiveItem;
import org.endeavourhealth.core.data.admin.models.Item;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.data.audit.AuditRepository;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.*;
import org.endeavourhealth.core.data.config.ConfigManager;
import org.endeavourhealth.core.messaging.pipeline.components.PostMessageToExchange;
import org.endeavourhealth.core.security.SecurityUtils;
import org.endeavourhealth.core.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.utility.StreamExtension;
import org.endeavourhealth.core.xml.TransformErrorSerializer;
import org.endeavourhealth.core.xml.transformError.Arg;
import org.endeavourhealth.core.xml.transformError.Error;
import org.endeavourhealth.core.xml.transformError.ExceptionLine;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.ui.json.JsonTransformExchangeError;
import org.endeavourhealth.ui.json.JsonTransformRerunRequest;
import org.endeavourhealth.ui.json.JsonTransformServiceErrorSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Path("/transformErrors")
public class TransformErrorsEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(TransformErrorsEndpoint.class);

    private static final AuditRepository auditRepository = new AuditRepository();
    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);
    private static final ServiceRepository serviceRepository = new ServiceRepository();
    private static final LibraryRepository libraryRepository = new LibraryRepository();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getErrorSummaries")
    public Response getErrorSummaries(@Context SecurityContext sc) throws Exception {
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
    @Path("/getErrorDetails")
    public Response getErrorDetails(@Context SecurityContext sc,
                                    @QueryParam("serviceId") String serviceIdStr,
                                    @QueryParam("systemId") String systemIdStr,
                                    @QueryParam("exchangeId") String exchangeIdStr) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
            "Error Details",
                "Exchange Id", exchangeIdStr);

        LOG.trace("getErrorDetails for exchange ID " + exchangeIdStr);

        UUID exchangeId = UUID.fromString(exchangeIdStr);
        UUID serviceId = UUID.fromString(serviceIdStr);
        UUID systemId = UUID.fromString(systemIdStr);
        ExchangeTransformAudit transformAudit = auditRepository.getMostRecentExchangeTransform(serviceId, systemId, exchangeId);
        TransformError errors = TransformErrorSerializer.readFromXml(transformAudit.getErrorXml());

        //until we need something more powerful, I'm displaying the errors just as a string, to
        //save sending complex JSON objects back to the client
        List<String> lines = new ArrayList<>();

        for (Error error: errors.getError()) {

            for (Arg arg: error.getArg()) {
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

                for (ExceptionLine line: exception.getLine()) {
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

        JsonTransformExchangeError ret = new JsonTransformExchangeError();
        ret.setExchangeId(exchangeId);
        ret.setTransformStart(transformAudit.getStarted());
        ret.setTransformEnd(transformAudit.getEnded());
        ret.setLines(lines);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
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
    @Path("/rerunFirstExchange")
    @RequiresAdmin
    public Response rerunFirstExchange(@Context SecurityContext sc, JsonTransformRerunRequest request) throws Exception {
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
    @Path("/rerunAllExchanges")
    @RequiresAdmin
    public Response rerunAllExchanges(@Context SecurityContext sc, JsonTransformRerunRequest request) throws Exception {
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
            postToRabbit(exchangeId);

            //if we only want to re-queue the first exchange, then break out
            if (firstOnly) {
                break;
            }
        }

    }

    private void postToRabbit(UUID exchangeId) throws Exception {

        Exchange exchangeAudit = new AuditRepository().getExchange(exchangeId);
        String body = exchangeAudit.getBody();
        String headerJson = exchangeAudit.getHeaders();
        HashMap<String, String> headers = ObjectMapperPool.getInstance().readValue(headerJson, HashMap.class);

        org.endeavourhealth.core.messaging.exchange.Exchange exchange = new org.endeavourhealth.core.messaging.exchange.Exchange(exchangeId, body);
        for (String header: headers.keySet()) {
            exchange.setHeader(header, headers.get(header));
        }

        //we access the messaging API config directly, to find out how it posts new incoming exchanges to rabbit
        String apiConfigXml = ConfigManager.getConfiguration("api-configuration", "messaging-api");
        ApiConfiguration config = ConfigWrapper.deserialise(apiConfigXml);
        ApiConfiguration.PostMessageAsync postConfig = config.getPostMessageAsync();
        Pipeline pipeline = postConfig.getPipeline();

        PostMessageToExchangeConfig exchangeConfig = pipeline
                .getPipelineComponents()
                .stream()
                .filter(t -> t instanceof PostMessageToExchangeConfig)
                .map(t -> (PostMessageToExchangeConfig)t)
                .collect(StreamExtension.singleOrNullCollector());

        if (exchangeConfig == null) {
            throw new BadRequestException("Failed to find PostMessageToExchange config details for messaging api");
        }

        //re-post back into Rabbit using the same pipeline component as used by the messaging API
        PostMessageToExchange component = new PostMessageToExchange(exchangeConfig);
        component.process(exchange);
    }

/*
    private void postToRabbit(UUID exchangeId) throws Exception {

        Exchange exchangeAudit = new AuditRepository().getExchange(exchangeId);
        String body = exchangeAudit.getBody();
        String headerJson = exchangeAudit.getHeaders();
        HashMap<String, String> headers = ObjectMapperPool.getInstance().readValue(headerJson, HashMap.class);

        org.endeavourhealth.core.messaging.exchange.Exchange exchange = new org.endeavourhealth.core.messaging.exchange.Exchange(exchangeId, body);
        for (String header: headers.keySet()) {
            exchange.setHeader(header, headers.get(header));
        }

        String repostMessageToExchangeConfigJson = ConfigManager.getConfiguration("rePostMessageToExchangeConfig");
        JsonNode c = ObjectMapperPool.getInstance().readTree(repostMessageToExchangeConfigJson);


        Credentials credentials = new Credentials();
        credentials.setUsername(c.get("username").asText());
        credentials.setPassword(c.get("password").asText());

        PostMessageToExchangeConfig config = new PostMessageToExchangeConfig();
        config.setCredentials(credentials);
        config.setMulticastHeader(c.get("mMulticastHeader").asText());
        config.setNodes(c.get("nodes").asText());
        config.setRoutingHeader(c.get("routingHeader").asText());
        config.setExchange(c.get("exchange").asText());

        //re-post back into Rabbit using the same pipeline component as used by the messaging API
        PostMessageToExchange component = new PostMessageToExchange(config);
        component.process(exchange);
    }*/

}
