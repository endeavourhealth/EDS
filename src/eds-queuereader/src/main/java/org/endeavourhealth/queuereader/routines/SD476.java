package org.endeavourhealth.queuereader.routines;

import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.fhir.schema.OrganisationType;
import org.endeavourhealth.core.configuration.PostMessageToExchangeConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.core.messaging.pipeline.components.PostMessageToExchange;
import org.endeavourhealth.core.queueing.QueueHelper;
import org.endeavourhealth.core.queueing.RoutingOverride;
import org.endeavourhealth.core.subscribers.SubscriberHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class SD476 extends AbstractRoutine {
    private static final Logger LOG = LoggerFactory.getLogger(SD476.class);

    private static final String BULK_OPERATION_NAME = "Fixing NWL records affected by BST problem (SD-476)";

    private static final String SUBSCRIBER_CONFIG_NAME = "nwl_wsic";

    private static List<String> routingkeys = null;
    private static int lastRoutingKey = 0;

    /**
     * fixes data for SD-476
     */
    public static void fixNwlRecordsAffectedByBstProblem(boolean includeStartedButNotFinishedServices, boolean testMode, int minsDelay, String odsCodeRegex) {
        LOG.debug("Fixing NWL Records Affected by BST Problem");
        LOG.debug("ODS regex = " + odsCodeRegex);
        LOG.debug("Mins delay = " + minsDelay);
        LOG.debug("Test mode = " + testMode);

        try {
            List<Service> services = findServices();
            LOG.debug("Found " + services.size() + " services");

            //sort so that GP practices are first
            OrganisationType gp = OrganisationType.fromCode(OrganisationType.CODE_GP_PRACTICE);
            services.sort((o1, o2) -> {
                if (o1.getOrganisationType() == gp
                        && o2.getOrganisationType() != gp) {
                    return -1;

                } else if (o1.getOrganisationType() != gp
                        && o2.getOrganisationType() == gp) {
                    return 1;

                } else {
                    return 0;
                }
            });

            for (Service service: services) {

                if (shouldSkipService(service, odsCodeRegex)) {
                    continue;
                }

                if (!testMode
                        && isServiceStartedOrDoneBulkOperation(service, BULK_OPERATION_NAME, includeStartedButNotFinishedServices)) {
                    continue;
                }

                LOG.debug("Doing " + service);
                fixForService(service, testMode);

                if (!testMode) {
                    setServiceDoneBulkOperation(service, BULK_OPERATION_NAME);
                }

                //throttle a bit so we don't hammer RabbitMQ too hard
                LOG.debug("Waiting " + minsDelay + " mins");
                Thread.sleep(minsDelay * 60 * 1000);
            }

            LOG.debug("Finished Fixing NWL Records Affected by BST Problem");

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }

    private static List<Service> findServices() throws Exception {
        Connection connection = ConnectionManager.getAuditConnection();
        Statement statement = null;
        try {
            String sql = "SELECT DISTINCT service_id FROM tmp.SD476_audits";
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            List<Service> ret = new ArrayList<>();
            ServiceDalI serviceDal = DalProvider.factoryServiceDal();

            while (rs.next()) {
                String s = rs.getString(1);
                UUID serviceId = UUID.fromString(s);

                Service service = serviceDal.getById(serviceId);
                ret.add(service);
            }

            return ret;

        } finally {
            statement.close();
            connection.close();
        }

    }

    private static void fixForService(Service service, boolean testMode) throws Exception {

        //verify they're part of WSIC
        List<String> subscribers = SubscriberHelper.getSubscriberConfigNamesForPublisher(null, service.getId());
        if (!subscribers.contains(SUBSCRIBER_CONFIG_NAME)) {
            throw new Exception("Service " + service + " is not a publisher to " + SUBSCRIBER_CONFIG_NAME);
        }

        //get exchanges and batches
        Map<UUID, Set<UUID>> hmExchangeIds = findExchangesAndBatches(service.getId());
        LOG.debug("Found " + hmExchangeIds.size() + " exchanges");

        //get exchanges
        ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();

        List<Exchange> exchanges = new ArrayList<>();
        for (UUID exchangeId : hmExchangeIds.keySet()) {
            Exchange exchange = exchangeDal.getExchange(exchangeId);
            exchanges.add(exchange);
        }

        //sort exchanges into date order
        exchanges.sort((o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));

        //backup of endpoints
        /*List<ServiceInterfaceEndpoint> endpointsBak = service.getEndpointsList(); //this creates new instances
        ServiceDalI serviceDal = DalProvider.factoryServiceDal();

        //make into bulk mode
        if (!testMode) {
            List<ServiceInterfaceEndpoint> endpoints = service.getEndpointsList();
            for (ServiceInterfaceEndpoint endpoint: endpoints) {
                if (endpoint.getEndpoint().equals(ServiceInterfaceEndpoint.STATUS_NORMAL)) {
                    endpoint.setEndpoint(ServiceInterfaceEndpoint.STATUS_BULK_PROCESSING);
                }
            }
            service.setEndpointsList(endpoints);
            serviceDal.save(service);
        }*/


        RoutingOverride override = new RoutingOverride();
        override.setServiceId(service.getId());
        override.setExchangeName(QueueHelper.ExchangeName.TRANSFORM.getName());
        override.setRoutingKey(getNextRoutingKey());
        RoutingOverride[] overrideArr = new RoutingOverride[]{override};
        String routingOverrideJson = ObjectMapperPool.getInstance().writeValueAsString(overrideArr);

        for (Exchange exchange : exchanges) {
            UUID exchangeId = exchange.getId();
            Set<UUID> batchIdsForExchange = hmExchangeIds.get(exchangeId);

            //set new batch ID in exchange header
            String batchIdString = ObjectMapperPool.getInstance().writeValueAsString(batchIdsForExchange.toArray());
            exchange.setHeader(HeaderKeys.BatchIdsJson, batchIdString);

            //there will be some exchanges that can't be re-queued, so we need to change that (but only in memory)
            exchange.setHeaderAsBoolean(HeaderKeys.AllowQueueing, null);

            //and route into the bulk queues
            exchange.setHeader(HeaderKeys.RoutingOverride, routingOverrideJson);

            //this is specifically for WSIC
            String[] arr = new String[]{SUBSCRIBER_CONFIG_NAME};
            exchange.setHeaderAsStringArray(HeaderKeys.SubscriberConfigNames, arr);

            if (testMode) {
                LOG.debug("    Exchange " + exchangeId + " with " + batchIdsForExchange.size() + " batches");

            } else {

                //post new batch to protocol queue
                PostMessageToExchangeConfig exchangeConfig = QueueHelper.findExchangeConfig(QueueHelper.ExchangeName.PROTOCOL);
                PostMessageToExchange component = new PostMessageToExchange(exchangeConfig);
                component.process(exchange);
            }
        }

        //restore to normal mode
        /*if (!testMode) {
            service.setEndpointsList(endpointsBak);
            serviceDal.save(service);
        }*/
    }

    private static Map<UUID, Set<UUID>> findExchangesAndBatches(UUID serviceId) throws Exception {
        Connection connection = ConnectionManager.getAuditConnection();
        PreparedStatement ps = null;
        try {
            String sql = "SELECT exchange_id, batch_id"
                    + " FROM tmp.SD476_audits"
                    + " WHERE service_id = ?";

            ps = connection.prepareStatement(sql);
            ps.setString(1, serviceId.toString());

            ResultSet rs = ps.executeQuery();

            Map<UUID, Set<UUID>> ret = new HashMap<>();

            while (rs.next()) {
                String exchangeStr = rs.getString(1);
                String batchStr = rs.getString(2);
                UUID exchangeId = UUID.fromString(exchangeStr);

                Set<UUID> s = ret.get(exchangeId);
                if (s == null) {
                    s = new HashSet<>();
                    ret.put(exchangeId, s);
                }
                s.add(UUID.fromString(batchStr));
            }

            return ret;

        } finally {
            ps.close();
            connection.close();
        }
    }

    public static String getNextRoutingKey() {
        if (routingkeys == null) {
            routingkeys = new ArrayList<>();

            routingkeys.add("zzBulk-A1");
            routingkeys.add("zzBulk-B1");
            routingkeys.add("zzBulk-C1");
            routingkeys.add("zzBulk-D1");
            routingkeys.add("zzBulk-E1");

            lastRoutingKey = 0;

        } else {

            lastRoutingKey ++;
            if (lastRoutingKey >= routingkeys.size()) {
                lastRoutingKey = 0;
            }
        }

        return routingkeys.get(lastRoutingKey);
    }
}