package org.endeavourhealth.messagingapi.endpoints;

import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.utility.MetricsHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.models.SubscriberApiAudit;
import org.endeavourhealth.core.database.dal.audit.models.SubscriberApiAuditHelper;
import org.endeavourhealth.core.database.dal.eds.PatientLinkDalI;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberResourceMappingDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.models.SubscriberId;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.fhirStorage.FhirSerializationHelper;
import org.endeavourhealth.core.subscribers.SubscriberHelper;
import org.endeavourhealth.transform.subscriber.SubscriberConfig;
import org.endeavourhealth.transform.subscriber.targetTables.SubscriberTableId;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

@Path("/subscriber")
public class SubscriberApiEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriberApiEndpoint.class);

    private static final String FRAILTY_CODE = "289999999105";
    private static final String FRAILTY_TERM = "Potentially frail";
    private static final String SUBSCRIBER_SYSTEM_NAME = "JSON_API"; //"Subscriber_Rest_API";
    private static final String FRAILTY_PROJECT_ID = "320baf45-37e2-4c8b-b7ee-27a9f877b95c"; //specific ID for live Frailty project

    private static final String METRIC_ERROR = "frailty-api.response-error";
    private static final String METRIC_NOT_FOUND = "frailty-api.response-not-found";
    private static final String METRIC_OK = "frailty-api.response-ok";
    private static final String METRIC_MS_DURATION = "frailty-api.ms-duration";

    private static final ServiceDalI serviceDal = DalProvider.factoryServiceDal();
    //private static final Map<String, String> mainSaltKeyCache = new ExpiringCache<>(1000 * 60 * 5); //cache for five mins
    //private static final Map<String, UUID> odsCodeToServiceIdCache = new ExpiringCache<>(1000 * 60 * 5); //cache for five mins
    //private static String lastDsmSlackMessage = null;


    @GET
    @Path("/{resourceType}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="SubscriberApiEndpoint.GET")
    @ResponseMetered(absolute = true, name="SubscriberApiEndpoint.GET")
    @RolesAllowed({"dds_api_read_only"})
    public Response getResources(@Context HttpServletRequest request,
                                 @Context SecurityContext sc,
                                @Context UriInfo uriInfo,
                                @ApiParam(value="Resource Type") @PathParam(value = "resourceType") String resourceTypeRequested,
                                @ApiParam(value="ODS Code") @HeaderParam(value = "OdsCode") String headerOdsCode,
                                @ApiParam(value="Auth Token") @HeaderParam(value = "Authorization") String headerAuthToken,
                                 @ApiParam(value="Project ID") @HeaderParam(value = "ProjectID") String headerProjectId) throws Exception{

        LOG.info("Subscriber API request received with resource type = [" + resourceTypeRequested + "] and ODS code [" + headerOdsCode + "]");

        UUID userUuid = SecurityUtils.getCurrentUserId(sc);
        SubscriberApiAudit audit = SubscriberApiAuditHelper.factory(userUuid, request, uriInfo);

        try {
            String subjectNhsNumber = null;
            String code = null;

            MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
            for (String key : params.keySet()) {
                String value = params.getFirst(key);
                LOG.info("Request parameter [" + key + "] = [" + value + "]");

                if (key.equalsIgnoreCase("subject")) {
                    subjectNhsNumber = value;

                } else if (key.equalsIgnoreCase("code")) {
                    code = value;

                } else {
                    return createErrorResponse(OperationOutcome.IssueType.STRUCTURE, "Invalid parameter '" + key + "'", audit);
                }
            }

            //validate all expected parameters and headers are there
            if (Strings.isNullOrEmpty(resourceTypeRequested)) {
                return createErrorResponse(OperationOutcome.IssueType.REQUIRED, "Missing resource type requested from URL path", audit);
            }

            //ODS code tells us who is making the request
            if (Strings.isNullOrEmpty(headerOdsCode)) {
                return createErrorResponse(OperationOutcome.IssueType.REQUIRED, "Missing OdsCode from request headers", audit);
            }

            //project ID is used in the DSM to get us to the protocol
            if (Strings.isNullOrEmpty(headerProjectId)) {
                //the need for the project ID was added AFTER the original implementation by the third party, so we've agreed
                //to hardcode it for now, to avoid them having to make and deploy changes
                headerProjectId = FRAILTY_PROJECT_ID;
                //return createErrorResponse(OperationOutcome.IssueType.REQUIRED, "Missing ProjectID from request headers", audit);
            }

            if (Strings.isNullOrEmpty(subjectNhsNumber)) {
                return createErrorResponse(OperationOutcome.IssueType.REQUIRED, "Missing subject parameter", audit);
            }

            if (Strings.isNullOrEmpty(code)) {
                return createErrorResponse(OperationOutcome.IssueType.REQUIRED, "Missing code parameter", audit);
            }

            //validate the parameters match what we're expecting
            if (!resourceTypeRequested.equalsIgnoreCase("flag")) {
                return createErrorResponse(OperationOutcome.IssueType.NOTSUPPORTED, "Only flag FHIR resource types can be requested", audit);
            }

            if (!code.equalsIgnoreCase(FRAILTY_CODE)) {
                return createErrorResponse(OperationOutcome.IssueType.NOTSUPPORTED, "Only code " + FRAILTY_CODE + " can be requested", audit);
            }

            //validate that the keycloak user (from the token) is permitted to make requests on behalf of the ODS code being requested for
            LOG.trace("Getting service IDs for security context");
            Set<String> permittedOdsCodes = SecurityUtils.getUserAllowedOrganisationIdsFromSecurityContext(sc);
            permittedOdsCodes = convertPermittedOdsCodes(permittedOdsCodes);
            LOG.trace("Got service IDs for security context " + permittedOdsCodes);

            //note that keyCloak may be configured with Service UUIDs or ODS codes
            if (!permittedOdsCodes.contains(headerOdsCode)) {
                LOG.error("Requesting ODS code " + headerOdsCode + " not in set of permitted ones");
                return createErrorResponse(OperationOutcome.IssueType.BUSINESSRULE, "You are not permitted to request for ODS code " + headerOdsCode, audit);
            }

            //ensure the service is a valid subscriber to at least one protocol
            LOG.trace("Getting publishing service IDs for requester " + headerOdsCode + " and project " + headerProjectId);
            Set<UUID> publisherServiceIds = null;
            try {
                publisherServiceIds = SubscriberHelper.findPublisherServiceIdsForSubscriber(headerOdsCode, headerProjectId);
            } catch (Exception ex) {
                //any exception from checking protocols the flag should be returned as a processing error
                String err = ex.getMessage();
                return createErrorResponse(OperationOutcome.IssueType.PROCESSING, err, audit);
            }

            //find patient records, filtering by the service IDs (gets map of patient UUID and service UUID)
            LOG.trace("Searching on NHS number against " + publisherServiceIds.size() + " service IDs");
            PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
            Map<UUID, UUID> patientSearchResults = patientSearchDal.findPatientIdsForNhsNumber(publisherServiceIds, subjectNhsNumber);
            LOG.trace("Done searching on NHS number, finding " + patientSearchResults.size() + " patient IDs");

            if (patientSearchResults.isEmpty()) {
                LOG.trace("Patient not found at services");
                return createErrorResponse(OperationOutcome.IssueType.NOTFOUND, "No patient record could be found for NHS number " + subjectNhsNumber, audit);
            }

            //calculate the flag (note that returning a NULL flag is a valid result if the patient isn't frail)
            try {

                Set<String> enterpriseEndpoints = getEnterpriseEndpoints(patientSearchResults);
                if (!enterpriseEndpoints.isEmpty()) {
                    LOG.info("Calculating frailty using " + enterpriseEndpoints);
                    Response response = null;

                    //test each endpoint until we get a result
                    for (String enterpriseEndpoint: enterpriseEndpoints) {
                        response = calculateFrailtyFlagLive(enterpriseEndpoint, patientSearchResults, params, subjectNhsNumber, audit);
                        if (response != null) {
                            break;
                        }
                    }

                    //if no result, then return a positive response without a flag
                    if (response == null) {
                        response = createSuccessResponse(null, params, audit);
                    }
                    return response;

                } else {
                    LOG.info("Using DUMMY mechanism to calculate Frailty");
                    return calculateFrailtyFlagDummy(patientSearchResults, params, audit);
                }

            } catch (Exception ex) {
                //any exception from calculating the flag should be returned as a processing error
                String err = ex.getMessage();
                return createErrorResponse(OperationOutcome.IssueType.PROCESSING, err, audit);
            }

        } finally {
            //save the audit, but if there's an error saving, catch and log here, so the API response isn't affected
            try {
                SubscriberApiAuditHelper.save(audit);

                //the above will calculate the duration on the audit object for us, so we can report it to graphite
                MetricsHelper.recordValue(METRIC_MS_DURATION, audit.getDurationMs());

            } catch (Exception ex) {
                LOG.error("Error saving audit", ex);
            }
        }
    }

    /**
     * Keycloak is configured to have service UUIDs rather than ODS codes, so we need to convert if any UUID is found
     */
    private static Set<String> convertPermittedOdsCodes(Set<String> set) throws Exception {

        LOG.trace("Received service IDs/ODS codes from Keycloak " + set);

        Set<String> ret = new HashSet<>();
        for (String s: set) {
            try {
                UUID serviceId = UUID.fromString(s);
                ServiceDalI serviceDal = DalProvider.factoryServiceDal();
                Service service = serviceDal.getById(serviceId);
                if (service == null) {
                    throw new Exception("Failed to look up ODS code for service UUID " + serviceId);
                }
                String odsCode = service.getLocalId();
                ret.add(odsCode);
                LOG.debug("Converted service ID " + serviceId + " to ODS code " + odsCode);

            } catch (IllegalArgumentException iae) {
                //if not a UUID then it's an ODS code already, so is OK
                ret.add(s);
            }
        }

        return ret;
    }

    /**
     * finds enterprise endpoint(s) that point to local (i.e. not remote) subscriber DBs that can be used
     * to search for the given patient
     */
    private static Set<String> getEnterpriseEndpoints(Map<UUID, UUID> patientSearchResults) throws Exception {

        Set<String> ret = new HashSet<>();

        ServiceDalI serviceDal = DalProvider.factoryServiceDal();

        //we have a config record telling us what subscribers we're allowed to use
        Set<String> permittedSubscribers = getPermittedSubscribers();

        //find all subscriber configs for each of the services that has a record for the person
        List<UUID> patientServiceIds = new ArrayList<>(patientSearchResults.values());
        for (UUID patientServiceId : patientServiceIds) {

            org.endeavourhealth.core.database.dal.admin.models.Service service = serviceDal.getById(patientServiceId);
            String odsCode = service.getLocalId();
            List<String> subscriberConfigNames = SubscriberHelper.getSubscriberConfigNamesForPublisher(null, patientServiceId, odsCode);

            for (String subscriberConfigName : subscriberConfigNames) {
                if (!permittedSubscribers.contains(subscriberConfigName)) {
                    continue;
                }

                ret.add(subscriberConfigName);
            }
        }

        return ret;
    }

    /*private Set<String> getEnterpriseEndpoints(org.endeavourhealth.core.database.dal.admin.models.Service service, UUID systemId) throws Exception {

        Set<String> ret = new HashSet<>();

        List<ServiceInterfaceEndpoint> serviceEndpoints = service.getEndpointsList();
        for (ServiceInterfaceEndpoint serviceEndpoint: serviceEndpoints) {
            if (serviceEndpoint.getSystemUuid().equals(systemId)) {
                String endpoint = serviceEndpoint.getEndpoint();
                ret.add(endpoint);
            }
        }

        return ret;
    }*/

    private Response createSuccessResponse(Flag frailtyFlag, MultivaluedMap<String, String> requestParams, SubscriberApiAudit audit) throws Exception {

        //the response object is a parameters resource, containing the
        //original request parameters, plus a special parameter containing the
        //response resource (this pattern is defined on the FHIR site)
        Parameters parameters = new Parameters();

        for (String key: requestParams.keySet()) {
            String value = requestParams.getFirst(key);

            Parameters.ParametersParameterComponent comp = parameters.addParameter();
            comp.setName(key);
            comp.setValue(new StringType(value));
        }

        Parameters.ParametersParameterComponent comp = parameters.addParameter();
        comp.setName("return");
        comp.setResource(frailtyFlag); //note that this may be null if not frail

        String json = FhirSerializationHelper.serializeResource(parameters);
        LOG.info("Returning success response: " + json);

        Response response = Response
                .ok()
                .entity(json)
                .build();

        SubscriberApiAuditHelper.updateAudit(audit, response, true);

        //update graphite
        MetricsHelper.recordEvent(METRIC_OK);

        return response;
    }

    private Response createErrorResponse(OperationOutcome.IssueType issueType, String message, SubscriberApiAudit audit) throws Exception {

        OperationOutcome outcome = new OperationOutcome();
        OperationOutcome.OperationOutcomeIssueComponent issue = outcome.addIssue();
        issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
        issue.setCode(issueType);

        CodeableConcept concept = new CodeableConcept();
        concept.setText(message);
        issue.setDetails(concept);

        String json = FhirSerializationHelper.serializeResource(outcome);
        LOG.info("Returning error response: " + json);

        Response response = Response
                .status(Response.Status.BAD_REQUEST)
                .entity(json)
                .build();

        SubscriberApiAuditHelper.updateAudit(audit, response, true);

        //send to graphite too
        if (issueType == OperationOutcome.IssueType.NOTFOUND) {
            MetricsHelper.recordEvent(METRIC_NOT_FOUND);
        } else {
            MetricsHelper.recordEvent(METRIC_ERROR);
        }

        return response;
    }

    private Response calculateFrailtyFlagLive(String subscriberConfigName, Map<UUID, UUID> patientAndServiceUuids,
                                              MultivaluedMap<String, String> requestParams, String subjectNhsNumber,
                                              SubscriberApiAudit audit) throws Exception {


        SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(subscriberConfigName);
        SubscriberResourceMappingDalI patientIdDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);

        Set<String> results = new HashSet<>();

        for (UUID patientUuid: patientAndServiceUuids.keySet()) {
            UUID serviceUuid = patientAndServiceUuids.get(patientUuid);
            LOG.trace("Testing patient " + patientUuid + " at " + serviceUuid);

            if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV1) {

                Long enterpriseId = patientIdDal.findEnterpriseIdOldWay(ResourceType.Patient.toString(), patientUuid.toString());
                LOG.trace("Found compass v1 enterprise ID " + enterpriseId);
                if (enterpriseId != null) {
                    String result = runCompassFrailtyQuery(subscriberConfig, enterpriseId);
                    LOG.trace("Got result " + result);
                    if (!Strings.isNullOrEmpty(result)) {
                        results.add(result);
                    }
                }

            } else if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV2) {

                String ref = ReferenceHelper.createResourceReference(ResourceType.Patient, patientUuid.toString());
                SubscriberId subscriberId = patientIdDal.findSubscriberId(SubscriberTableId.PATIENT.getId(), ref);
                LOG.trace("Found compass v2 enterprise ID " + subscriberId);
                if (subscriberId != null) {
                    String result = runCompassFrailtyQuery(subscriberConfig, subscriberId.getSubscriberId());
                    LOG.trace("Got result " + result);
                    if (!Strings.isNullOrEmpty(result)) {
                        results.add(result);
                    }
                }

            } else {
                throw new Exception("Unexpected subscriber database type " + subscriberConfig.getSubscriberType());
            }
        }

        if (results.contains("1_MILD")
                || results.contains("2_MODERATE")
                || results.contains("3_SEVERE")) {

            //if we got any of these back, then this is something we want to use as our result for the Flag
            CodeableConcept codeableConcept = new CodeableConcept();
            Coding coding = codeableConcept.addCoding();
            coding.setCode(FRAILTY_CODE);
            coding.setDisplay(FRAILTY_TERM);
            codeableConcept.setText(FRAILTY_TERM);

            Flag flag = new Flag();
            flag.setStatus(Flag.FlagStatus.ACTIVE);
            flag.setCode(codeableConcept);

            return createSuccessResponse(flag, requestParams, audit);

        } else {
            return null;
        }
    }

    private static String runCompassFrailtyQuery(SubscriberConfig subscriberConfig, long patientId) throws Exception {

        String queryConfigName = null;
        if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV1) {
            queryConfigName = "frailtyQueryCompassv1";

        } else if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV2) {
            queryConfigName = "frailtyQueryCompassv2";

        } else {
            throw new Exception("Unexpected subscriber database type " + subscriberConfig.getSubscriberType());
        }

        String sql = ConfigManager.getConfiguration(queryConfigName);
        if (Strings.isNullOrEmpty(sql)) {
            throw new Exception("Failed to find query for config name [" + queryConfigName + "]");
        }

        Connection connection = ConnectionManager.getSubscriberConnection(subscriberConfig.getSubscriberConfigName());
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, patientId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String result = rs.getString(1);
                return result;

            } else {
                return null;
            }

        } finally {
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
    }


    /*private Response calculateFrailtyFlagLive(String enterpriseEndpoint, Map<UUID, UUID> results, UriInfo uriInfo, MultivaluedMap<String, String> requestParams, String headerAuthToken, SubscriberApiAudit audit) throws Exception {

        //there are cases where we've had different dates of birth for the same patient, resulting
        //in multiple pseudo IDs, so we need to perform the test for EACH pseudo ID and return the "best" result (i.e. worst)
        Set<String> pseudoIds = new HashSet<>();

        PseudoIdDalI pseudoIdDal = DalProvider.factoryPseudoIdDal(enterpriseEndpoint);

        SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(enterpriseEndpoint);
        String mainSaltKeyName = findMainSaltKeyName(subscriberConfig);

        for (UUID patientUuid: results.keySet()) {
            //String pseudoId = pseudoIdDal.findPseudoIdOldWay(patientUuid.toString());
            String pseudoId = pseudoIdDal.findSubscriberPseudoId(patientUuid, mainSaltKeyName);
            if (!Strings.isNullOrEmpty(pseudoId)) {
                pseudoIds.add(pseudoId);
            }
        }

        //make the call to the Enterprise web server
        String serverUrl = subscriberConfig.getEnterpriseServerUrl();

        Map<String, String> hmResults = new HashMap<>();

        //work out the path we should call on the Enterprise server based on our path
        //this is only necessary because this endpoint may be called with either realm, so we need
        //to ensure the down-stream call uses the same realm, since we just pass through the original Keycloak token
        String enterprisePath = null;
        String requestPath = uriInfo.getRequestUri().toString();
        if (requestPath.indexOf("machine-api") > -1) {
            enterprisePath = "machine-api/cohort/getFrailty";
        } else {
            enterprisePath = "api/cohort/getFrailty";
        }

        Client client = ClientBuilder.newClient();

        for (String pseudoId: pseudoIds) {

            WebTarget target = client.target(serverUrl).path(enterprisePath);
            LOG.debug("Making call to " + target.getUri());
            target = target.queryParam("pseudoId", pseudoId);

            try {
                Response response = target
                        .request()
                        .header("Authorization", headerAuthToken)
                        .get();

                if (response.getStatus() == HttpStatus.SC_OK) {
                    String calculatedFrailty = response.readEntity(String.class);
                    LOG.debug("Received response [" + calculatedFrailty + "] for pseudo ID " + pseudoId);

                    hmResults.put(pseudoId, calculatedFrailty);

                } else {
                    String msg = "HTTP error " + response.getStatus() + " calling into frailty calculation service";

                    try {
                        String errResponse = response.readEntity(String.class);
                        if (!Strings.isNullOrEmpty(errResponse)) {
                            msg += " (" + errResponse + ")";
                        }
                    } catch (Exception ex) {
                        //do nothing
                    }

                    return createErrorResponse(OperationOutcome.IssueType.PROCESSING, msg, audit);
                }

            } catch (Exception ex) {
                String msg = ex.getMessage();
                return createErrorResponse(OperationOutcome.IssueType.EXCEPTION, msg, audit);
            }
        }

        //now check the results to find the most appropriate result to return
        for (String pseudoId: hmResults.keySet()) {
            String result = hmResults.get(pseudoId);

            if (result.equalsIgnoreCase("NONE")) {
                //if we got "none" back, then it's a valid result, but not one we're interested in

            } else if (result.equalsIgnoreCase("MILD")
                    || result.equalsIgnoreCase("MODERATE")
                    || result.equalsIgnoreCase("SEVERE")) {

                //if we got any of these back, then this is something we want to use as our result for the Flag
                CodeableConcept codeableConcept = new CodeableConcept();
                Coding coding = codeableConcept.addCoding();
                coding.setCode(FRAILTY_CODE);
                coding.setDisplay(FRAILTY_TERM);
                codeableConcept.setText(FRAILTY_TERM);

                Flag flag = new Flag();
                flag.setStatus(Flag.FlagStatus.ACTIVE);
                flag.setCode(codeableConcept);

                return createSuccessResponse(flag, requestParams, audit);

            } else {
                throw new Exception("Unsupported frailty calculated value [" + result + "] from pseudo ID " + pseudoId);
            }
        }

        //if we get here, we only got "none" back for all our results, so return a positive response without a flag
        return createSuccessResponse(null, requestParams, audit);
    }

    private String findMainSaltKeyName(SubscriberConfig subscriberConfig) throws Exception {

        if (!subscriberConfig.isPseudonymised()) {
            throw new Exception("Subscriber " + subscriberConfig.getSubscriberConfigName() + " isn't pseudonymised");
        }

        List<LinkDistributorConfig> pseudoIds = subscriberConfig.getPseudoSalts();
        if (pseudoIds == null || pseudoIds.isEmpty()) {
            throw new Exception("Subscriber " + subscriberConfig.getSubscriberConfigName() + " doesn't have any pseudo IDs set");
        }

        LinkDistributorConfig first = pseudoIds.get(0);
        return first.getSaltKeyName();
    }*/

    /**
     * function to calculate the frailty flag of a person in Discovery, using only data held
     * by the publisher service IDs supplied. If a frailty flag can't be calculated, an exception
     * is thrown with the reason for the failure. If the patient is calculated to not be frail, null is returned.
     *
     * Note: this returns a Flag from the DSTU2 FHIR library, but this is compatible with STU3,
     * so receivers of this flag shouldn't need to worry about it being DSTU2.
     */
    private Response calculateFrailtyFlagDummy(Map<UUID, UUID> searchResults, MultivaluedMap<String, String> requestParams, SubscriberApiAudit audit) throws Exception {

        //ensure all results, map to the same PERSON
        PatientLinkDalI patientLinkDal = DalProvider.factoryPatientLinkDal();

        String personId = null;
        for (UUID patientUuid: searchResults.keySet()) {
            String patientId = patientUuid.toString();

            String thisPersonId = patientLinkDal.getPersonId(patientId);
            if (personId == null
                    || personId.equals(thisPersonId)) {
                personId = thisPersonId;

            } else {
                //this shouldn't happen while we continue to match patient-person on NHS number, but if that changes, this will be relevant
                return createErrorResponse(OperationOutcome.IssueType.PROCESSING, "Multiple person records exist for patients with NHS number", audit);
            }
        }

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
            return createSuccessResponse(null, requestParams, audit);

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

            return createSuccessResponse(flag, requestParams, audit);
        }
    }

    private static Set<String> getPermittedSubscribers() throws Exception {

        Set<String> ret = new HashSet<>();

        JsonNode json = ConfigManager.getConfigurationAsJson("frailtyCompassDatabases");
        for (int i=0; i<json.size(); i++) {
            String subscriber = json.get(i).asText();
            ret.add(subscriber);
        }

        return ret;
    }
}
