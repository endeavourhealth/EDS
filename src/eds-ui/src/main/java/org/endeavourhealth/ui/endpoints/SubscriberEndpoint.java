package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.subscribers.SubscriberHelper;
import org.endeavourhealth.core.xml.TransformErrorSerializer;
import org.endeavourhealth.core.xml.transformError.Error;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.transform.subscriber.SubscriberConfig;
import org.endeavourhealth.ui.utility.LastDataCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;

@Path("/subscribers")
public class SubscriberEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriberEndpoint.class);

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Library);
    
    private static final ExpiringObject<Map<String, List<Service>>> cachedPublishersForSubscribers = new ExpiringObject<>(1000 * 60 * 5); 

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed(absolute = true, name="SubscriberEndpoint.subscribers")
    @Path("/subscribers")
    public Response getSubscribers(@Context SecurityContext sc) throws Exception {
        super.setLogbackMarkers(sc);

        userAudit.save(SecurityUtils.getCurrentUserId(sc), getOrganisationUuidFromToken(sc), AuditAction.Load, "Get Subscribers");

        String ret = getSubscriberSummaryJson();

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

        String ret = getSubscriberDetailJson(subscriberName);

        clearLogbackMarkers();

        return Response
                .ok()
                .entity(ret)
                .build();
    }


    /**
     * gets top level details on the subscriber feeds
     */
    private String getSubscriberSummaryJson() throws Exception {

        Map<String, List<Service>> hmPublishers = findPublishersBySubscriber();
        LastDataCache lastData = LastDataCache.getLastData();

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode root = new ArrayNode(mapper.getNodeFactory());

        Map<String, String> subscriberMap = ConfigManager.getConfigurations("db_subscriber");
        for (String subscriberName: subscriberMap.keySet()) {

            ObjectNode obj = root.addObject();
            populateSubscriberNode(obj, subscriberName, false, hmPublishers, lastData);
        }

        return mapper.writeValueAsString(root);
    }

    private String getSubscriberDetailJson(String specificSubscriber) throws Exception {

        Map<String, List<Service>> hmPublishers = findPublishersBySubscriber();
        LastDataCache lastData = LastDataCache.getLastData();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = new ObjectNode(mapper.getNodeFactory());

        populateSubscriberNode(root, specificSubscriber, true, hmPublishers, lastData);

        return mapper.writeValueAsString(root);
    }

    private void populateSubscriberNode(ObjectNode obj, String subscriberName, boolean detailedOutput,
                                        Map<String, List<Service>> hmPublishers, LastDataCache lastData) throws Exception {


        //don't bother messing with the JSON in the above map, just re-get using the proper object class
        SubscriberConfig config = SubscriberConfig.readFromConfig(subscriberName);
        String description = config.getDescription();
        String schema = "" + config.getSubscriberType();
        boolean isPseudonymised = config.isPseudonymised();
        boolean excludeTestPatients = config.isExcludeTestPatients();
        String excludeNhsNumberRegex = config.getExcludeNhsNumberRegex();
        boolean excludePatientsWithoutNhsNumber = config.isExcludePatientsWithoutNhsNumber();
        Integer remoteSubscriberId = config.getRemoteSubscriberId();
        String subscriberLocation = "" + config.getSubscriberLocation();
        String cohortType = "" + config.getCohortType();


        obj.put("name", subscriberName);
        obj.put("description", description);
        obj.put("schema", schema);
        obj.put("deidentified", isPseudonymised);
        obj.put("excludeTestPatients", excludeTestPatients);
        obj.put("excludeNhsNumberRegex", excludeNhsNumberRegex);
        obj.put("excludePatientsWithoutNhsNumber", excludePatientsWithoutNhsNumber);
        obj.put("subscriberLocation", subscriberLocation);
        obj.put("remoteSubscriberId", remoteSubscriberId);
        obj.put("cohortType", cohortType);

        if (config.getCohortGpServices() != null) {
            ArrayNode arr = obj.putArray("cohort");
            for (String odsCode: config.getCohortGpServices()) {
                arr.add(odsCode);
            }
        }

        //get details about the databases, since this is useful sometimes
        getDatabaseDetails(obj, subscriberName);

        List<Service> servicesToSubscriber = hmPublishers.get(subscriberName);
        getPublisherStatus(obj, subscriberName, servicesToSubscriber, lastData, detailedOutput);
    }

    private void getPublisherStatus(ObjectNode obj,
                                    String subscriberName, 
                                    List<Service> services,
                                    LastDataCache lastData,
                                    boolean detailedOutput) throws Exception {

        //this might be null if we've got no publishers
        if (services == null) {
            services = new ArrayList<>();
        }

        //number publishers up to date with inbound processing
        int inboundUpToDate = 0;
        int inboundOneDay = 0;
        int inboundMoreDays = 0;
        int outboundUpToDate = 0;
        int outboundOneDay = 0;
        int outboundMoreDays = 0;

        ArrayNode arr = null;
        if (detailedOutput) {
            arr = obj.putArray("publisherServices");
        }

        for (Service service: services) {
            UUID serviceId = service.getId();

            //add the publisher details if we've been asked for it
            ArrayNode systemsNode = null;
            if (detailedOutput) {
                ObjectNode serviceNode = arr.addObject();

                serviceNode.put("uuid", service.getId().toString());
                serviceNode.put("odsCode", service.getLocalId());
                serviceNode.put("name", service.getName());
                serviceNode.put("alias", service.getAlias());

                ObjectNode tagsNode = serviceNode.putObject("tags");
                Map<String, String> tags = service.getTags();
                for (String tag: tags.keySet()) {
                    String tagValue = tags.get(tag);
                    tagsNode.put(tag, tagValue);
                }

                systemsNode = serviceNode.putArray("systemStatus");
            }

            Date firstLastReceived = null;
            Date firstLastProcessedIn = null;
            Date firstLastProcessedOut = null;

            List<ServiceInterfaceEndpoint> systemEndpoints = service.getEndpointsList();
            for (ServiceInterfaceEndpoint endpoint: systemEndpoints) {
                UUID systemId = endpoint.getSystemUuid();

                LastDataReceived lastReceived = lastData.getLastReceived(serviceId, systemId);
                LastDataProcessed lastProcessedIn = lastData.getLastProcessedInbound(serviceId, systemId);
                LastDataToSubscriber lastProcessedOut = lastData.getLastProcessedOutbound(serviceId, systemId, subscriberName);

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

                //add the detail if we need it
                if (detailedOutput) {

                    String systemDesc = ServiceEndpoint.findSoftwareDescForSystem(systemId);
                    String publisherMode = endpoint.getEndpoint();

                    ObjectNode systemNode = systemsNode.addObject();
                    systemNode.put("uuid", systemId.toString());
                    systemNode.put("name", systemDesc);
                    systemNode.put("publisherMode", publisherMode); //bulk/regular etc.

                    if (lastReceived != null) {
                        systemNode.put("lastReceivedExtract", lastReceived.getReceivedDate().getTime());
                        systemNode.put("lastReceivedExtractDate", lastReceived.getExtractDate().getTime());
                        systemNode.put("lastReceivedExtractCutoff", lastReceived.getExtractCutoff().getTime());
                    }

                    if (lastProcessedIn != null) {
                        systemNode.put("lastProcessedInExtract", lastProcessedIn.getProcessedDate().getTime());
                        systemNode.put("lastProcessedInExtractDate", lastProcessedIn.getExtractDate().getTime());
                        systemNode.put("lastProcessedInExtractCutoff", lastProcessedIn.getExtractCutoff().getTime());
                    }

                    if (lastProcessedOut != null) {
                        systemNode.put("lastProcessedOutExtract", lastProcessedOut.getSentDate().getTime());
                        systemNode.put("lastProcessedOutExtractDate", lastProcessedOut.getExtractDate().getTime());
                        systemNode.put("lastProcessedOutExtractCutoff", lastProcessedOut.getExtractCutoff().getTime());
                    }

                    //work out if we've got an inbound error
                    UUID inboundErrorExchangeId = lastData.getInboundErrorExchangeId(serviceId, systemId);
                    if (inboundErrorExchangeId != null) {
                        systemNode.put("processingInError", true);

                        String errorMessage = findErrorMessage(serviceId, systemId, inboundErrorExchangeId);
                        systemNode.put("processingInErrorMessage", errorMessage);
                    }
                }
            }

            //inbound status
            int daysDiffInbound = getDaysDiff(firstLastReceived, firstLastProcessedIn);
            if (daysDiffInbound == 0) {
                inboundUpToDate ++;

            } else if (daysDiffInbound <= 1) {
                inboundOneDay++;

            } else {
                inboundMoreDays++;
            }

            //outbound
            //base outbound on the data received
            //int daysDiffOutbound = getDaysDiff(firstLastProcessedIn, firstLastProcessedOut);
            int daysDiffOutbound = getDaysDiff(firstLastReceived, firstLastProcessedOut);
            if (daysDiffOutbound == 0) {
                outboundUpToDate ++;

            } else if (daysDiffOutbound <= 1) {
                outboundOneDay++;

            } else {
                outboundMoreDays++;
            }
        }

        //total number of publishers
        obj.put("numPublishers", services.size());
        obj.put("inboundUpToDate", inboundUpToDate);
        obj.put("inboundOneDay", inboundOneDay);
        obj.put("inboundMoreDays", inboundMoreDays);
        obj.put("outboundUpToDate", outboundUpToDate);
        obj.put("outboundOneDay", outboundOneDay);
        obj.put("outboundMoreDays", outboundMoreDays);
    }

    /**
     * attempts to find an error message in the inbound transform for the given details
     */
    private String findErrorMessage(UUID serviceId, UUID systemId, UUID exchangeId) throws Exception {

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
        String msg = exception.getMessage();
        LOG.trace("Found exception msg [" + msg + "] for service " + serviceId + " and system " + systemId + " and exchange " + exchangeId);
        return msg;
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
    
    private int getDaysDiff(Date dtReceived, Date dtProcessed) {

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
        long msDiff = msTo - msFrom;
        return (int)(msDiff / (1000 * 60 * 60 * 24));
    }



    /**
     * gets the database server and name for our subscriber DBs
     */
    private void getDatabaseDetails(ObjectNode obj, String subscriberName) throws Exception {

        //subscriber DB
        String subscriberConfigName = ConnectionManager.Db.Subscriber.getConfigNameIncludingInstance(subscriberName);
        JsonNode json = ConfigManager.getConfigurationAsJson(subscriberConfigName);
        if (json != null
                && json.has("url")) {
            String url = json.get("url").asText();

            String[] details = getDatabaseDetailsFromUrl(url);
            if (details != null) {
                obj.put("subscriberDatabase", details[0]);
                obj.put("subscriberDatabaseName", details[1]);
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
                obj.put("subscriberTransformDatabase", details[0]);
                obj.put("subscriberTransformDatabaseName", details[1]);
            }
        }
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
    private String[] getDatabaseDetailsFromUrl(String url) {

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

    private Map<String, List<Service>> findPublishersBySubscriber() throws Exception {
        
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
