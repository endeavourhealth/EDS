package org.endeavourhealth.ui.endpoints;

import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.ActiveItem;
import org.endeavourhealth.core.data.admin.models.Item;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.data.audit.AuditRepository;
import org.endeavourhealth.core.data.audit.UserAuditRepository;
import org.endeavourhealth.core.data.audit.models.*;
import org.endeavourhealth.core.security.SecurityUtils;
import org.endeavourhealth.core.security.annotations.RequiresAdmin;
import org.endeavourhealth.core.xml.TransformErrorSerializer;
import org.endeavourhealth.core.xml.transformError.Arg;
import org.endeavourhealth.core.xml.transformError.Error;
import org.endeavourhealth.core.xml.transformError.ExceptionLine;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.ui.json.JsonTransformErrorDetail;
import org.endeavourhealth.ui.json.JsonTransformErrorSummary;
import org.endeavourhealth.ui.json.JsonTransformRequeueRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/transformAudit")
public class TransformAuditEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(TransformAuditEndpoint.class);

    private static final AuditRepository auditRepository = new AuditRepository();
    private static final UserAuditRepository userAudit = new UserAuditRepository(AuditModule.EdsUiModule.Organisation);
    private static final ServiceRepository serviceRepository = new ServiceRepository();
    private static final LibraryRepository libraryRepository = new LibraryRepository();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getErrors")
    public Response getErrors(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        LOG.trace("getErrors");

        List<JsonTransformErrorSummary> ret = new ArrayList<>();

        for (ExchangeTransformErrorState errorState: auditRepository.getAllErrorStates()) {
            ret.add(convertErrorStateToJson(errorState));
        }

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }


    private static JsonTransformErrorSummary convertErrorStateToJson(ExchangeTransformErrorState errorState) {

        List<UUID> exchangeIdsToReProcess = auditRepository.getExchangeUuidsToReProcess(errorState);

        UUID firstExchangeInError = errorState
                .getExchangeIdsInError()
                .stream()
                .filter(T -> !exchangeIdsToReProcess.contains(T))
                .findFirst()
                .get();

        JsonTransformErrorSummary summary = new JsonTransformErrorSummary();
        summary.setServiceId(errorState.getServiceId());
        summary.setSystemName(getServiceNameForId(errorState.getServiceId()));
        summary.setSystemId(errorState.getSystemId());
        summary.setSystemName(getSystemNameForId(errorState.getSystemId()));
        summary.setCountExchanges(errorState.getExchangeIdsInError().size());
        summary.setCountExchangesReQueued(exchangeIdsToReProcess.size());
        summary.setFirstExchangeIdInError(firstExchangeInError);
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
                "Transform Errors for Exchange Id", exchangeIdStr);

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
                lines.add(argName + " = " + argValue);
            }

            org.endeavourhealth.core.xml.transformError.Exception exception = error.getException();
            while (exception != null) {

                lines.add("");
                if (exception.getMessage() != null) {
                    lines.add(exception.getMessage());
                }

                for (ExceptionLine line: exception.getLines()) {
                    String cls = line.getClazz();
                    String method = line.getMethod();
                    Integer lineNumber = line.getLine();

                    lines.add("    at " + cls + "." + method + ":" + lineNumber);
                }

                exception = exception.getCause();
            }

            //add some space between the separate errors in the audit
            lines.add("");
            lines.add("");
        }

        JsonTransformErrorDetail ret = new JsonTransformErrorDetail();
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
    @Path("/requeueTransforms")
    @RequiresAdmin
    public Response requeueTransforms(@Context SecurityContext sc, JsonTransformRequeueRequest request) throws Exception {
        super.setLogbackMarkers(sc);
        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Save,
                "Requeue Transforms", request);

        super.setLogbackMarkers(sc);

        UUID serviceId = request.getServiceId();
        UUID systemId = request.getSystemId();

        ExchangeTransformErrorState errorState = auditRepository.getErrorState(serviceId, systemId);
        List<UUID> exchangesReQueued = auditRepository.getExchangeUuidsToReProcess(errorState);

        for (UUID exchangeId: errorState.getExchangeIdsInError()) {

            //skip any exchange IDs we've already re-queued up to be processed again
            if (exchangesReQueued.contains(exchangeId)) {
                continue;
            }

            //save the entity, so EDS UI knows we've re-queued this exchange
            ExchangeTransformErrorToReProcess o = new ExchangeTransformErrorToReProcess();
            o.setServiceId(errorState.getServiceId());
            o.setSystemId(errorState.getSystemId());
            o.setExchangeId(exchangeId);
            auditRepository.save(o);

            //then re-submit the exchange to Rabbit MQ for the queue reader to pick up

            //TODO - re-post the transform(s) to RABBIT!!!


            //if we only want to re-queue the first exchange, then break out
            if (request.isFirstExchangeOnly()) {
                break;
            }
        }

        //we return the updated error state, so the UI can replace its old content
        JsonTransformErrorSummary ret = convertErrorStateToJson(errorState);

        clearLogbackMarkers();

        return Response
                .ok(ret)
                .build();
    }


    //get Errors
    //get Details about first exchange
    //re-queue first exchange
    //re-queue all exchanges

}
