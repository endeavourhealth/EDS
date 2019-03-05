
package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
import io.astefanutti.metrics.aspectj.Metrics;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryDalI;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.ActiveItem;
import org.endeavourhealth.core.database.dal.admin.models.Item;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.*;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.endeavourhealth.core.xml.TransformErrorSerializer;
import org.endeavourhealth.core.xml.transformError.Arg;
import org.endeavourhealth.core.xml.transformError.Error;
import org.endeavourhealth.core.xml.transformError.ExceptionLine;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.ui.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.text.SimpleDateFormat;
import java.util.*;

@Path("/exchangeAudit")
@Metrics(registry = "EdsRegistry")
public class ExchangeAuditEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(ExchangeAuditEndpoint.class);

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.ExchangeAudit);
    private static final ExchangeDalI auditRepository = DalProvider.factoryExchangeDal();
    private static final ServiceDalI serviceRepository = DalProvider.factoryServiceDal();
    private static final LibraryDalI libraryRepository = DalProvider.factoryLibraryDal();

    private static final String INBOUND_EXCHANGE = "EdsInbound";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.ExchangeAuditEndpoint.GetExchangeList")
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
    @Timed(absolute = true, name="EDS-UI.ExchangeAuditEndpoint.GetExchangeList")
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

        Set<UUID> exchangeIdsInError = findAllExchangeIdsInErrorForService(serviceUuid, systemUuid);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.ExchangeAuditEndpoint.GetExchangeList")
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

            //don't need to re-retrieve, since MySQL gives us all the content. It's only Cassandra that doesn't give everything
            //exchange = auditRepository.getExchange(exchangeId);

            Date timestamp = exchange.getTimestamp();
            Map<String, String> headers = exchange.getHeaders();
            List<String> bodyLines = getExchangeBodyLines(exchange);
            boolean inError = exchangeIdsInError.contains(exchangeId);

            JsonExchange jsonExchange = new JsonExchange(exchangeId, serviceUuid, systemUuid, timestamp, headers, bodyLines, inError);
            ret.add(jsonExchange);
        }

        return ret;
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
    @Timed(absolute = true, name="EDS-UI.ExchangeAuditEndpoint.GetExchangeById")
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
    @Timed(absolute = true, name="EDS-UI.ExchangeAuditEndpoint.GetExchangeEvents")
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

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.ExchangeAuditEndpoint.PostToExchange")
    @Path("/postToExchange")
    @RequiresAdmin
    public Response postToExchange(@Context SecurityContext sc, JsonPostToExchangeRequest request) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "Post to exchange",
                "Exchange ID", request.getExchangeId(),
                "Service ID", request.getServiceId(),
                "System ID", request.getSystemId(),
                "Exchange Name", request.getExchangeName(),
                "Post Mode", request.getPostMode(),
                "Protocol ID", request.getSpecificProtocolId(),
                "File Types to Filter", request.getFileTypesToFilterOn());

        UUID selectedExchangeId = request.getExchangeId();
        UUID serviceId = request.getServiceId();
        UUID systemId = request.getSystemId();
        String exchangeName = request.getExchangeName();
        String postMode = request.getPostMode();
        UUID specificProtocolId = request.getSpecificProtocolId();
        String fileTypesToFilterOn = request.getFileTypesToFilterOn();

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

        } else {
            throw new IllegalArgumentException("Invalid post mode [" + postMode + "]");
        }

        //work out if there are any transform audits to mark as resubmitted, which we need to do before posting to Rabbit
        //as that will start the transforms and create new audits
        List<ExchangeTransformAudit> auditsToFlagAsResubmited = new ArrayList<>();
        if (exchangeName.equals(INBOUND_EXCHANGE)) {
            for (UUID exchangeId : exchangeIds) {
                ExchangeTransformAudit audit = auditRepository.getMostRecentExchangeTransform(serviceId, systemId, exchangeId);
                if (audit != null) {
                    auditsToFlagAsResubmited.add(audit);
                }
            }
        }

        //tokenise and validate the filtering file types
        Set<String> fileTypesSet = null;
        if (!Strings.isNullOrEmpty(fileTypesToFilterOn)) {
            fileTypesSet = new HashSet<>();

            String[] toks = fileTypesToFilterOn.split("\n");
            for (String tok: toks) {
                tok = tok.trim();
                fileTypesSet.add(tok);
            }
        }

        //post the exchanges to RabbitMQ
        QueueHelper.postToExchange(exchangeIds, exchangeName, specificProtocolId, true, fileTypesSet);

        //and update any past transform audits to say we've resubmitted them to rabbit, if it's the inbound queue
        for (ExchangeTransformAudit audit: auditsToFlagAsResubmited) {
            audit.setResubmitted(true);
            auditRepository.save(audit);
        }

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
    @Timed(absolute = true, name="EDS-UI.ExchangeAuditEndpoint.GetTransformErrorSummaries")
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


    private static JsonTransformServiceErrorSummary convertErrorStateToJson(ExchangeTransformErrorState errorState) throws Exception {

        if (errorState == null) {
            return null;
        }

        //too confusing to return only the audits that haven't been resubmitted, so just return all of them
        List<UUID> exchangeIdsInError = errorState.getExchangeIdsInError();

        /*//the error state has a list of all exchange IDs that are currently in error, but we only
        //want to return to EDS UI those that haven't yet been resubmitted
        List<UUID> exchangeIdsInError = new ArrayList<>();
        for (UUID exchangeId: errorState.getExchangeIdsInError()) {
            ExchangeTransformAudit audit = auditRepository.getMostRecentExchangeTransform(errorState.getServiceId(), errorState.getSystemId(), exchangeId);

            if (audit != null
                && !audit.isResubmitted()) {
                exchangeIdsInError.add(exchangeId);
            }
        }

        //if all of the exchanges have been resubmitted, then the error state is effectively cleared for now
        if (exchangeIdsInError.isEmpty()) {
            return null;
        }*/

        UUID serviceId = errorState.getServiceId();
        Service service = serviceRepository.getById(serviceId);

        JsonService jsonService = new JsonService(service);

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
    @Timed(absolute = true, name="EDS-UI.ExchangeAuditEndpoint.GetInboundTransformAudits")
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

        List<JsonTransformExchangeError> ret = new ArrayList<>();

        List<ExchangeTransformAudit> transformAudits = null;

        if (getAllAuditsAndEvents) {
            transformAudits = auditRepository.getAllExchangeTransformAudits(serviceId, systemId, exchangeId);

        } else {
            ExchangeTransformAudit transformAudit = auditRepository.getMostRecentExchangeTransform(serviceId, systemId, exchangeId);
            transformAudits = new ArrayList<>();
            transformAudits.add(transformAudit);
        }

        for (ExchangeTransformAudit transformAudit: transformAudits) {

            String desc = "Transform";
            if (transformAudit.getEnded() == null) {
                desc += " (started)";
            } else {
                desc += " (finished)";
            }

            JsonTransformExchangeError jsonObj = new JsonTransformExchangeError();
            jsonObj.setExchangeId(exchangeId);
            jsonObj.setVersion(transformAudit.getId());
            jsonObj.setEventDesc(desc);
            jsonObj.setTransformStart(transformAudit.getStarted());
            jsonObj.setTransformEnd(transformAudit.getEnded());
            jsonObj.setNumberBatchIdsCreated(transformAudit.getNumberBatchesCreated());
            jsonObj.setHadErrors(transformAudit.getErrorXml() != null);
            jsonObj.setResubmitted(transformAudit.isResubmitted());
            jsonObj.setDeleted(transformAudit.getDeleted());
            ret.add(jsonObj);

            //if we're NOT getting everything for this exchange, include any error lines we had
            if (!getAllAuditsAndEvents) {
                List<String> lines = formatTransformAuditErrorLines(transformAudit);
                jsonObj.setLines(lines);
            }
        }

        //if we want everything, also include the exchange_event records for the exchange
        if (getAllAuditsAndEvents) {
            List<ExchangeEvent> exchangeEvents = auditRepository.getExchangeEvents(exchangeId);
            for (ExchangeEvent event: exchangeEvents) {
                String eventDesc = event.getEventDesc();
                Date eventTimestamp = event.getTimestamp();

                //we can only populate a few fields on the json proxy, but the javascript handles this
                JsonTransformExchangeError jsonObj = new JsonTransformExchangeError();
                jsonObj.setExchangeId(exchangeId);
                jsonObj.setEventDesc(eventDesc);
                jsonObj.setTransformStart(eventTimestamp);
                ret.add(jsonObj);
            }
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
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
    @Timed(absolute = true, name="EDS-UI.ExchangeAuditEndpoint.RerunFirstExchangeInError")
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
    @Timed(absolute = true, name="EDS-UI.ExchangeAuditEndpoint.RerunAllExchangeInError")
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
        LOG.debug("Re-running exchanges firstOnly = " + firstOnly);

        List<UUID> exchangeIdsToRePost = new ArrayList<>();
        List<ExchangeTransformAudit> auditsToMarkAsResubmitted = new ArrayList<>();

        for (UUID exchangeId: errorState.getExchangeIdsInError()) {

            //update the transform audit, so EDS UI knows we've re-queued this exchange
            ExchangeTransformAudit audit = auditRepository.getMostRecentExchangeTransform(serviceId, systemId, exchangeId);
            if (audit != null) {

                //skip any exchange IDs we've already re-queued up to be processed again
                if (audit.isResubmitted()) {
                    LOG.debug("Not re-posting " + audit.getExchangeId() + " as it's already been resubmitted");
                    continue;
                }

                auditsToMarkAsResubmitted.add(audit);
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
        QueueHelper.postToExchange(exchangeIdsToRePost, INBOUND_EXCHANGE, null, true);

        //after posting to rabbit, mark those audits as resubmitted
        for (ExchangeTransformAudit audit: auditsToMarkAsResubmitted) {
            audit.setResubmitted(true);
            auditRepository.save(audit);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.ExchangeAuditEndpoint.GetTransformErrorLines")
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

    /*@POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/postTest")
    @RequiresAdmin
    public Response postTest(@Context SecurityContext sc, JsonPostToExchangeRequest request) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save, "Testing Post Rerequesting");

        LOG.info("-----posting to exchange for service " + request.getServiceId());
        Throwable t = new RuntimeException();
        t.fillInStackTrace();
        LOG.error("", t);

        for (int i=0; i<20; i++) {
            LOG.info("Sleeping " + i);
            Thread.sleep(1000 * 60);
        }
        LOG.info("Done");

        clearLogbackMarkers();

        return Response
                .ok()
                .build();
    }*/

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.ExchangeAuditEndpoint.GetProtocolsForService")
    @Path("/getProtocolsForService")
    public Response getProtocolsForService(@Context SecurityContext sc,
                                           @QueryParam("serviceId") String serviceIdStr,
                                           @QueryParam("systemId") String systemIdStr) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Get Protocols For Service",
                "Service Id", serviceIdStr,
                "System Id", systemIdStr);

        List<JsonProtocol> ret = new ArrayList<>();

        List<LibraryItem> libraryItems = LibraryRepositoryHelper.getProtocolsByServiceId(serviceIdStr, systemIdStr);
        for (LibraryItem libraryItem: libraryItems) {
            Protocol protocol = libraryItem.getProtocol();

            //only return active protocols
            if (protocol.getEnabled() == ProtocolEnabled.TRUE) {

                //only return protocols with an active service contract for our service
                for (ServiceContract serviceContract : protocol.getServiceContract()) {
                    if (serviceContract.getService().getUuid().equals(serviceIdStr)
                        && serviceContract.getActive() == ServiceContractActive.TRUE) {

                        JsonProtocol json = new JsonProtocol();
                        json.setId(UUID.fromString(libraryItem.getUuid()));
                        json.setName(libraryItem.getName());
                        ret.add(json);
                        break;
                    }
                }
            }
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }


}

