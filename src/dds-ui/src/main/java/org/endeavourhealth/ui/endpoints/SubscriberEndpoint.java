package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.common.utility.ExpiringObject;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.*;
import org.endeavourhealth.core.database.dal.datagenerator.models.RemoteFilingRecord;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.subscribers.SubscriberHelper;
import org.endeavourhealth.core.xml.TransformErrorSerializer;
import org.endeavourhealth.core.xml.TransformErrorUtility;
import org.endeavourhealth.core.xml.transformError.Arg;
import org.endeavourhealth.core.xml.transformError.Error;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.transform.subscriber.SubscriberConfig;
import org.endeavourhealth.ui.json.JsonSubscriberPublisherService;
import org.endeavourhealth.ui.json.JsonSubscriberPublisherSystem;
import org.endeavourhealth.ui.json.JsonSubscriberStatus;
import org.endeavourhealth.ui.utility.LastDataCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Path("/subscribers")
public class SubscriberEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriberEndpoint.class);

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Library);
    
    private static final ExpiringObject<Map<String, List<Service>>> cachedPublishersForSubscribers = new ExpiringObject<>(1000 * 60 * 5);
    private static final ExpiringObject<LastDataCache> cachedSubscriberData = new ExpiringObject<>(1000 * 60 * 1); //cache for one min

    private static final long MS_HOUR_DUR = 1000L * 60L * 60L;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="SubscriberEndpoint.subscribers")
    @Path("/subscribers")
    public Response getSubscribers(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Subscribers");

        List<JsonSubscriberStatus> ret = getSubscriberSummaryJson();

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="SubscriberEndpoint.subscriberDetail")
    @Path("/subscriberDetail")
    public Response getSubscriberDetail(@Context SecurityContext sc, @QueryParam("subscriberName") String subscriberName) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Subscriber Detail", "Subscriber", subscriberName);

        JsonSubscriberStatus ret = getSubscriberDetailJson(subscriberName);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }


    /**
     * gets top level details on the subscriber feeds
     */
    private List<JsonSubscriberStatus> getSubscriberSummaryJson() throws Exception {

        //retrieve all the data we need, using a short-duration cache
        LastDataCache lastData = cachedSubscriberData.get();
        if (lastData == null) {
            lastData = new LastDataCache(null);
            cachedSubscriberData.set(lastData);
        }

        LOG.trace("Getting subscriber summary");
        List<JsonSubscriberStatus> ret = getSubscribersStatus(false, lastData);
        LOG.trace("Finished getting subscriber summary");

        return ret;
    }

    private JsonSubscriberStatus getSubscriberDetailJson(String specificSubscriber) throws Exception {

        //for the detail view, we only want to load data specific to that subscriber, so don't bother using a cache
        LastDataCache lastData = new LastDataCache(specificSubscriber);

        List<JsonSubscriberStatus> ret = getSubscribersStatus(true, lastData);
        return ret.get(0); //since our last data cache was initialised with the specific subscriber, this will only have one element
    }

    private static List<JsonSubscriberStatus> getSubscribersStatus(boolean detailedOutput, LastDataCache lastData) throws Exception {

        List<JsonSubscriberStatus> ret = new ArrayList<>();

        //work out which publishers are linked to which subscriber
        Map<String, List<Service>> hmPublishers = findsPublishersPerSubscriber();

        //create a status object for each subscriber and populate with the basic details about it
        Set<String> subscriberNames = lastData.getSubscriberNames();
        for (String subscriberName: subscriberNames) {
            JsonSubscriberStatus status = new JsonSubscriberStatus();
            status.setName(subscriberName);

            //config properties
            populateBasicProperties(status, lastData);

            //technical details
            populateTechnicalDetails(status, lastData);

            //populate the publisher details
            List<Service> publishers = hmPublishers.get(subscriberName);
            if (publishers != null) { //some subscribers don't have publishers at first
                for (Service service: publishers) {
                    getPublisherStatus(service, status, lastData, detailedOutput);
                }
            }

            ret.add(status);
        }

        //return all the status objects
        return ret;
    }


    /**
     * populates the technical details of the subscriber object, such as database names
     */
    private static void populateTechnicalDetails(JsonSubscriberStatus status, LastDataCache lastData) throws Exception {

        String subscriberName = status.getName();

        //subscriber DB
        String subscriberConfigName = ConnectionManager.Db.Subscriber.getConfigNameIncludingInstance(subscriberName);
        JsonNode json = ConfigManager.getConfigurationAsJson(subscriberConfigName);
        if (json != null
                && json.has("url")) {
            String url = json.get("url").asText();

            String[] details = getDatabaseDetailsFromUrl(url);
            if (details != null) {
                status.setSubscriberDatabase(details[0]);
                status.setSubscriberDatabaseName(details[1]);
            }
        }

        //subscriber transform DB
        subscriberConfigName = ConnectionManager.Db.SubscriberTransform.getConfigNameIncludingInstance(subscriberName);
        json = ConfigManager.getConfigurationAsJson(subscriberConfigName);
        if (json != null
                && json.has("url")) {
            String url = json.get("url").asText();

            String[] details = getDatabaseDetailsFromUrl(url);
            if (details != null) {
                status.setSubscriberTransformDatabase(details[0]);
                status.setSubscriberTransformDatabaseName(details[1]);
            }
        }
    }

    /**
     * populates the return object with basic config settings about the subscriber
     */
    private static void populateBasicProperties(JsonSubscriberStatus status, LastDataCache lastData) {

        SubscriberConfig config = lastData.getSubscriberConfig(status.getName());

        String description = config.getDescription();
        String schema = "" + config.getSubscriberType();
        boolean isPseudonymised = config.isPseudonymised();
        boolean excludeTestPatients = config.isExcludeTestPatients();
        String excludeNhsNumberRegex = config.getExcludeNhsNumberRegex();
        boolean excludePatientsWithoutNhsNumber = config.isExcludePatientsWithoutNhsNumber();
        Integer remoteSubscriberId = config.getRemoteSubscriberId();
        String subscriberLocation = "" + config.getSubscriberLocation();
        String cohortType = "" + config.getCohortType();
        String cohortDesc = cohortType;
        if (config.getCohortType() == SubscriberConfig.CohortType.GpRegisteredAt
            || config.getCohortType() == SubscriberConfig.CohortType.ActiveOnlyGpRegisteredAt)  {
            cohortDesc += " [" + String.join(", ", config.getCohortGpServices()) + "]";
        }

        //basic properties of the subscriber
        status.setDescription(description);
        status.setSchema(schema);
        status.setDeidentified(isPseudonymised);
        status.setExcludeTestPatients(excludeTestPatients);
        status.setExcludeNhsNumberRegex(excludeNhsNumberRegex);
        status.setExcludePatientsWithoutNhsNumber(excludePatientsWithoutNhsNumber);
        status.setSubscriberLocation(subscriberLocation);
        status.setRemoteSubscriberId(remoteSubscriberId);
        status.setCohortType(cohortType);
        status.setCohortDesc(cohortDesc);

        if (config.getCohortGpServices() != null) {
            List<String> cohortMember = new ArrayList<>();
            for (String odsCode: config.getCohortGpServices()) {
                cohortMember.add(odsCode);
            }
            status.setCohort(cohortMember);
        }
    }


    private static void getPublisherStatus(Service service, JsonSubscriberStatus subscriberStatus, LastDataCache lastData, boolean detailedOutput) throws Exception {

        UUID serviceId = service.getId();
        List<ServiceInterfaceEndpoint> systemEndpoints = service.getEndpointsList();
        String subscriberName = subscriberStatus.getName();

        //add the publisher details if we've been asked for it
        JsonSubscriberPublisherService serviceStatus = null;
        if (detailedOutput) {
            serviceStatus = new JsonSubscriberPublisherService();
            subscriberStatus.getPublisherServices().add(serviceStatus);

            serviceStatus.setUuid(service.getId());
            serviceStatus.setOdsCode(service.getLocalId());
            serviceStatus.setName(service.getName());
            serviceStatus.setAlias(service.getAlias());
            serviceStatus.setTags(service.getTags());
        }

        //work out all the global things, that aren't specific to a subscriber (e.g. inbound status)
        boolean hasInboundError = false;
        Date firstLastReceived = null;
        Date firstLastProcessedIn = null;
        Date firstLastProcessedOut = null;
        Date firstLastWrittenOut = null;
        boolean hasOutboundError = false;

        for (ServiceInterfaceEndpoint endpoint: systemEndpoints) {
            UUID systemId = endpoint.getSystemUuid();

            LastDataReceived lastReceived = lastData.getLastReceived(serviceId, systemId);
            LastDataProcessed lastProcessedIn = lastData.getLastProcessedInbound(serviceId, systemId);
            LastDataToSubscriber lastProcessedOut = lastData.getLastProcessedOutbound(serviceId, systemId, subscriberName);
            LastDataInSubscriber lastWrittenOut = lastData.getLastWrittenOutbound(serviceId, systemId, subscriberName);

            //see if we've got an inbound error
            UUID inboundErrorExchangeId = lastData.getInboundErrorExchangeId(serviceId, systemId);
            if (inboundErrorExchangeId != null) {
                hasInboundError = true;
            }

            //for the summary display, we need to just have one set of dates per service, so use the earliest found out of all systems for the service
            if (lastReceived != null) {
                firstLastReceived = getEarliest(firstLastReceived, lastReceived.getExtractCutoff());
            }
            if (lastProcessedIn != null) {
                firstLastProcessedIn = getEarliest(firstLastProcessedIn, lastProcessedIn.getExtractCutoff());
            }

            if (lastProcessedOut != null) {
                firstLastProcessedOut = getEarliest(firstLastProcessedOut, lastProcessedOut.getExtractCutoff());
            }
            if (lastWrittenOut != null) {
                firstLastWrittenOut = getEarliest(firstLastWrittenOut, lastWrittenOut.getExtractCutoff());
            }

            //see if we've got an outbound error
            List<RemoteFilingRecord> outboundWriteErrors = lastData.getOutboundWriteErrors(serviceId, systemId, subscriberName);
            if (!outboundWriteErrors.isEmpty()) {
                hasOutboundError = true;
            }


            //add the detail if we need it
            if (detailedOutput) {
                JsonSubscriberPublisherSystem systemStatus = new JsonSubscriberPublisherSystem();
                serviceStatus.getSystemStatus().add(systemStatus);

                String systemDesc = ServiceEndpoint.findSoftwareDescForSystem(systemId);
                String publisherMode = endpoint.getEndpoint();

                systemStatus.setUuid(systemId);
                systemStatus.setName(systemDesc);
                systemStatus.setPublisherMode(publisherMode); //bulk/regular etc.

                if (lastReceived != null) {
                    systemStatus.setLastReceivedTimestamp(lastReceived.getReceivedDate());
                    systemStatus.setLastReceivedExtractDate(lastReceived.getExtractDate());
                    systemStatus.setLastReceivedExtractCutoff(lastReceived.getExtractCutoff());
                }

                if (lastProcessedIn != null) {
                    systemStatus.setLastProcessedInTimestamp(lastProcessedIn.getProcessedDate());
                    systemStatus.setLastProcessedInExtractDate(lastProcessedIn.getExtractDate());
                    systemStatus.setLastProcessedInExtractCutoff(lastProcessedIn.getExtractCutoff());
                }

                if (lastProcessedOut != null) {
                    systemStatus.setLastProcessedOutTimestamp(lastProcessedOut.getSentDate());
                    systemStatus.setLastProcessedOutExtractDate(lastProcessedOut.getExtractDate());
                    systemStatus.setLastProcessedOutExtractCutoff(lastProcessedOut.getExtractCutoff());
                }

                if (lastWrittenOut != null) {
                    systemStatus.setLastWriteOutTimestamp(lastWrittenOut.getWrittenDate());
                    systemStatus.setLastWriteOutExtractDate(lastWrittenOut.getExtractDate());
                    systemStatus.setLastWriteOutExtractCutoff(lastWrittenOut.getExtractCutoff());
                }

                //provide inbound error details
                if (inboundErrorExchangeId != null) {
                    systemStatus.setProcessingInError(true);

                    String errorMessage = findErrorMessage(serviceId, systemId, inboundErrorExchangeId);
                    systemStatus.setProcessingInErrorMessage(errorMessage);
                }

                //provide outbound error details
                if (!outboundWriteErrors.isEmpty()) {
                    systemStatus.setWriteOutError(true);

                    String errorMessage = findErrorMessage(outboundWriteErrors);
                    systemStatus.setWriteOutErrorMessage(errorMessage);
                }
            }
        }

        //update the status object with what we've found
        subscriberStatus.incrementNumPublishers();

        //NOTE: in all the below, it counts "one day" as being 25 hours.
        //This is intentional - DDS receives a lot of extracts at ABOUT the same time each day, so this gives
        //the display a bit of wiggle room before starting to show everything as being late.

        //receipt
        long msDiffReceipt = getMsDiff(new Date(), firstLastReceived);
        if (msDiffReceipt <= MS_HOUR_DUR * 25L) {
            subscriberStatus.incrementReceivedOneDay();

        } else if (msDiffReceipt <= MS_HOUR_DUR * 25L * 2) {
            subscriberStatus.incrementReceivedTwoDays();

        } else {
            subscriberStatus.incrementReceivedMoreDays();
        }

        if (hasInboundError) {
            subscriberStatus.incrementInboundError();
        }

        //inbound transform
        long msDiffInbound = getMsDiff(firstLastReceived, firstLastProcessedIn);
        //when bypassing the Messaging API to test transforms in non-production environments, we can end up with a negative, so
        //treat any negative number as being zero
        if (msDiffInbound <= 0) {
            subscriberStatus.incrementInboundUpToDate();

        } else if (msDiffInbound <= MS_HOUR_DUR * 25L) {
            subscriberStatus.incrementInboundOneDay();

        } else {
            subscriberStatus.incrementInboundMoreDays();
        }

        //outbound transform
        //base this on the difference between the inbound transform and this stage, otherwise we end up showing errors everywhere due to an inbound error
        long msDiffOutbound = getMsDiff(firstLastProcessedIn, firstLastProcessedOut);
        //long msDiffOutbound = getMsDiff(firstLastReceived, firstLastProcessedOut);
        if (msDiffOutbound <= 0) {
            subscriberStatus.incrementOutboundUpToDate();

        } else if (msDiffOutbound <= MS_HOUR_DUR * 25L) {
            subscriberStatus.incrementOutboundOneDay();

        } else {
            subscriberStatus.incrementOutboundMoreDays();
        }

        //outbound writing
        //base this on the difference between the inbound transform and this stage, otherwise we end up showing errors everywhere due to an inbound error
        long msDiffWriteOut = getMsDiff(firstLastProcessedOut, firstLastWrittenOut);
        //long msDiffWriteOut = getMsDiff(firstLastReceived, firstLastWrittenOut);
        if (msDiffWriteOut <= 0) {
            subscriberStatus.incrementWriteOutUpToDate();

        } else if (msDiffWriteOut <= MS_HOUR_DUR * 25L) {
            subscriberStatus.incrementWriteOutOneDay();

        } else {
            subscriberStatus.incrementWriteOutMoreDays();
        }

        if (hasOutboundError) {
            subscriberStatus.incrementOutboundError();
        }
    }

    /**
     * generates a suitable error message from the given outbound write errors
     */
    private static String findErrorMessage(List<RemoteFilingRecord> outboundWriteErrors) {

        StringBuilder sb = new StringBuilder();

        //add the details of the most recent
        RemoteFilingRecord latest = outboundWriteErrors.get(0);
        Date dtFailed = latest.getDtFileFilingAttempted();
        String message = latest.getFilingFailureMessage();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        sb.append("Latest error ");
        sb.append(dateFormat.format(dtFailed));
        sb.append(": ");
        sb.append(message);

        //work out a suitable size. If we've retrieved our max, then all we know is that there's at least that number
        int size = outboundWriteErrors.size();
        if (size == LastDataCache.MAX_OUTBOUND_WRITE_ERRORS_CONST) {
            sb.append(" (" + (size-1) + "+ errors)");
        } else if (size == 1) {
            sb.append(" (1 error)");
        } else {
            sb.append(" (" + size + " errors)");
        }

        return sb.toString();
    }

    /**
     * attempts to find an error message in the inbound transform for the given details
     */
    private static String findErrorMessage(UUID serviceId, UUID systemId, UUID exchangeId) throws Exception {

        ExchangeDalI dal = DalProvider.factoryExchangeDal();
        ExchangeTransformAudit transformAudit = dal.getMostRecentExchangeTransform(serviceId, systemId, exchangeId);
        if (transformAudit == null) {
            return "<<UNKNOWN ERROR>>";
        }

        //the error XML will contain the detail of the exception, but will be null if we've re-queued the exchange and it's mid-way through being processed again
        if (Strings.isNullOrEmpty(transformAudit.getErrorXml())) {
            //so retrieve all audits and find the last one with an error XML
            List<ExchangeTransformAudit> allTransformAudits = dal.getAllExchangeTransformAudits(serviceId, systemId, exchangeId);
            for (int i=allTransformAudits.size()-1; i>=0; i--) {
                ExchangeTransformAudit a = allTransformAudits.get(i);
                if (!Strings.isNullOrEmpty(a.getErrorXml())) {
                    transformAudit = a;
                    break;
                }
            }

            //if still null, then something is wrong
            if (Strings.isNullOrEmpty(transformAudit.getErrorXml())) {
                return "<<NO ERROR DATA>>";
            }
        }

        String errorXml = transformAudit.getErrorXml();
        TransformError errorWrapper = TransformErrorSerializer.readFromXml(errorXml);
        List<Error> errors = errorWrapper.getError();
        if (errors == null || errors.isEmpty()) {
            return "<<NO ERRORS FOUND>>";
        }

        org.endeavourhealth.core.xml.transformError.Error firstError = errors.get(0);
        org.endeavourhealth.core.xml.transformError.Exception exception = firstError.getException();
        if (exception == null) {

            //if an error with a previous exchange was fixed, and we've already processed that exchange, we might be processing one
            //of the subsequent exchanges, that won't have the original exception on. So try to generate something meaningful in this case.
            List<Arg> args = firstError.getArg();
            for (Arg arg: args) {
                if (arg.getName().equals(TransformErrorUtility.ARG_WAITING)) {
                    return "Recovering from previous error";
                }
            }

            LOG.error("Failed to find error message in audit data from " + transformAudit.getStarted() + " for service " + serviceId + " and exchange " + exchangeId);
            return "<<FAILED TO GET ERROR MESSAGE>>";

        } else {
            String msg = exception.getMessage();
            LOG.trace("Found exception msg [" + msg + "] for service " + serviceId + " and system " + systemId + " and exchange " + exchangeId);
            return msg;
        }
    }

    /**
     * returns the earliest non-null date out of two passed in
     */
    private static Date getEarliest(Date d1, Date d2) {
        if (d1 == null && d2 == null) {
            return null;
            
        } else if (d1 != null && d2 != null) {
            if (d1.before(d2)) {
                return d1;
            } else {
                return d2;
            }
            
        } else if (d1 != null) {
            return d1;
            
        } else {
            return d2;
        }
    }
    
    private static long getMsDiff(Date dtReceived, Date dtProcessed) {

        //if we don't have a "from" date, then by definition the "to" date is up to date
        if (dtReceived == null) {
            return 0;
        }

        //if we don't have a "to" date then we're very behind
        if (dtProcessed == null) {
            return Integer.MAX_VALUE;
        }

        //if we have both dates, then work out the actual number of days difference
        long msFrom = dtProcessed.getTime();
        long msTo = dtReceived.getTime();
        return msTo - msFrom;
    }




    /**
     *
     * haven't found a good way to get the elements out of this without actually making a connection, but this
     * seems to work for MySQL and SQL Server
     *
     * e.g.
     *  jdbc:mysql://localhost:3306/publisher_common?useSSL=false
     *  jdbc:sqlserver://localhost;database=compass_v1_pseudo;integratedSecurity=false
     */
    private static String[] getDatabaseDetailsFromUrl(String url) {

        try {

            String databaseServer = null;
            String databaseName = null;

            int indexPrefix = url.indexOf("//");
            if (indexPrefix == -1) {
                LOG.error("Failed to find // in url " + url);

            } else {
                String prefix = url.substring(0, indexPrefix);
                url = url.substring(indexPrefix + 2);

                if (prefix.contains("mysql")) {
                    int slashIndex = url.indexOf("/");
                    String beforeSlash = url.substring(0, slashIndex);
                    String afterSlash = url.substring(slashIndex+1);

                    int colonIndex = beforeSlash.indexOf(":");
                    if (colonIndex == -1) {
                        databaseServer = beforeSlash;
                    } else {
                        databaseServer = beforeSlash.substring(0, colonIndex);
                    }

                    int questionIndex = afterSlash.indexOf("?");
                    if (questionIndex == -1) {
                        databaseName = afterSlash;
                    } else {
                        databaseName = afterSlash.substring(0, questionIndex);
                    }

                } else if (prefix.contains("sqlserver")) {

                    String[] toks = url.split(";");
                    databaseServer = toks[0];

                    for (String tok: toks) {
                        if (tok.startsWith("database=")) {
                            int eqIndex = tok.indexOf("=");
                            databaseName = tok.substring(eqIndex+1);
                        }
                    }

                } else {
                    LOG.error("Unsupported database URL prefix [" + prefix + "]");
                }

            }

            return new String[]{databaseServer, databaseName};

        } catch (Exception ex) {
            LOG.error("Failed to parse database url " + url, ex);
            return null;
        }
    }

    private static Map<String, List<Service>> findsPublishersPerSubscriber() throws Exception {
        
        //cache this set for five minutes, since it will rarely change
        Map<String, List<Service>> ret = cachedPublishersForSubscribers.get();
        
        if (ret == null) {
            ret = new HashMap<>();

            ServiceDalI serviceDal = DalProvider.factoryServiceDal();
            List<Service> all = serviceDal.getAll();
            for (Service service : all) {
                List<String> subs = SubscriberHelper.getSubscriberConfigNamesForPublisher(null, service.getId());
                for (String sub : subs) {

                    List<Service> l = ret.get(sub);
                    if (l == null) {
                        l = new ArrayList<>();
                        ret.put(sub, l);
                    }
                    l.add(service);
                }
            }

            cachedPublishersForSubscribers.set(ret);
        }

        return ret;
    }


}
