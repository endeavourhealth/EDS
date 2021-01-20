package org.endeavourhealth.ui.endpoints;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.security.SecurityUtils;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.UserAuditDalI;
import org.endeavourhealth.core.database.dal.audit.models.*;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.subscribers.SubscriberHelper;
import org.endeavourhealth.coreui.endpoints.AbstractEndpoint;
import org.endeavourhealth.transform.subscriber.SubscriberConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.*;

@Path("/subscribers")
public class SubscriberEndpoint extends AbstractEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriberEndpoint.class);

    private static final UserAuditDalI userAudit = DalProvider.factoryUserAuditDal(AuditModule.EdsUiModule.Library);

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

    /**
     * gets top level details on the subscriber feeds
     */
    private String getSubscriberSummaryJson() throws Exception {

        Map<String, List<Service>> hmPublishers = findPublishers();

        Map<UUID, Set<LastDataReceived>> hmStatusReceived = findReceivedStatus();
        Map<UUID, Set<LastDataProcessed>> hmStatusInbound = findInboundStatus();
        Map<String, Map<UUID, Set<LastDataToSubscriber>>> hmStatusOutboundBySubscriber = findOutboundStatus();


        ObjectMapper mapper = new ObjectMapper();
        ArrayNode root = new ArrayNode(mapper.getNodeFactory());

        Map<String, String> subscriberMap = ConfigManager.getConfigurations("db_subscriber");
        for (String subscriberName: subscriberMap.keySet()) {

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

            ObjectNode obj = root.addObject();
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
            Map<UUID, Set<LastDataToSubscriber>> hmStatusOutbound = hmStatusOutboundBySubscriber.get(subscriberName);
            getPublisherStatus(obj, subscriberName, servicesToSubscriber, hmStatusReceived, hmStatusInbound, hmStatusOutbound);



            //publishers where inbound transform is complete
            //publishers where outbound transform
        }

        return mapper.writeValueAsString(root);
    }

    private void getPublisherStatus(ObjectNode obj,
                                    String subscriberName,
                                    List<Service> services,
                                    Map<UUID, Set<LastDataReceived>> hmStatusReceived,
                                    Map<UUID, Set<LastDataProcessed>> hmStatusInbound,
                                    Map<UUID, Set<LastDataToSubscriber>> hmStatusOutbound) throws Exception {

        //this might be null if we've got no publishers
        if (services == null) {
            services = new ArrayList<>();
        }

        //this might be null if o service has nothing in the subscriber yet
        if (hmStatusOutbound == null) {
            hmStatusOutbound = new HashMap<>();
        }

        //number publishers up to date with inbound processing
        int inboundUpToDate = 0;
        int inboundOneDay = 0;
        int inboundMoreDays = 0;
        int outboundUpToDate = 0;
        int outboundOneDay = 0;
        int outboundMoreDays = 0;

        //ArrayNode arr = obj.putArray("publishers");

        for (Service service: services) {
            UUID serviceId = service.getId();
            List<UUID> systemIds = SystemHelper.getSystemIdsForService(service);
            Set<UUID> hsSystemIds = new HashSet<>(systemIds);
            
            Date dLastDataReceived = null;
            Set<LastDataReceived> hsReceived = hmStatusReceived.get(serviceId);
            if (hsReceived != null) {
                for (LastDataReceived r: hsReceived) {
                    if (hsSystemIds.contains(r.getSystemId())) {
                        if (dLastDataReceived == null
                                || r.getDataDate().before(dLastDataReceived)) {
                            dLastDataReceived = r.getDataDate();
                        }
                    }
                }
            }
            //if we've never received anything for this service, then skip it
            if (dLastDataReceived == null) {
                continue;
            }

            Date dLastDataProcessedInbound = null;
            Set<LastDataProcessed> hsInbound = hmStatusInbound.get(serviceId);
            if (hsInbound != null) {
                for (LastDataProcessed r: hsInbound) {
                    if (hsSystemIds.contains(r.getSystemId())) {
                        if (dLastDataProcessedInbound == null
                                || r.getDataDate().before(dLastDataProcessedInbound)) {
                            dLastDataProcessedInbound = r.getDataDate();
                        }
                    }
                }
            }

            Date dLastDataProcessedOutbound = null;
            Set<LastDataToSubscriber> hsOutbound = hmStatusOutbound.get(serviceId);
            if (hsOutbound != null) {
                for (LastDataToSubscriber r: hsOutbound) {
                    if (hsSystemIds.contains(r.getSystemId())) {
                        if (dLastDataProcessedOutbound == null
                                || r.getDataDate().before(dLastDataProcessedOutbound)) {
                            dLastDataProcessedOutbound = r.getDataDate();
                        }
                    }
                }
            }

            //inbound status
            int daysDiffInbound = getDaysDiff(dLastDataReceived, dLastDataProcessedInbound);
            if (daysDiffInbound == 0) {
                inboundUpToDate ++;

            } else if (daysDiffInbound <= 1) {
                inboundOneDay++;

            } else {
                inboundMoreDays++;
            }

            //outbound
            int daysDiffOutbound = getDaysDiff(dLastDataProcessedInbound, dLastDataProcessedOutbound);
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

    private int getDaysDiff(Date dtFrom, Date dtTo) {

        if (dtTo == null
                || dtFrom == null) {
            return Integer.MAX_VALUE;
        }

        long msFrom = dtFrom.getTime();
        long msTo = dtTo.getTime();
        long msDiff = msTo - msFrom;
        return (int)(msDiff / (1000 * 60 * 60 * 24));
    }

    private Map<String, Map<UUID, Set<LastDataToSubscriber>>> findOutboundStatus() throws Exception {

        Map<String, Map<UUID, Set<LastDataToSubscriber>>> ret = new HashMap<>();

        ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
        List<LastDataToSubscriber> statusReceived = exchangeDal.getLastDataToSubscriber();
        for (LastDataToSubscriber r: statusReceived) {
            String subscriber = r.getSubscriberConfigName();
            UUID serviceId = r.getServiceId();

            Map<UUID, Set<LastDataToSubscriber>> m = ret.get(subscriber);
            if (m == null) {
                m = new HashMap<>();
                ret.put(subscriber, m);
            }

            Set<LastDataToSubscriber> s = m.get(serviceId);
            if (s == null) {
                s = new HashSet<>();
                m.put(serviceId, s);
            }
            s.add(r);
        }

        return ret;

    }

    private Map<UUID, Set<LastDataProcessed>> findInboundStatus() throws Exception {

        Map<UUID, Set<LastDataProcessed>> ret = new HashMap<>();

        ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
        List<LastDataProcessed> statusReceived = exchangeDal.getLastDataProcessed();
        for (LastDataProcessed r: statusReceived) {
            UUID serviceId = r.getServiceId();

            Set<LastDataProcessed> s = ret.get(serviceId);
            if (s == null) {
                s = new HashSet<>();
                ret.put(serviceId, s);
            }
            s.add(r);
        }

        return ret;
    }

    private Map<UUID, Set<LastDataReceived>> findReceivedStatus() throws Exception {

        Map<UUID, Set<LastDataReceived>> ret = new HashMap<>();

        ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
        List<LastDataReceived> statusReceived = exchangeDal.getLastDataReceived();
        for (LastDataReceived r: statusReceived) {
            UUID serviceId = r.getServiceId();

            Set<LastDataReceived> s = ret.get(serviceId);
            if (s == null) {
                s = new HashSet<>();
                ret.put(serviceId, s);
            }
            s.add(r);
        }

        return ret;
    }

    private void getProcessingStatus() throws Exception {



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

    private Map<String, List<Service>> findPublishers() throws Exception {

        Map<String, List<Service>> ret = new HashMap<>();

        ServiceDalI serviceDal = DalProvider.factoryServiceDal();
        List<Service> all = serviceDal.getAll();
        for (Service service: all) {
            List<String> subs = SubscriberHelper.getSubscriberConfigNamesForPublisher(null, service.getId());
            for (String sub: subs) {

                List<Service> l = ret.get(sub);
                if (l == null) {
                    l = new ArrayList<>();
                    ret.put(sub, l);
                }
                l.add(service);
            }
        }

        return ret;
    }
}
