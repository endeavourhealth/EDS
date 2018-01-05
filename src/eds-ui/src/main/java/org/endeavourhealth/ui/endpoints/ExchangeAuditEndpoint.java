
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


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="EDS-UI.ExchangeAuditEndpoint.GetExchangeList")
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

        List<Exchange> exchangeByServices = auditRepository.getExchangesByService(serviceUuid, maxRows, dateFrom, dateTo);

        Set<UUID> exchangeIdsInError = findAllExchangeIdsInErrorForService(serviceUuid);

        for (Exchange exchangeByService: exchangeByServices) {

            UUID exchangeId = exchangeByService.getId();
            Exchange exchange = auditRepository.getExchange(exchangeId);
            Date timestamp = exchange.getTimestamp();
            Map<String, String> headers = exchange.getHeaders();
            List<String> bodyLines = getExchangeBodyLines(exchange);
            boolean inError = exchangeIdsInError.contains(exchangeId);

            JsonExchange jsonExchange = new JsonExchange(exchangeId, serviceUuid, timestamp, headers, bodyLines, inError);
            ret.add(jsonExchange);
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    private Set<UUID> findAllExchangeIdsInErrorForService(UUID serviceId) throws Exception {

        Set<UUID> ret = new HashSet<>();
        List<ExchangeTransformErrorState> errorStates = auditRepository.getErrorStatesForService(serviceId);
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
        Map<String, String> headers = exchange.getHeaders();
        List<String> bodyLines = getExchangeBodyLines(exchange);

        //validate the exchange is for our service
        String exchangeServiceIdStr = headers.get(HeaderKeys.SenderServiceUuid);
        if (exchangeServiceIdStr == null
                || !exchangeServiceIdStr.equals(serviceIdStr)) {
            String err = "Exchange isn't for this service";

            if (!Strings.isNullOrEmpty(exchangeServiceIdStr)) {
                Service service = serviceRepository.getById(UUID.fromString(serviceIdStr));
                if (service != null) {
                    err += " (" + service.getName() + ")";
                }
            }

            throw new BadRequestException(err);
        }

        Set<UUID> exchangeIdsInError = findAllExchangeIdsInErrorForService(serviceUuid);
        boolean inError = exchangeIdsInError.contains(exchangeUuid);

        JsonExchange jsonExchange = new JsonExchange(exchangeUuid, serviceUuid, timestamp, headers, bodyLines, inError);
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
    }

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
                "Exchange Name", request.getExchangeName(),
                "Post Mode", request.getPostMode(),
                "Protocol ID", request.getSpecificProtocolId());

        UUID selectedExchangeId = request.getExchangeId();
        UUID serviceId = request.getServiceId();
        String exchangeName = request.getExchangeName();
        String postMode = request.getPostMode();
        UUID specificProtocolId = request.getSpecificProtocolId();

        if (postMode.equalsIgnoreCase("This")) {
            QueueHelper.postToExchange(selectedExchangeId, exchangeName, specificProtocolId, true);

        } else if (postMode.equalsIgnoreCase("Onwards")) {
            List<UUID> exchangeIds = new ArrayList<>();

            List<UUID> allExchangeIds = auditRepository.getExchangeIdsForService(serviceId);
            int index = allExchangeIds.indexOf(selectedExchangeId);
            for (int i=index; i<allExchangeIds.size(); i++) {
                UUID exchangeId = allExchangeIds.get(i);
                exchangeIds.add(exchangeId);
            }

            QueueHelper.postToExchange(exchangeIds, exchangeName, specificProtocolId, true);

        } else if (postMode.equalsIgnoreCase("All")) {
            List<UUID> exchangeIds = auditRepository.getExchangeIdsForService(serviceId);
            QueueHelper.postToExchange(exchangeIds, exchangeName, specificProtocolId, true);

        } else {
            throw new IllegalArgumentException("Invalid post mode [" + postMode + "]");
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
        jsonService.setHasInboundError(true); //by definition, all services in this fn as in error

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
                                     @QueryParam("getMostRecent") boolean getMostRecent,
                                     @QueryParam("getErrorLines") boolean getErrorLines) throws Exception {
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
            jsonObj.setVersion(transformAudit.getId());
            jsonObj.setTransformStart(transformAudit.getStarted());
            jsonObj.setTransformEnd(transformAudit.getEnded());
            jsonObj.setNumberBatchIdsCreated(transformAudit.getNumberBatchesCreated());
            jsonObj.setHadErrors(transformAudit.getErrorXml() != null);
            jsonObj.setResubmitted(transformAudit.isResubmitted());
            jsonObj.setDeleted(transformAudit.getDeleted());
            ret.add(jsonObj);

            if (getErrorLines) {
                List<String> lines = formatTransformAuditErrorLines(transformAudit);
                jsonObj.setLines(lines);
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

    private static String getSystemNameForId(UUID systemId) throws Exception {
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

        for (UUID exchangeId: errorState.getExchangeIdsInError()) {

            //update the transform audit, so EDS UI knows we've re-queued this exchange
            ExchangeTransformAudit audit = auditRepository.getMostRecentExchangeTransform(serviceId, systemId, exchangeId);
            if (audit != null) {

                //skip any exchange IDs we've already re-queued up to be processed again
                if (audit.isResubmitted()) {
                    LOG.debug("Not re-posting " + audit.getExchangeId() + " as it's already been resubmitted");
                    continue;
                }

                audit.setResubmitted(true);
                auditRepository.save(audit);
            }

            //then re-submit the exchange to Rabbit MQ for the queue reader to pick up
            LOG.debug("Re-posting " + exchangeId);
            QueueHelper.postToExchange(exchangeId, "EdsInbound", null, false);

            //if we only want to re-queue the first exchange, then break out
            if (firstOnly) {
                break;
            }
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
                                           @QueryParam("serviceId") String serviceIdStr) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load,
                "Get Protocols For Service",
                "Service Id", serviceIdStr);

        List<JsonProtocol> ret = new ArrayList<>();

        List<LibraryItem> libraryItems = LibraryRepositoryHelper.getProtocolsByServiceId(serviceIdStr);
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

