package org.endeavourhealth.messagingapi.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import io.swagger.annotations.ApiParam;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.utility.ExpiringCache;
import org.endeavourhealth.common.utility.MetricsHelper;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.models.SubscriberApiAudit;
import org.endeavourhealth.core.database.dal.audit.models.SubscriberApiAuditHelper;
import org.endeavourhealth.core.database.dal.eds.PatientLinkDalI;
import org.endeavourhealth.core.database.dal.eds.PatientSearchDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberPersonMappingDalI;
import org.endeavourhealth.core.database.dal.usermanager.caching.UserCache;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.fhirStorage.FhirSerializationHelper;
import org.endeavourhealth.core.subscribers.SubscriberHelper;
import org.endeavourhealth.transform.subscriber.SubscriberConfig;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Path("/subscriber")
public class SubscriberApiEndpoint {
    //Initialize logger
    private static final Logger LOG = LoggerFactory.getLogger(SubscriberApiEndpoint.class);

    //This UM_APP_NAME to check access for request in user-management
    private static final String APP_NAME = "Subscriber API";

    //This is frailty project_code and It should be hardcoded if no project code passed in header for the flag_code 289999999105
    private static final String DEFAULT_FRAILTY_PROJECT_ID = "320baf45-37e2-4c8b-b7ee-27a9f877b95c";

    private static final String METRIC_ERROR = "subscriber-api.response-error";
    private static final String METRIC_NOT_FOUND = "subscriber-api.response-not-found";
    private static final String METRIC_OK = "subscriber-api.response-ok";
    private static final String METRIC_MS_DURATION = "subscriber-api.ms-duration";

    //Initialize slack alert timer
    private static ExpiringCache<String, Boolean> hmErrorAlertTimer = new ExpiringCache<>(60L * 60L * 1000L);

    @GET
    @Path("/{resourceType}")
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name = "SubscriberApiEndpoint.GET")
    @PermitAll
    public Response getResources(@Context HttpServletRequest request,
                                 @Context SecurityContext sc,
                                 @Context UriInfo uriInfo,
                                 @ApiParam(value = "Resource Type") @PathParam(value = "resourceType") String resourceTypeRequested,
                                 @ApiParam(value = "ODS Code") @HeaderParam(value = "OdsCode") String headerOdsCode,
                                 @ApiParam(value = "Auth Token") @HeaderParam(value = "Authorization") String headerAuthToken,
                                 @ApiParam(value = "Project ID") @HeaderParam(value = "ProjectID") String headerProjectIds) throws Exception {

        LOG.info("Subscriber API request received with resource type = [" + resourceTypeRequested + "] and ODS code [" + headerOdsCode + "]");

        //UserID from security context
        UUID userUuid = SecurityUtils.getCurrentUserId(sc);

        //Create audit  with userid,request,userinfo
        SubscriberApiAudit audit = SubscriberApiAuditHelper.factory(userUuid, request, uriInfo);

        try {
            String subjectNhsNumber = null;
            String code = null;

            //read subject ,code from Query parameters and validate it for null check
            MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
            for (String key : params.keySet()) {
                String value = params.getFirst(key);
                LOG.info("Request parameter [" + key + "] = [" + value + "]");

                if (key.equalsIgnoreCase("subject")) {
                    subjectNhsNumber = value;

                } else if (key.equalsIgnoreCase("code")) {

                    code = value;

                } else {
                    return createErrorResponse(OperationOutcome.IssueType.STRUCTURE, "Invalid parameter '" + key + "'", audit, code);
                }
            }
            String errorMessage = doNullCheck(code, subjectNhsNumber, resourceTypeRequested, headerOdsCode);
            if (null != errorMessage) {
                return createErrorResponse(OperationOutcome.IssueType.REQUIRED, errorMessage, audit, code);
            }

            //project ID check in header parameter
            //current redwood user not passing ProjectID for frailty code 289999999105 , code should support for it.other than code 289999999105 projectID is mandatory
            if (Strings.isNullOrEmpty(headerProjectIds)) {
                if (code.equalsIgnoreCase("289999999105")) {
                    LOG.trace("No project ID specified for old Frailty code so will use hard-coded project ID");
                    headerProjectIds = DEFAULT_FRAILTY_PROJECT_ID;
                } else {
                    return createErrorResponse(OperationOutcome.IssueType.REQUIRED, "Missing ProjectID from request headers", audit, code);
                }
            }

            // Read configuration details for given code, if not found raise an error
            String storedProcedureName, um_attribute, positive_response, negative_response;
            JsonNode config_subscriber_api = ConfigManager.getConfigurationAsJson("Flag:" + code);
            if (null != config_subscriber_api) {
                storedProcedureName = config_subscriber_api.get("sp_name").textValue();
                um_attribute = config_subscriber_api.get("um_attribute").textValue();
                positive_response = config_subscriber_api.get("positive_response").toString();
                negative_response = config_subscriber_api.get("negative_response").toString();
            } else {
                return createErrorResponse(OperationOutcome.IssueType.NOTSUPPORTED, "Invalid code " + code + " requested", audit, code);
            }

            //validate user with ProjectID
            String[] projectIds = headerProjectIds.split(Pattern.quote("|"));
            for (String projectId: projectIds) {
                String err = validateUserWithProjectID(userUuid, projectId, um_attribute, code);
                if (null != err) {
                    return createErrorResponse(OperationOutcome.IssueType.FORBIDDEN, err, audit, code);
                }
            }

            //check for given project_id, odscode there should be one subscriber  then fetch all publishers for project_id
            LOG.trace("Getting publishing service IDs for requester " + headerOdsCode + " and project " + headerProjectIds);
            Set<UUID> publisherServiceIds = new HashSet<>();
            for (String projectId: projectIds) {

                try {
                    Set<UUID> ids = SubscriberHelper.findPublisherServiceIdsForSubscriber(headerOdsCode, projectId);
                    publisherServiceIds.addAll(ids);

                } catch (Exception ex) {
                    //log any exception
                    LOG.error("Error getting publisher services for subscriber " + headerOdsCode + " and project " + projectId, ex);

                    //any exception should be returned as a processing error
                    String err = "Unexpected error finding publishers for project " + projectId + ": " + ex.getMessage();
                    return createErrorResponse(OperationOutcome.IssueType.PROCESSING, err, audit, code);
                }
            }
            LOG.trace("Found " + publisherServiceIds.size() + " publishers for project ID(s) " + headerProjectIds);

            if (publisherServiceIds.isEmpty()) {
                return createErrorResponse(OperationOutcome.IssueType.PROCESSING, "No publishers found for requesting organisation " + headerOdsCode + " and project " + headerProjectIds + " (either project has no publishers or requesting ODS code not valid)", audit, code);
            }

            //search in Patient_Search table for the service IDs and nhsNumber (gets map of patient UUID and service UUID)
            LOG.trace("Searching on NHS number against " + publisherServiceIds.size() + " service IDs");
            PatientSearchDalI patientSearchDal = DalProvider.factoryPatientSearchDal();
            Map<UUID, UUID> patientSearchResults = patientSearchDal.findPatientIdsForNhsNumber(publisherServiceIds, subjectNhsNumber);
            LOG.trace("Done searching on NHS number, finding " + patientSearchResults.size() + " patient IDs");

            if (patientSearchResults.isEmpty()) {
                LOG.trace("Patient not found at services");
                return createErrorResponse(OperationOutcome.IssueType.NOTFOUND, "No patient record could be found for NHS number " + subjectNhsNumber, audit, code);
            }

            //Search for the flag ,any exception from calculating the flag should be returned as a processing error
            try {
                //Find the SubscriberConfigNames
                Set<String> enterpriseEndpoints = getEnterpriseEndpoints(patientSearchResults);

                if (enterpriseEndpoints.isEmpty()) {
                    return createErrorResponse(OperationOutcome.IssueType.PROCESSING, "Failed to find suitable subscriber database for subject " + subjectNhsNumber, audit, code);
                } else {
                    LOG.info("Calculating Flag using " + enterpriseEndpoints);
                    Response response = null;
                    //test each endpoint until we get a result
                    for (String enterpriseEndpoint: enterpriseEndpoints) {
                        LOG.trace("Calculating Flag on " + enterpriseEndpoint + " using " + storedProcedureName);
                        response = calculateFlag(enterpriseEndpoint, patientSearchResults, subjectNhsNumber, audit, storedProcedureName, positive_response);
                        if (response != null) {
                            break;
                        }
                    }
                    //if no result, then return a positive response without a flag
                    if (response == null) {
                        response = createNegativeResponse(negative_response, subjectNhsNumber, audit);
                    }
                    return response;
                }
            } catch (Exception ex) {
                //any exception from calculating the flag should be returned as a processing error
                LOG.error("Error calculating flag for " + subjectNhsNumber + " and code " + code, ex);

                String err = ex.getMessage();
                return createErrorResponse(OperationOutcome.IssueType.PROCESSING, err, audit, code);
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

    private String doNullCheck(String code, String subjectNhsNumber, String resourceTypeRequested, String headerOdsCode) {
        if (Strings.isNullOrEmpty(subjectNhsNumber)) {
            return "Missing subject parameter";
        }
        if (Strings.isNullOrEmpty(code)) {
            return "Missing code parameter";
        }
        //validate  resourceType , resource type should be flag
        if (Strings.isNullOrEmpty(resourceTypeRequested)) {
            return "Missing resource type ";
        }
        if (!resourceTypeRequested.equalsIgnoreCase("flag")) {
            return "Only flag resource types can be requested";
        }

        //Ods code null check in header parameter
        if (Strings.isNullOrEmpty(headerOdsCode)) {
            return "Missing OdsCode from request headers";
        }

        return null;
    }

    private String validateUserWithProjectID(UUID userUuid, String projectId, String um_attribute, String code) {

        try {
            //validating user application policy , dsm project-application policy and policy attributes
            boolean canUseApi = UserCache.getUserProjectApplicationAccess(userUuid.toString(), projectId, APP_NAME);
            if (!canUseApi) {
                return "User not permitted to use DDS Subscriber API for project " + projectId;
            }
            boolean canUseApiForCode = UserCache.getUserProjectApplicationAttributeAccess(userUuid.toString(), projectId, APP_NAME, um_attribute);
            if (!canUseApiForCode) {
                return "User not permitted to request Subscriber API with code " + code + " for project " + projectId;
            }

            return null;

        } catch (Exception ex) {
            //log any exception
            LOG.error("Error validating user " + userUuid + " for project " + projectId + " and code " + code, ex);

            //any exception should be returned as a processing error
            String err = ex.getMessage();
            return "Unexpected error validating project " + projectId + ": " + ex.getMessage();
        }
    }

    /**
     * Method to finds enterprise endpoint(s) that point to local (i.e. not remote) subscriber DBs that can be used
     * to search for the given patient
     */
    private static Set<String> getEnterpriseEndpoints(Map<UUID, UUID> patientSearchResults) throws Exception {
        List<String> subscriberConfigList = new ArrayList<>();
        //find all subscriber configs for each of the services that has a record for the person
        List<UUID> patientServiceIds = new ArrayList<>(patientSearchResults.values());
        //we have a config record telling us what subscribers we're allowed to use
        Set<String> permittedSubscribers = getPermittedSubscribers();
        for (UUID patientServiceId : patientServiceIds) {
            List<String> subscriberConfigNames = SubscriberHelper.getSubscriberConfigNamesForPublisher(null, patientServiceId);
            for (String subscriberConfigName : subscriberConfigNames) {
                if (permittedSubscribers.contains(subscriberConfigName)) {
                    subscriberConfigList.add(subscriberConfigName);
                }
            }
        }
    /*
    Although the query will be run against all relevant subscriber databases to find a match,
    this sorting ensures that databases with more data for the NHS number will be searched first,
    potentially reducing the need to query against the lower ranked databases if an early match is found
    */
        return sortByRepetition(subscriberConfigList);

    }

    /**
     * Method to create nagative response based on the given template
     */
    private Response createNegativeResponse(String json, String nhsnumber, SubscriberApiAudit audit) throws Exception {

        json = json.replace("<subject>", nhsnumber);

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

    /**
     * Method to create positive response based on the given template
     */
    private Response createPositiveResponse(String json, String nhsnumber, SubscriberApiAudit audit, Map<String, String> results) throws Exception {

        json = json.replace("<subject>", nhsnumber);

        Set<String> keyset = results.keySet();
        for (String key : keyset) {
            json = json.replace("<" + key + ">", results.get(key));
        }


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

    /**
     * Method to create error response for validation errors and technical exceptions
     */

    private Response createErrorResponse(OperationOutcome.IssueType issueType, String message, SubscriberApiAudit audit, String code) throws Exception {

        code = (null != code ? code : "NaN");

        OperationOutcome outcome = new OperationOutcome();
        OperationOutcome.OperationOutcomeIssueComponent issue = outcome.addIssue();
        issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
        issue.setCode(issueType);

        CodeableConcept concept = new CodeableConcept();
        concept.setText(message);
        issue.setDetails(concept);

        String json = FhirSerializationHelper.serializeResource(outcome);

        //the Not Found response is expected, but if it's anything else send an alert and log the stack
        if (issueType != OperationOutcome.IssueType.NOTFOUND) {
            Exception ex = new Exception();
            ex.fillInStackTrace();
            LOG.error("Returning error response: " + json, ex);
            sendSlackAlert(new Exception(message), code);

        } else {
            LOG.error("Returning error response: " + json);
        }

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

    /**
     * Method to finds the Flag if found send positive response
     */
    private Response calculateFlag(String subscriberConfigName, Map<UUID, UUID> patientAndServiceUuids,
                                   String subjectNhsNumber, SubscriberApiAudit audit, String spname, String responsejsonTemplate) throws Exception {


        SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(subscriberConfigName);
        SubscriberPersonMappingDalI personMappingDal = DalProvider.factorySubscriberPersonMappingDal(subscriberConfigName);

        Set<String> personIdsChecked = new HashSet<>();

        for (UUID patientUuid : patientAndServiceUuids.keySet()) {
            //look up the internal DDS global person UUID from the patient UUID
            PatientLinkDalI patientLinkDal = DalProvider.factoryPatientLinkDal();
            String discoveryPersonId = patientLinkDal.getPersonId(patientUuid.toString());
            if (Strings.isNullOrEmpty(discoveryPersonId)) {
                continue;
            }

            //if we find multiple patient IDs they will all map to the same person ID so only check each person ID
            //once. There should be no reason by there would be multiple person IDs, but better to be safe and assume there might be
            if (personIdsChecked.contains(discoveryPersonId)) {
                continue;
            }
            personIdsChecked.add(discoveryPersonId);

            //look up the subscriber-specific person LONG for the person UUID
            List<Long> personIds = personMappingDal.findEnterprisePersonIdsForPersonId(discoveryPersonId);
            //if no person ID then the patient deosn't exist in that database, so skip checking it
            if (personIds.isEmpty()) {
                continue;
            }

            //the above function only returns one element in the list, so just get the first one out​​​​​​​
            Long personId = personIds.get(0);
            //then run the frailty/remote monitoring query, using this person ID
            Map<String, String> results = runCompassFlagQuery(subscriberConfig, personId, spname);
            if (null != results) {
                return createPositiveResponse(responsejsonTemplate, subjectNhsNumber, audit, results);
            }

        }//for

        return null;

    }

    /**
     * Method to send slack alert
     */
    private static void sendSlackAlert(Exception ex, String code) {
        Boolean alertAlreadySent = hmErrorAlertTimer.get(code);
        if (alertAlreadySent != null
                && alertAlreadySent.booleanValue()) {
            return;
        }
        String msg = "Error in Subscriber API for code " + code + " (this alert will only be sent every hour)";
        SlackHelper.sendSlackMessage(SlackHelper.Channel.MessagingApi, msg, ex);
        //add to map so we don't send again
        hmErrorAlertTimer.put(code, Boolean.TRUE);
    }

    /**
     * Method to execute the stored procedure for given personId and schema_name and procedure name
     */
    private static Map<String, String> runCompassFlagQuery(SubscriberConfig subscriberConfig, long personId, String spname) throws Exception {

        Map<String, String> rsMap = null;
        Connection connection = ConnectionManager.getSubscriberConnection(subscriberConfig.getSubscriberConfigName());
        CallableStatement stmt = null;

        try {
            LOG.trace("Calling " + spname + " (" + personId + ") for " + subscriberConfig.getSubscriberConfigName());

            String query = "{CALL " + spname + "(?)}";
            stmt = connection.prepareCall(query);
            stmt.setLong(1, personId);

            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();

            //WARNING: due to a change in the MySQL driver, the metadata contains the true column
            //names of the selected result set and ignores any specified aliases, so if there are problems
            //with column names, it's almost certainly going to be that (see https://bugs.mysql.com/bug.php?id=88949)
            //So change the SQL to not use aliases and change the flag JSON to use the true column names
            String[] columnNames = new String[meta.getColumnCount()];
            for (int i = 0; i < meta.getColumnCount(); i++) {
                String columnName = meta.getColumnName(i + 1);
                //LOG.trace("Column " + i + " = [" + columnName + "]");
                columnNames[i] = columnName;
            }

            if (rs.next()) {

                for (String columnName : columnNames) {
                    String value = rs.getString(columnName);

                    //if null is returned for any result, ignore it
                    if (value == null) {
                        continue;
                    }

                    //create the result map is still null
                    if (rsMap == null) {
                        rsMap = new HashMap<>();
                    }

                    rsMap.put(columnName, value);
                }
            }

            //all Subscriber API stored procedures should return 0 or 1 result set rows. If a second row is
            //present, then this SP isn't necessarily right
            if (rs.next()) {
                throw new Exception("More than one result returned for " + spname + " on " + subscriberConfig.getSubscriberConfigName());
            }

            return rsMap;

        } finally {
            if (stmt != null) {
                stmt.close();
            }
            connection.close();
        }
    }

    /**
     * Method to read all permitted subscriber schema (config names)
     */
    private static Set<String> getPermittedSubscribers() throws Exception {

        Set<String> ret = new HashSet<>();

        JsonNode json = ConfigManager.getConfigurationAsJson("SubscriberAPICompassDatabases");
        for (int i = 0; i < json.size(); i++) {
            String subscriber = json.get(i).asText();
            ret.add(subscriber);
        }
        return ret;
    }

    /**
     * Method to sort string list based on max repetition of element
     */
    private static Set<String> sortByRepetition(List<String> slist) {
        Set<String> set = new HashSet<>();
        if (slist != null) {
            List<String> sortedList = slist.stream().sorted().collect(Collectors.toList());
            sortedList.sort(Comparator.comparingInt(i -> Collections.frequency(sortedList, i)).reversed());
            set = new LinkedHashSet<String>(sortedList);
        }
        return set;

    }

}
