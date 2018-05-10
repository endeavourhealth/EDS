package org.endeavourhealth.messagingapi.endpoints;

import com.google.common.base.Strings;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.eds.PatientLinkDalI;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.eds.models.PatientSearch;
import org.endeavourhealth.core.fhirStorage.FhirSerializationHelper;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.xml.QueryDocument.*;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

@Path("/subscriber")
public class SubscriberApi {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriberApi.class);

    private static final String FRAILTY_CODE = "289999999105";
    private static final String FRAILTY_TERM = "Potentially frail";
    private static final String SUBSCRIBER_SYSTEM_NAME = "JSON_API"; //"Subscriber_Rest_API";

    @GET
    @Path("/{resourceType}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"eds_read", "eds_read_only", "eds_read_write"})
    public Response getResources(@Context HttpServletRequest request,
                                @Context UriInfo uriInfo,
                                @ApiParam(value="Resource Type") @PathParam(value = "resourceType") String resourceTypeRequested,
                                @ApiParam(value="ODS Code") @HeaderParam(value = "OdsCode") String headerOdsCode) throws Exception{

        LOG.info("Subscriber API request received with resource type = [" + resourceTypeRequested + "] and ODS code [" + headerOdsCode + "]");

        MultivaluedMap<String, String> params = uriInfo.getQueryParameters();

        String subjectNhsNumber = null;
        String code = null;

        for (String key: params.keySet()) {
            String value = params.getFirst(key);
            LOG.info("Request parameter [" + key + "] = [" + value + "]");

            if (key.equalsIgnoreCase("subject")) {
                subjectNhsNumber = value;

            } else if (key.equalsIgnoreCase("code")) {
                code = value;

            } else {
                return createErrorResponse(OperationOutcome.IssueType.STRUCTURE, "Invalid parameter '" + key + "'");
            }
        }

        //validate all expected parameters and headers are there
        if (Strings.isNullOrEmpty(resourceTypeRequested)) {
            return createErrorResponse(OperationOutcome.IssueType.REQUIRED, "Missing resource type requested from URL path");
        }

        if (Strings.isNullOrEmpty(headerOdsCode)) {
            return createErrorResponse(OperationOutcome.IssueType.REQUIRED, "Missing OdsCode from request headers");
        }

        if (Strings.isNullOrEmpty(subjectNhsNumber)) {
            return createErrorResponse(OperationOutcome.IssueType.REQUIRED, "Missing subject parameter");
        }

        if (Strings.isNullOrEmpty(code)) {
            return createErrorResponse(OperationOutcome.IssueType.REQUIRED, "Missing code parameter");
        }

        //validate the parameters match what we're expecting
        if (!resourceTypeRequested.equalsIgnoreCase("flag")) {
            return createErrorResponse(OperationOutcome.IssueType.NOTSUPPORTED, "Only flag FHIR resource types can be requested");
        }

        if (!code.equalsIgnoreCase(FRAILTY_CODE)) {
            return createErrorResponse(OperationOutcome.IssueType.NOTSUPPORTED, "Only code " + FRAILTY_CODE + " can be requested");
        }

        //TODO - the ODS code of the requesting 111 service is passed as a parameter - we need to validate that the requesting user can request on behalf of that service

        //find the service the request is being made for
        ServiceDalI serviceDalI = DalProvider.factoryServiceDal();
        org.endeavourhealth.core.database.dal.admin.models.Service requestingService = serviceDalI.getByLocalIdentifier(headerOdsCode);
        if (requestingService == null) {
            return createErrorResponse(OperationOutcome.IssueType.VALUE, "Unknown requesting ODS code '" + headerOdsCode + "'");
        }
        UUID serviceId = requestingService.getId();

        UUID systemId = SystemHelper.findSystemUuid(requestingService, SUBSCRIBER_SYSTEM_NAME);
        if (systemId == null) {
            return createErrorResponse(OperationOutcome.IssueType.VALUE, "Requesting organisation not configured for " + SUBSCRIBER_SYSTEM_NAME);
        }

        //ensure the service is a valid subscriber to at least one protocol
        List<Protocol> protocols = getProtocolsForSubscriberService(serviceId.toString(), systemId.toString());
        if (protocols.isEmpty()) {
            return createErrorResponse(OperationOutcome.IssueType.VALUE, "No valid subscriber agreement found for requesting ODS code '" + headerOdsCode + "'");
        }

        for (Protocol protocol: protocols) {

            Set<String> publisherServiceIds = getPublisherServiceIdsForProtocol(protocol);

            //find patient
            PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
            List<PatientSearch> results = patientSearchDal.searchByNhsNumber(publisherServiceIds, subjectNhsNumber);

            if (results.isEmpty()) {
                continue;
            }

            //ensure all results, map to the same PERSON
            PatientLinkDalI patientLinkDal = DalProvider.factoryPatientLinkDal();

            String personId = null;
            for (PatientSearch result: results) {
                String patientId = result.getPatientId().toString();

                String thisPersonId = patientLinkDal.getPersonId(patientId);
                if (personId == null
                        || personId.equals(thisPersonId)) {
                    personId = thisPersonId;

                } else {
                    //this shouldn't happen while we continue to match patient-person on NHS number, but if that changes, this will be relevant
                    return createErrorResponse(OperationOutcome.IssueType.PROCESSING, "Multiple person records exist for patients with NHS number " + subjectNhsNumber);
                }
            }

            //calculate the flag (note that returning a NULL flag is a valid result if the patient isn't frail)
            Flag frailtyFlag = null;
            try {
                frailtyFlag = calculateFrailtyFlag(personId, publisherServiceIds);

            } catch (Exception ex) {
                //any exception from calculating the flag should be returned as a processing error
                String err = ex.getMessage();
                return createErrorResponse(OperationOutcome.IssueType.PROCESSING, err);
            }

            //the response object is a parameters resource, containing the
            //original request parameters, plus a special parameter containing the
            //response resource (this pattern is defined on the FHIR site)
            Parameters parameters = new Parameters();

            for (String key: params.keySet()) {
                String value = params.getFirst(key);

                Parameters.ParametersParameterComponent comp = parameters.addParameter();
                comp.setName(key);
                comp.setValue(new StringType(value));
            }

            Parameters.ParametersParameterComponent comp = parameters.addParameter();
            comp.setName("return");
            comp.setResource(frailtyFlag);

            String json = FhirSerializationHelper.serializeResource(parameters);
            LOG.info("Returning success response: " + json);

            return Response
                    .ok()
                    .entity(json)
                    .build();

        }

        //if we make it here, we simply didn't find the patient
        return createErrorResponse(OperationOutcome.IssueType.NOTFOUND, "No patient record could be found for NHS number " + subjectNhsNumber);
    }

    private Response createErrorResponse(OperationOutcome.IssueType issueType, String message) throws Exception {

        OperationOutcome outcome = new OperationOutcome();
        OperationOutcome.OperationOutcomeIssueComponent issue = outcome.addIssue();
        issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
        issue.setCode(issueType);

        CodeableConcept concept = new CodeableConcept();
        concept.setText(message);
        issue.setDetails(concept);

        String json = FhirSerializationHelper.serializeResource(outcome);
        LOG.info("Returning error response: " + json);

        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(json)
                .build();
    }

    /**
     * function to calculate the frailty flag of a person in Discovery, using only data held
     * by the publisher service IDs supplied. If a frailty flag can't be calculated, an exception
     * is thrown with the reason for the failure. If the patient is calculated to not be frail, null is returned.
     *
     * Note: this returns a Flag from the DSTU2 FHIR library, but this is compatible with STU3,
     * so receivers of this flag shouldn't need to worry about it being DSTU2.
     */
    private Flag calculateFrailtyFlag(String personId, Set<String> publisherServiceIds) throws Exception {

        //TODO - implement true calculation of frailty, returning "potentially frail", null (i.e. not frail) or throwing an exception on failure

        //there are four potential outcomes, so use the hashcode of the person ID to determine
        //which result should be returned, so it's consistent for NHS numbers
        long hashCode = UUID.fromString(personId).hashCode();
        long result = hashCode % 4;

        if (result == 0) {
            //error
            throw new Exception("Error calculating frailty flag");

        } else if (result == 1) {
            //not enough data to calculate accurately
            throw new Exception("Insufficient data to calculate frailty");

        } else if (result == 2) {
            //not frail
            return null;

        } else {
            //potentially frail
            CodeableConcept codeableConcept = new CodeableConcept();
            Coding coding = codeableConcept.addCoding();
            coding.setCode(FRAILTY_CODE);
            coding.setDisplay(FRAILTY_TERM);
            codeableConcept.setText(FRAILTY_TERM);

            Flag flag = new Flag();
            flag.setStatus(Flag.FlagStatus.ACTIVE);
            flag.setCode(codeableConcept);

            return flag;
        }
    }

    private static Set<String> getPublisherServiceIdsForProtocol(Protocol protocol) {
        Set<String> ret = new HashSet<>();

        for (ServiceContract serviceContract : protocol.getServiceContract()) {
            if (serviceContract.getType().equals(ServiceContractType.PUBLISHER)
                    && serviceContract.getActive() == ServiceContractActive.TRUE) {

                Service service = serviceContract.getService();
                ret.add(service.getUuid());
            }
        }

        return ret;
    }

    private static List<Protocol> getProtocolsForSubscriberService(String serviceUuid, String systemUuid) throws PipelineException {

        try {
            List<Protocol> ret = new ArrayList<>();

            List<LibraryItem> libraryItems = LibraryRepositoryHelper.getProtocolsByServiceId(serviceUuid, systemUuid);

            //the above fn will return is all protocols where the service and system are present, but we want to filter
            //that down to only ones where our service and system are an active publisher
            for (LibraryItem libraryItem: libraryItems) {
                Protocol protocol = libraryItem.getProtocol();
                if (protocol.getEnabled() == ProtocolEnabled.TRUE) { //added missing check

                    for (ServiceContract serviceContract : protocol.getServiceContract()) {
                        if (serviceContract.getType().equals(ServiceContractType.SUBSCRIBER)
                                && serviceContract.getService().getUuid().equals(serviceUuid)
                                && serviceContract.getSystem().getUuid().equals(systemUuid)
                                && serviceContract.getActive() == ServiceContractActive.TRUE) { //added missing check

                            ret.add(protocol);
                            break;
                        }
                    }
                }
            }

            return ret;

        } catch (Exception ex) {
            throw new PipelineException("Error getting protocols for service " + serviceUuid, ex);
        }
    }


}
