package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.configuration.PostMessageToExchangeConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryDalI;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.ActiveItem;
import org.endeavourhealth.core.database.dal.admin.models.Item;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.*;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.messaging.pipeline.components.OpenEnvelope;
import org.endeavourhealth.core.messaging.pipeline.components.PostMessageToExchange;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.subscribers.SubscriberHelper;
import org.endeavourhealth.core.xml.TransformErrorSerializer;
import org.endeavourhealth.core.xml.transformError.Arg;
import org.endeavourhealth.core.xml.transformError.Error;
import org.endeavourhealth.core.xml.transformError.ExceptionLine;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.transform.common.AuditWriter;
import org.endeavourhealth.transform.common.ExchangeHelper;
import org.endeavourhealth.transform.common.ExchangePayloadFile;
import org.endeavourhealth.ui.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

@Path("/exchangeAudit")
public class ExchangeAuditEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(ExchangeAuditEndpoint.class);

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.ExchangeAudit);
    private static final ExchangeDalI auditRepository = DalProvider.factoryExchangeDal();
    private static final ServiceDalI serviceRepository = DalProvider.factoryServiceDal();
    private static final LibraryDalI libraryRepository = DalProvider.factoryLibraryDal();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="ExchangeAuditEndpoint.GetExchangeList")
    @Path("/getExchangesByDate")
    public Response getExchangesByDate(@Context SecurityContext sc,
                                    @QueryParam("serviceId") String serviceIdStr,
                                    @QueryParam("systemId") String systemIdStr,
                                    @QueryParam("maxRows") int maxRows,
                                    @QueryParam("dateFrom") Long dateFromMillis,
                                    @QueryParam("dateTo") Long dateToMillis) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Exchange List",
                "Service Id", serviceIdStr);

        UUID serviceUuid = UUID.fromString(serviceIdStr);
        UUID systemUuid = UUID.fromString(systemIdStr);
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

        List<Exchange> exchanges = auditRepository.getExchangesByService(serviceUuid, systemUuid, maxRows, dateFrom, dateTo);
        List<JsonExchange> ret = convertExchangesToJson(serviceUuid, systemUuid, exchanges);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="ExchangeAuditEndpoint.GetExchangeList")
    @Path("/getRecentExchanges")
    public Response getRecentExchanges(@Context SecurityContext sc,
                                          @QueryParam("serviceId") String serviceIdStr,
                                          @QueryParam("systemId") String systemIdStr,
                                          @QueryParam("maxRows") int maxRows) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Recent Exchanges",
                "Service Id", serviceIdStr,
                "System Id", systemIdStr,
                "Max Rows", maxRows);

        UUID serviceUuid = UUID.fromString(serviceIdStr);
        UUID systemUuid = UUID.fromString(systemIdStr);

        //just use the date search function, but passing in the full date range of start of time to now
        Date dateFrom = new Date(0);
        Date dateTo = new Date();

        List<Exchange> exchanges = auditRepository.getExchangesByService(serviceUuid, systemUuid, maxRows, dateFrom, dateTo);
        List<JsonExchange> ret = convertExchangesToJson(serviceUuid, systemUuid, exchanges);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="ExchangeAuditEndpoint.GetExchangeList")
    @Path("/getExchangesFromFirstError")
    public Response getExchangesFromFirstError(@Context SecurityContext sc,
                                       @QueryParam("serviceId") String serviceIdStr,
                                       @QueryParam("systemId") String systemIdStr,
                                       @QueryParam("maxRows") int maxRows) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Exchanges From Error",
                "Service Id", serviceIdStr,
                "System Id", systemIdStr,
                "Max Rows", maxRows);

        UUID serviceUuid = UUID.fromString(serviceIdStr);
        UUID systemUuid = UUID.fromString(systemIdStr);

        Date dateFrom = null;
        Date dateTo = null;

        ExchangeTransformErrorState errorState = auditRepository.getErrorState(serviceUuid, systemUuid);
        if (errorState == null) {
            //if there's no error state, then there's no errors, so just return the last maxRows over the full time period possible
            dateFrom = new Date(0);
            dateTo = new Date();

        } else {
            //if there is an error state, then we know the exchange ID of the first one in error
            UUID firstExchangeIdInError = errorState.getExchangeIdsInError().get(0);
            Exchange firstExchangeInError = auditRepository.getExchange(firstExchangeIdInError);
            Date errorDate = firstExchangeInError.getTimestamp();

            //we want to return the previous exchange, to clearly show it did transform OK, and also the "maxRows"
            //exchanges after. To do this, we need to just search using date, making an assumption that the exchanges
            //are one a day at least.
            Calendar cal = Calendar.getInstance();
            cal.setTime(errorDate);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);

            //go back a day to get the previous exchange
            cal.add(Calendar.DAY_OF_YEAR, -1);
            dateFrom = cal.getTime();

            //go forward maxRows plus 2 (one day to put us back on the original date and one date for luck)
            cal.add(Calendar.DAY_OF_YEAR, maxRows+1);
            dateTo = cal.getTime();
        }

        List<Exchange> exchanges = auditRepository.getExchangesByService(serviceUuid, systemUuid, maxRows, dateFrom, dateTo);
        List<JsonExchange> ret = convertExchangesToJson(serviceUuid, systemUuid, exchanges);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private List<JsonExchange> convertExchangesToJson(UUID serviceUuid, UUID systemUuid, List<Exchange> exchanges) throws Exception {

        List<JsonExchange> ret = new ArrayList<>();

        Set<UUID> exchangeIdsInError = findAllExchangeIdsInErrorForService(serviceUuid, systemUuid);

        for (Exchange exchange: exchanges) {

            UUID exchangeId = exchange.getId();

            Date timestamp = exchange.getTimestamp();
            Map<String, String> headers = exchange.getHeaders();
            List<String> bodyLines = getExchangeBodyLines(exchange);
            boolean inError = exchangeIdsInError.contains(exchangeId);
            Long exchangeSizeBytes = getExchangeSize(exchange);
            String exchangeSizeDesc = null;
            if (exchangeSizeBytes != null) {
                exchangeSizeDesc = FileUtils.byteCountToDisplaySize(exchangeSizeBytes.longValue());
            }
            Map<String, String> routingKeys = getRoutingKeys(exchange);

            JsonExchange jsonExchange = new JsonExchange(exchangeId, serviceUuid, systemUuid, timestamp, headers, bodyLines, inError, exchangeSizeBytes, exchangeSizeDesc, routingKeys);
            ret.add(jsonExchange);
        }

        return ret;
    }

    private Map<String, String> getRoutingKeys(Exchange exchange) throws Exception {

        List<QueueHelper.ExchangeName> exchangeNames = new ArrayList<>();
        exchangeNames.add(QueueHelper.ExchangeName.INBOUND);
        exchangeNames.add(QueueHelper.ExchangeName.PROTOCOL);
        exchangeNames.add(QueueHelper.ExchangeName.TRANSFORM);
        exchangeNames.add(QueueHelper.ExchangeName.SUBSCRIBER);

        Map<String, String> ret = new HashMap<>();

        for (QueueHelper.ExchangeName exchangeName: exchangeNames) {
            PostMessageToExchangeConfig config = QueueHelper.findExchangeConfig(exchangeName);

            try {
                String routingKey = PostMessageToExchange.getRoutingKey(false, exchange.getHeaders(), config);
                ret.put(exchangeName.getName(), routingKey);
            } catch (PipelineException px) {
                //with the subscriber queues configured to require a subscriber batch, the above will
                //throw an exception because there is no subscriber batch in the exchange header, so deal with it
                ret.put(exchangeName.getName(), "N/A");
            }
        }

        return ret;
    }

    /**
     * returns the size of the exchange data, in a human readable format
     */
    private Long getExchangeSize(Exchange exchange) {

        //the SFTP Reader now adds a header giving us the total size, so use that if present
        Long l = exchange.getHeaderAsLong(HeaderKeys.TotalFileSize);
        if (l != null) {
            return l;
        }

        String body = exchange.getBody();

        try {
            long extractSize = 0;
            List<ExchangePayloadFile> files = ExchangeHelper.parseExchangeBody(body);
            for (ExchangePayloadFile file: files) {

                //if we hit some old extract data without the size in the JSON, then just abort
                if (file.getSize() == null) {
                    return null;
                }

                extractSize += file.getSize().longValue();
            }

            return new Long(extractSize);

        } catch (Throwable t) {
            //if the body can't be parsed into payload files, then it's most likely because it's either
            //really old data or HL7 concent
            return null;
        }



    }

    private Set<UUID> findAllExchangeIdsInErrorForService(UUID serviceId, UUID systemId) throws Exception {

        Set<UUID> ret = new HashSet<>();
        List<ExchangeTransformErrorState> errorStates = auditRepository.getErrorStatesForService(serviceId, systemId);
        for (ExchangeTransformErrorState errorState: errorStates) {
            List<UUID> exchangeIds = errorState.getExchangeIdsInError();
            for (UUID exchangeId: exchangeIds) {
                ret.add(exchangeId);
            }
        }

        return ret;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="ExchangeAuditEndpoint.GetExchangeById")
    @Path("/getExchangeById")
    public Response getExchangeById(@Context SecurityContext sc,
                                    @QueryParam("serviceId") String serviceIdStr,
                                    @QueryParam("systemId") String systemIdStr,
                                    @QueryParam("exchangeId") String exchangeIdStr) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Exchange For ID",
                "Service Id", serviceIdStr,
                "System Id", systemIdStr,
                "Exchange Id", exchangeIdStr);

        UUID exchangeUuid = null;
        try {
            exchangeUuid = UUID.fromString(exchangeIdStr);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid Exchange ID");
        }

        UUID serviceUuid = UUID.fromString(serviceIdStr);
        UUID systemUuid = UUID.fromString(systemIdStr);

        Exchange exchange = auditRepository.getExchange(exchangeUuid);
        if (exchange == null) {
            throw new BadRequestException("No exchange found for " + exchangeIdStr);
        }

        //validate the exchange is for our service
        UUID exchangeServiceId = exchange.getServiceId();
        if (exchangeServiceId == null
                || !exchangeServiceId.equals(serviceUuid)) {

            String err = "Exchange isn't for this service";

            if (exchangeServiceId != null) {
                Service service = serviceRepository.getById(exchangeServiceId);
                if (service != null) {
                    err += " (" + service.getName() + ")";
                }
            }

            throw new BadRequestException(err);
        }

        //validate the exchange is for the right system
        UUID exchangeSystemId = exchange.getSystemId();
        if (exchangeSystemId == null
                || !exchangeSystemId.equals(systemUuid)) {
            String err = "Exchange isn't for this system";
            throw new BadRequestException(err);
        }

        List<Exchange> exchanges = new ArrayList<>();
        exchanges.add(exchange);

        List<JsonExchange> ret = convertExchangesToJson(serviceUuid, systemUuid, exchanges);

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

    /*@GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="ExchangeAuditEndpoint.GetExchangeEvents")
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
    }*/

    /**
     * endpoint just for testing that users with two different roles can make a specific call
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="TestEndpoint.TestPostToExchange")
    @Path("/testPostToExchange")
    @RequiresAdmin
    public Response testPostToExchange(@Context SecurityContext sc) throws Exception {
        return testPostToExchangeImpl(sc);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="TestEndpoint.TestPostToExchangeApi")
    @Path("/testPostToExchangeApi")
    @RolesAllowed({"dds_requeue_message"})
    public Response testPostToExchangeApi(@Context SecurityContext sc) throws Exception {
        return testPostToExchangeImpl(sc);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="TestEndpoint.TestPostToExchangeAll")
    @Path("/testPostToExchangeAll")
    @PermitAll
    public Response testPostToExchangeAll(@Context SecurityContext sc) throws Exception {
        return testPostToExchangeImpl(sc);
    }


    private Response testPostToExchangeImpl(SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "TestPostToExchange");

        LOG.info("TestPostToExchange");

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = new ObjectNode(mapper.getNodeFactory());
        root.put("Ok", "Yes");

        String json = mapper.writeValueAsString(root);
        LOG.info(json);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(json)
                .build();
    }

    /**
     * version of the function called by DDS-UI front-end to post into RabbitMQ,
     * requires the eds_admin user role from the realm used by regular users
     */
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="ExchangeAuditEndpoint.PostToExchange")
    @Path("/postToExchange")
    @RequiresAdmin
    public Response postToExchange(@Context SecurityContext sc, JsonPostToExchangeRequest request) throws Exception {
        return postToExchangeImpl(sc, request);
    }


    /**
     * version of the function called by the Inbound Queue Reader to post into RabbitMQ,
     * requires the dds_requeue_message user role from the machine realm
     */
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="ExchangeAuditEndpoint.PostToExchangeApi")
    @Path("/postToExchangeApi")
    @RolesAllowed({"dds_requeue_message"}) //first to allow use by DDS-UI and second to allow use from Emis transform code
    public Response postToExchangeApi(@Context SecurityContext sc, JsonPostToExchangeRequest request) throws Exception {
        return postToExchangeImpl(sc, request);
    }


    private Response postToExchangeImpl(SecurityContext sc, JsonPostToExchangeRequest request) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "Post to exchange",
                "Exchange ID", request.getExchangeId(),
                "Service ID", request.getServiceId(),
                "System ID", request.getSystemId(),
                "Exchange Name", request.getExchangeName(),
                "Post Mode", request.getPostMode(),
                "Subscriber Config", "" + request.getSpecificSubscriberConfigNames(),
                "File Types to Filter", request.getFileTypesToFilterOn(),
                "Delete Error State", request.getDeleteTransformErrorState());

        UUID selectedExchangeId = request.getExchangeId();
        UUID serviceId = request.getServiceId();
        UUID systemId = request.getSystemId();
        String exchangeNameStr = request.getExchangeName();
        String postMode = request.getPostMode();
        Set<String> specificSubscriberConfigNames = request.getSpecificSubscriberConfigNames();
        String fileTypesToFilterOn = request.getFileTypesToFilterOn();
        Boolean deleteErrorState = request.getDeleteTransformErrorState();
        String reason = request.getReason();

        QueueHelper.ExchangeName exchangeName = QueueHelper.ExchangeName.fromName(exchangeNameStr);

        //work out the exchange IDs to post
        List<UUID> exchangeIds = null;

        if (postMode.equalsIgnoreCase("This")) {
            exchangeIds = new ArrayList<>();
            exchangeIds.add(selectedExchangeId);

        } else if (postMode.equalsIgnoreCase("Onwards")) {
            exchangeIds = new ArrayList<>();

            List<UUID> allExchangeIds = auditRepository.getExchangeIdsForService(serviceId, systemId);
            int index = allExchangeIds.indexOf(selectedExchangeId);
            for (int i=index; i<allExchangeIds.size(); i++) {
                UUID exchangeId = allExchangeIds.get(i);
                exchangeIds.add(exchangeId);
            }

        } else if (postMode.equalsIgnoreCase("All")) {
            exchangeIds = auditRepository.getExchangeIdsForService(serviceId, systemId);

        } else if (postMode.equalsIgnoreCase("FullDelete")
                || postMode.equalsIgnoreCase("FullRefresh")
                || postMode.equalsIgnoreCase("QuickRefresh")
                || postMode.equalsIgnoreCase("FullRefreshAdminOnly")) {

            if (exchangeName != QueueHelper.ExchangeName.PROTOCOL) {
                throw new IllegalArgumentException("Invalid post mode [" + postMode + "] when exchange name is [" + exchangeName + "]");
            }

            //make sure there's nothing in the inbound queue otherwise data could be missed (see https://endeavourhealth.atlassian.net/browse/SD-40)
            boolean inQueue = OpenEnvelope.isAnythingInInboundQueue(serviceId, systemId);
            if (inQueue) {
                return Response
                        .ok()
                        .entity("Cannot queue full load or delete while inbound messages are queued")
                        .build();
            }

            if (postMode.equalsIgnoreCase("FullDelete")) {
                QueueHelper.queueUpFullServiceForPopulatingSubscriber(serviceId, true, false, true, specificSubscriberConfigNames, reason);

            } else if (postMode.equalsIgnoreCase("FullRefresh")) {
                QueueHelper.queueUpFullServiceForPopulatingSubscriber(serviceId, false, true, true, specificSubscriberConfigNames, reason);

            } else if (postMode.equalsIgnoreCase("QuickRefresh")) {
                QueueHelper.queueUpFullServiceForPopulatingSubscriber(serviceId, false, false, false, specificSubscriberConfigNames, reason);

            } else if (postMode.equalsIgnoreCase("FullRefreshAdminOnly")) {
                QueueHelper.queueUpPatientsForSubscriberTransform(serviceId, false, true, true, specificSubscriberConfigNames, new ArrayList<>(), reason);

            } else {
                throw new Exception("Unhandled mode [" + postMode + "]");
            }

            exchangeIds = new ArrayList<>(); //just create an empty list so the rest of this function does nothing

        } else {
            throw new IllegalArgumentException("Invalid post mode [" + postMode + "]");
        }

        //work out if there are any transform audits to mark as resubmitted, which we need to do before posting to Rabbit
        //as that will start the transforms and create new audits
        //List<ExchangeTransformAudit> auditsToFlagAsResubmited = new ArrayList<>();
        Set<String> fileTypesSet = null;

        //the below only apply if posting to inbound queue
        if (exchangeName == QueueHelper.ExchangeName.INBOUND) {

            //this bit hasn't been working for a long time, and it's not been missed, so don't bother trying
            /*for (UUID exchangeId : exchangeIds) {
                ExchangeTransformAudit audit = auditRepository.getMostRecentExchangeTransform(serviceId, systemId, exchangeId);
                if (audit != null) {
                    auditsToFlagAsResubmited.add(audit);
                }
            }*/

            //tokenise and validate the filtering file types
            if (!Strings.isNullOrEmpty(fileTypesToFilterOn)) {
                fileTypesSet = new HashSet<>();

                String[] toks = fileTypesToFilterOn.split("\r|\n|,| |;");
                for (String tok: toks) {
                    tok = tok.trim();
                    if (!Strings.isNullOrEmpty(tok)) {
                        fileTypesSet.add(tok);
                    }
                }
            }

            //delete the error state if needed
            if (deleteErrorState != null
                    && deleteErrorState.booleanValue()) {

                ExchangeTransformErrorState state = new ExchangeTransformErrorState();
                state.setServiceId(serviceId);
                state.setSystemId(systemId);

                ExchangeDalI auditRepository = DalProvider.factoryExchangeDal();
                auditRepository.delete(state);
            }
        } else if (exchangeName == QueueHelper.ExchangeName.PROTOCOL) {
            //nothing extra for this

        } else {
            throw new IllegalArgumentException("Invalid exchange name [" + exchangeName + "]");
        }

        Map<String, String> additionalHeaders = request.getAdditionalHeaders();

        //post the exchanges to RabbitMQ
        QueueHelper.postToExchange(exchangeIds, exchangeName, specificSubscriberConfigNames, reason, fileTypesSet, additionalHeaders);

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }




    /*private org.endeavourhealth.core.messaging.exchange.Exchange retrieveExchange(UUID exchangeId) throws Exception {

        Exchange exchangeAudit = new AuditRepository().getExchange(exchangeId);
        String body = exchangeAudit.getBody();
        String headerJson = exchangeAudit.getHeaders();
        HashMap<String, String> headers = ObjectMapperPool.getInstance().readValue(headerJson, HashMap.class);

        org.endeavourhealth.core.messaging.exchange.Exchange exchange = new org.endeavourhealth.core.messaging.exchange.Exchange(exchangeId, body);

        for (Map.Entry<String, String> entry : headers.entrySet())
            exchange.setHeader(entry.getKey(), entry.getValue());

        return exchange;
    }*/


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="ExchangeAuditEndpoint.GetTransformErrorSummaries")
    @Path("/getTransformErrorSummaries")
    public Response getTransformErrorSummaries(@Context SecurityContext sc) throws Exception {
        try {
            super.setLogbackMarkers(sc);

            //get the errors
            List<ExchangeTransformErrorState> errors = auditRepository.getAllErrorStates();

            //generate the list of service details for each error
            //and remove any errors that apply to obsolete/deleted services
            List<Service> services = new ArrayList<>();
            for (int i=errors.size()-1; i>=0; i--) {
                ExchangeTransformErrorState error = errors.get(i);
                UUID serviceId = error.getServiceId();
                UUID systemId = error.getSystemId();

                Service service = serviceRepository.getById(serviceId);
                //SD-205 if a service has been deleted, just skip it
                if (service == null) {
                    errors.remove(i);
                    continue;
                }

                //if a service no longer has the system, skip it
                List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
                if (!systemIds.contains(systemId)) {
                    errors.remove(i);
                    continue;
                }

                //if we make it here, we want to keep the error and add the service
                services.add(service);
            }

            //convert service details to JSON objects and hash by UUID
            List<JsonService> jsonServices = ServiceEndpoint.createAndPopulateJsonServices(services);
            Map<UUID, JsonService> hmJsonServices = new HashMap<>();
            for (JsonService jsonService : jsonServices) {
                hmJsonServices.put(jsonService.getUuid(), jsonService);
            }

            //wrap the errors in JSON objects
            List<JsonTransformServiceErrorSummary> ret = new ArrayList<>();

            for (ExchangeTransformErrorState errorState : errors) {
                JsonTransformServiceErrorSummary summary = convertErrorStateToJson(errorState, hmJsonServices);
                if (summary != null) {
                    ret.add(summary);
                }
            }

            clearLogbackMarkers();

            return Response
                    .ok()
                    .entity(ret)
                    .build();
        } catch (Throwable t) {
            //added explicit try/catch as Tomcat log just shows "null pointer" without any exception
            LOG.error("", t);
            throw t;
        }
    }


    private static JsonTransformServiceErrorSummary convertErrorStateToJson(ExchangeTransformErrorState errorState, Map<UUID, JsonService> hmJsonServices) throws Exception {

        //find service for ID
        UUID serviceId = errorState.getServiceId();
        JsonService jsonService = hmJsonServices.get(serviceId);

        //too confusing to return only the audits that haven't been resubmitted, so just return all of them
        List<UUID> exchangeIdsInError = errorState.getExchangeIdsInError();

        JsonTransformServiceErrorSummary summary = new JsonTransformServiceErrorSummary();
        summary.setService(jsonService);
        summary.setSystemId(errorState.getSystemId());
        summary.setSystemName(getSystemNameForId(errorState.getSystemId()));
        summary.setCountExchanges(exchangeIdsInError.size());
        summary.setExchangeIds(exchangeIdsInError);
        return summary;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="ExchangeAuditEndpoint.GetInboundTransformAudits")
    @Path("/getInboundTransformAudits")
    public Response getInboundTransformAudits(@Context SecurityContext sc,
                                            @QueryParam("serviceId") String serviceIdStr,
                                            @QueryParam("systemId") String systemIdStr,
                                            @QueryParam("exchangeId") String exchangeIdStr,
                                            @QueryParam("getAllAuditsAndEvents") boolean getAllAuditsAndEvents) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Error Details",
                "Exchange Id", exchangeIdStr);

        LOG.trace("getInboundTransformAudits for exchange ID " + exchangeIdStr);

        UUID exchangeId = UUID.fromString(exchangeIdStr);
        UUID serviceId = UUID.fromString(serviceIdStr);
        UUID systemId = UUID.fromString(systemIdStr);

        List<JsonTransformEvent> ret = getInboundTransformEvents(serviceId, systemId, exchangeId, getAllAuditsAndEvents);

        //if we want everything, also include the exchange_event records for the exchange
        if (getAllAuditsAndEvents) {
            List<JsonTransformEvent> l = getExchangeEvents(exchangeId);
            ret.addAll(l);

            List<JsonTransformEvent> l2 = getOutboundTransformEvents(exchangeId);
            ret.addAll(l2);

            List<JsonTransformEvent> l3 = getOutboundSendEvents(exchangeId);
            ret.addAll(l3);
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private List<JsonTransformEvent> getOutboundSendEvents(UUID exchangeId) throws Exception {
        List<JsonTransformEvent> ret = new ArrayList<>();

        //since this SQL is going to be used in just this ONE place, I'm not going to add to the core repo for others to reuse
        Connection connection = ConnectionManager.getAuditConnection();
        PreparedStatement ps = null;
        try {
            //this SQL gets a rough grouping of outbound transform audits, since it's not reasonable to get each distinct event
            String sql = "SELECT a.subscriber_config_name, min(a.inserted_at), max(a.inserted_at), count(1)"
                    + " FROM exchange_batch b"
                    + " INNER JOIN exchange_subscriber_send_audit a"
                    + " ON a.exchange_id = b.exchange_id"
                    + " AND a.exchange_batch_id = b.batch_id"
                    + " WHERE b.exchange_id = ?"
                    + " GROUP BY a.subscriber_config_name, date(a.inserted_at)";
            ps = connection.prepareStatement(sql);

            int col = 1;
            ps.setString(col++, exchangeId.toString());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                col = 1;

                String subscriber = rs.getString(col++);
                java.util.Date started = new java.util.Date(rs.getTimestamp(col++).getTime());
                java.util.Date ended = new java.util.Date(rs.getTimestamp(col++).getTime());
                int count = rs.getInt(col++);

                String eventDesc = "Outbound Writing to " + subscriber;

                JsonTransformEvent a = new JsonTransformEvent();
                a.setExchangeId(exchangeId);
                a.setEventDesc(eventDesc);
                a.setTransformStart(started);
                a.setTransformEnd(ended);
                a.setNumberBatchIdsCreated(new Integer(count));
                ret.add(a);
            }

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }

        return ret;
    }

    private List<JsonTransformEvent> getOutboundTransformEvents(UUID exchangeId) throws Exception {
        List<JsonTransformEvent> ret = new ArrayList<>();

        //since this SQL is going to be used in just this ONE place, I'm not going to add to the core repo for others to reuse
        Connection connection = ConnectionManager.getAuditConnection();
        PreparedStatement ps = null;
        try {
            //this SQL gets a rough grouping of outbound transform audits, since it's not reasonable to get each distinct event
            String sql = "SELECT a.subscriber_config_name, min(a.started), max(a.started), count(1)"
                    + " FROM exchange_batch b"
                    + " INNER JOIN exchange_subscriber_transform_audit a"
                    + " ON a.exchange_id = b.exchange_id"
                    + " AND a.exchange_batch_id = b.batch_id"
                    + " WHERE b.exchange_id = ?"
                    + " GROUP BY a.subscriber_config_name, date(a.started)";
            ps = connection.prepareStatement(sql);

            int col = 1;
            ps.setString(col++, exchangeId.toString());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                col = 1;

                String subscriber = rs.getString(col++);
                java.util.Date started = new java.util.Date(rs.getTimestamp(col++).getTime());
                java.util.Date ended = new java.util.Date(rs.getTimestamp(col++).getTime());
                int count = rs.getInt(col++);

                String eventDesc = "Outbound Transform to " + subscriber;

                JsonTransformEvent a = new JsonTransformEvent();
                a.setExchangeId(exchangeId);
                a.setEventDesc(eventDesc);
                a.setTransformStart(started);
                a.setTransformEnd(ended);
                a.setNumberBatchIdsCreated(new Integer(count));
                ret.add(a);
            }

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }

        return ret;
    }

    private List<JsonTransformEvent> getInboundTransformEvents(UUID serviceId, UUID systemId, UUID exchangeId, boolean getAllAuditsAndEvents) throws Exception {

        List<JsonTransformEvent> ret = new ArrayList<>();

        List<ExchangeTransformAudit> transformAudits = null;

        if (getAllAuditsAndEvents) {
            transformAudits = auditRepository.getAllExchangeTransformAudits(serviceId, systemId, exchangeId);

        } else {
            //if not getting ALL, then just get the latest audit
            ExchangeTransformAudit transformAudit = auditRepository.getMostRecentExchangeTransform(serviceId, systemId, exchangeId);
            transformAudits = new ArrayList<>();
            transformAudits.add(transformAudit);
        }

        for (ExchangeTransformAudit transformAudit: transformAudits) {

            String desc = "Inbound Transform";
            if (transformAudit.getEnded() == null) {
                desc += " (started)";
            } else {
                desc += " (finished)";
            }

            JsonTransformEvent jsonObj = new JsonTransformEvent();
            jsonObj.setExchangeId(exchangeId);
            jsonObj.setVersion(transformAudit.getId());
            jsonObj.setEventDesc(desc);
            jsonObj.setTransformStart(transformAudit.getStarted());
            jsonObj.setTransformEnd(transformAudit.getEnded());
            jsonObj.setNumberBatchIdsCreated(transformAudit.getNumberBatchesCreated());
            jsonObj.setTransformError(transformAudit.getEnded() != null && transformAudit.getErrorXml() != null);
            jsonObj.setTransformSuccess(transformAudit.getEnded() != null && transformAudit.getErrorXml() == null);
            jsonObj.setTransformInProgress(transformAudit.getEnded() == null);
            jsonObj.setResubmitted(transformAudit.isResubmitted());
            jsonObj.setDeleted(transformAudit.getDeleted());
            ret.add(jsonObj);

            //if we're NOT getting everything for this exchange, include any error lines we had
            if (!getAllAuditsAndEvents) {
                List<String> lines = formatTransformAuditErrorLines(transformAudit);
                jsonObj.setLines(lines);
            }
        }

        return ret;
    }

    private List<JsonTransformEvent> getExchangeEvents(UUID exchangeId) throws Exception {

        List<JsonTransformEvent> ret = new ArrayList<>();

        List<ExchangeEvent> exchangeEvents = auditRepository.getExchangeEvents(exchangeId);
        for (ExchangeEvent event: exchangeEvents) {
            String eventDesc = event.getEventDesc();
            Date eventTimestamp = event.getTimestamp();

            //we can only populate a few fields on the json proxy, but the javascript handles this
            JsonTransformEvent jsonObj = new JsonTransformEvent();
            jsonObj.setExchangeId(exchangeId);
            jsonObj.setEventDesc(eventDesc);
            jsonObj.setTransformStart(eventTimestamp);
            ret.add(jsonObj);
        }

        return ret;
    }

    private List<String> formatTransformAuditErrorLines(ExchangeTransformAudit transformAudit) throws Exception {

        //until we need something more powerful, I'm displaying the errors just as a string, to
        //save sending complex JSON objects back to the client
        List<String> lines = new ArrayList<>();

        if (Strings.isNullOrEmpty(transformAudit.getErrorXml())) {
            return lines;
        }

        TransformError errors = TransformErrorSerializer.readFromXml(transformAudit.getErrorXml());

        for (Error error : errors.getError()) {

            //the error will only be null for older errors, from before the field was introduced
            if (error.getDatetime() != null) {
                Calendar calendar = error.getDatetime().toGregorianCalendar();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
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

    public static String getSystemNameForId(UUID systemId) throws Exception {
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
    @Timed(absolute = true, name="ExchangeAuditEndpoint.RerunAllExchangeInError")
    @Path("/rerunAllExchangesInError")
    @RequiresAdmin
    public Response rerunAllExchangesInError(@Context SecurityContext sc, JsonTransformRerunRequest request) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Rerun All Exchanges",
                "Request", request);

        LOG.trace("Re-queuing for " + request);
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

        List<UUID> exchangeIdsToRePost = new ArrayList<>();

        for (UUID exchangeId: errorState.getExchangeIdsInError()) {

            //update the transform audit, so EDS UI knows we've re-queued this exchange
            ExchangeTransformAudit audit = auditRepository.getMostRecentExchangeTransform(serviceId, systemId, exchangeId);
            if (audit != null) {

                //skip any exchange IDs we've already re-queued up to be processed again
                if (audit.isResubmitted()) {
                    LOG.debug("Not re-posting " + audit.getExchangeId() + " as it's already been resubmitted");
                    continue;
                }
            }

            //then re-submit the exchange to Rabbit MQ for the queue reader to pick up
            LOG.debug("Re-posting " + exchangeId);
            exchangeIdsToRePost.add(exchangeId);

            //if we only want to re-queue the first exchange, then break out
            if (firstOnly) {
                break;
            }
        }

        //post to rabbit
        QueueHelper.postToExchange(exchangeIdsToRePost, QueueHelper.ExchangeName.INBOUND, null, null);

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="ExchangeAuditEndpoint.GetTransformErrorLines")
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

        List<String> lines = formatTransformAuditErrorLines(transformAudit);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(lines)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="ExchangeAuditEndpoint.getSubscriberConfigNamesForService")
    @Path("/getSubscriberConfigNamesForService")
    public Response getSubscriberConfigNamesForService(@Context SecurityContext sc,
                                           @QueryParam("serviceId") String serviceIdStr) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Get Subscriber Config Names For Service",
                "Service Id", serviceIdStr);

        UUID serviceId = UUID.fromString(serviceIdStr);
        ServiceDalI serviceDal = DalProvider.factoryServiceDal();
        Service service = serviceDal.getById(serviceId);
        String odsCode = service.getLocalId();
        List<String> ret = SubscriberHelper.getSubscriberConfigNamesForPublisher(null, serviceId);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="ExchangeAuditEndpoint.getAllSubscriberConfigNames")
    @Path("/getAllSubscriberConfigNames")
    public Response getAllSubscriberConfigNames(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Get Subscriber Config Names For Service");

        List<String> ret = new ArrayList<>();
        Map<String, String> configs = ConfigManager.getConfigurations("db_subscriber");
        for (String configName: configs.keySet()) {
            ret.add(configName);
        }
        ret.sort(((o1, o2) -> o1.toLowerCase().compareToIgnoreCase(o2.toLowerCase())));
        //List<String> ret = RunDataDistributionProtocols.getAllSubscriberConfigNamesFromOldProtocols();

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }



    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="ExchangeAuditEndpoint.RequeueServicesInError")
    @Path("/requeueServicesInError")
    @RequiresAdmin
    public Response rerunAllExchangesInError(@Context SecurityContext sc, List<JsonTransformRerunRequest> requests) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Rerun All Exchanges For Services",
                "Request", requests);

        for (JsonTransformRerunRequest request: requests) {
            LOG.trace("Re-queuing for " + request);
            rerunExchanges(request, false);
        }


        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="ExchangeAuditEndpoint.AddExchangeEvent")
    @Path("/addExchangeEvent")
    @RequiresAdmin
    public Response addExchangeEvent(@Context SecurityContext sc, JsonAddExchangeEventRequest request) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Add exchange event",
                "Request", request);

        UUID exchangeId = request.getExchangeId();
        String text = request.getText();
        AuditWriter.writeExchangeEvent(exchangeId, text);

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }

}

