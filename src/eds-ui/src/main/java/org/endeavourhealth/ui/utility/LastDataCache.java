package org.endeavourhealth.ui.utility;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.LastDataDalI;
import org.endeavourhealth.core.database.dal.audit.models.*;
import org.endeavourhealth.core.database.dal.datagenerator.SubscriberZipFileUUIDsDalI;
import org.endeavourhealth.core.database.dal.datagenerator.models.RemoteFilingRecord;
import org.endeavourhealth.transform.subscriber.SubscriberConfig;

import java.util.*;

/**
 * utility object to temporarily store a bunch of audit data about publishing and subscribing
 */
public class LastDataCache {

    public static final int MAX_OUTBOUND_WRITE_ERRORS = 11;

    private Map<CacheKey, LastDataReceived> hmReceived = new HashMap<>();
    private Map<CacheKey, UUID> hmInboundErrorExchanges = new HashMap<>();
    private Map<CacheKey, LastDataProcessed> hmInboundTransform = new HashMap<>();
    private Map<String, SubscriberConfig> hmSubscriberConfigs = new HashMap<>();
    private Map<CacheKey, Map<String, LastDataToSubscriber>> hmOutboundTransform = new HashMap<>();
    private Map<CacheKey, Map<String, LastDataInSubscriber>> hmOutboundWrite = new HashMap<>();
    private Map<CacheKey, Map<String, List<RemoteFilingRecord>>> hmOutboundErrors = new HashMap<>();
    private Collection<String> subscriberNames;


    public LastDataCache(String restrictToSubscriber) throws Exception {

        cacheLastDataReceived();
        cacheInboundErrors();
        cacheInboundTransformStatus();
        cacheSubscrierConfigs(restrictToSubscriber);
        copyFromDataGenerator();
        cacheOutboundTransformStatus(restrictToSubscriber);
        cacheOutboundWriteStatus(restrictToSubscriber);
        cacheOutboundWriteErrors();

    }

    private void cacheOutboundWriteErrors() throws Exception {

        //outbound writing errors
        for (String subscriberName: hmSubscriberConfigs.keySet()) {
            readOutboundWriteErrorsFromDataGenerator(subscriberName);
        }
    }

    private void readOutboundWriteErrorsFromDataGenerator(String subscriberName) throws Exception {
        SubscriberConfig config = hmSubscriberConfigs.get(subscriberName);
        Integer remoteSubscriberId = config.getRemoteSubscriberId();

        //if not a remote subscriber feed, then skip
        if (remoteSubscriberId == null) {
            return;
        }

        SubscriberZipFileUUIDsDalI dataGeneratorDal = DalProvider.factorySubscriberZipFileUUIDs();
        List<RemoteFilingRecord> failures = dataGeneratorDal.getFailures(remoteSubscriberId.intValue(), MAX_OUTBOUND_WRITE_ERRORS); //get just the latest 11 for each service

        for (RemoteFilingRecord r: failures) {

            UUID serviceId = r.getServiceId();
            UUID systemId = r.getSystemId();
            CacheKey key = new CacheKey(serviceId, systemId);

            Map<String, List<RemoteFilingRecord>> map = hmOutboundErrors.get(key);
            if (map == null) {
                map = new HashMap<>();
                hmOutboundErrors.put(key, map);
            }

            List<RemoteFilingRecord> l = map.get(subscriberName);
            if (l == null) {
                l = new ArrayList<>();
                map.put(subscriberName, l);
            }

            l.add(r);
        }
    }

    private void cacheOutboundWriteStatus(String restrictToSubscriber) throws Exception {

        //outbound writing status
        LastDataDalI lastDataDal = DalProvider.factoryLastDataDal();
        List<LastDataInSubscriber> writtenOut = lastDataDal.getLastDataInSubscriber(restrictToSubscriber);
        for (LastDataInSubscriber p: writtenOut) {
            CacheKey key = new CacheKey(p.getServiceId(), p.getSystemId());
            String subscriberName = p.getSubscriberConfigName();
            Map<String, LastDataInSubscriber> map = hmOutboundWrite.get(key);
            if (map == null) {
                map = new HashMap<>();
                hmOutboundWrite.put(key, map);
            }
            map.put(subscriberName, p);
        }
    }

    private void cacheOutboundTransformStatus(String restrictToSubscriber) throws Exception {

        //outbound transform status
        LastDataDalI lastDataDal = DalProvider.factoryLastDataDal();
        List<LastDataToSubscriber> processedOut = lastDataDal.getLastDataToSubscriber(restrictToSubscriber);
        for (LastDataToSubscriber p: processedOut) {
            CacheKey key = new CacheKey(p.getServiceId(), p.getSystemId());
            String subscriberName = p.getSubscriberConfigName();
            Map<String, LastDataToSubscriber> map = hmOutboundTransform.get(key);
            if (map == null) {
                map = new HashMap<>();
                hmOutboundTransform.put(key, map);
            }
            map.put(subscriberName, p);
        }

    }

    private void copyFromDataGenerator() throws Exception {

        //the data_generator DB populates a table with audit information which we should copy over into the true audit DB
        for (String subscriberName: hmSubscriberConfigs.keySet()) {
            copyAuditFromDataGenerator(subscriberName);
        }

    }

    private void cacheSubscrierConfigs(String restrictToSubscriber) throws Exception {

        //we need to know about remote subscriber DBs, so hit the config DB for their configs
        Map<String, String> subscriberMap = ConfigManager.getConfigurations("db_subscriber");
        for (String subscriberName: subscriberMap.keySet()) {

            //skip this one if we're filtering on a specific subscriber
            if (restrictToSubscriber != null
                    && !restrictToSubscriber.equals(subscriberName)) {
                continue;
            }

            String configJson = subscriberMap.get(subscriberName);
            JsonNode json = ObjectMapperPool.getInstance().readTree(configJson);
            SubscriberConfig config = SubscriberConfig.readFromJson(subscriberName, json);

            hmSubscriberConfigs.put(subscriberName, config);
        }

    }


    private void cacheInboundTransformStatus() throws Exception {

        LastDataDalI lastDataDal = DalProvider.factoryLastDataDal();
        List<LastDataProcessed> processedIn = lastDataDal.getLastDataProcessed();
        for (LastDataProcessed p: processedIn) {
            CacheKey key = new CacheKey(p.getServiceId(), p.getSystemId());
            hmInboundTransform.put(key, p);
        }

    }

    private void cacheInboundErrors() throws Exception {
        ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
        List<ExchangeTransformErrorState> inboundErrors = exchangeDal.getAllErrorStates();
        for (ExchangeTransformErrorState e: inboundErrors) {
            CacheKey key = new CacheKey(e.getServiceId(), e.getSystemId());
            List<UUID> exchangeIds = e.getExchangeIdsInError();
            UUID firstExchangeId = exchangeIds.get(0);
            hmInboundErrorExchanges.put(key, firstExchangeId);
        }

    }

    private void cacheLastDataReceived() throws Exception {

        LastDataDalI lastDataDal = DalProvider.factoryLastDataDal();
        List<LastDataReceived> received = lastDataDal.getLastDataReceived();
        for (LastDataReceived r: received) {
            CacheKey key = new CacheKey(r.getServiceId(), r.getSystemId());
            hmReceived.put(key, r);
        }
    }

    public LastDataReceived getLastReceived(UUID serviceId, UUID systemId) {
        CacheKey key = new CacheKey(serviceId, systemId);
        return hmReceived.get(key);
    }

    public UUID getInboundErrorExchangeId(UUID serviceId, UUID systemId) {
        CacheKey key = new CacheKey(serviceId, systemId);
        return hmInboundErrorExchanges.get(key);
    }

    public LastDataProcessed getLastProcessedInbound(UUID serviceId, UUID systemId) {
        CacheKey key = new CacheKey(serviceId, systemId);
        return hmInboundTransform.get(key);
    }

    public LastDataToSubscriber getLastProcessedOutbound(UUID serviceId, UUID systemId, String subscriberConfigName) {
        CacheKey key = new CacheKey(serviceId, systemId);
        Map<String, LastDataToSubscriber> map = hmOutboundTransform.get(key);
        if (map == null) {
            return null;
        } else {
            return map.get(subscriberConfigName);
        }
    }

    public LastDataInSubscriber getLastWrittenOutbound(UUID serviceId, UUID systemId, String subscriberConfigName) {
        CacheKey key = new CacheKey(serviceId, systemId);
        Map<String, LastDataInSubscriber> map = hmOutboundWrite.get(key);
        if (map == null) {
            return null;
        } else {
            return map.get(subscriberConfigName);
        }
    }

    /**
     * for remote subscriber DBs we have a table in the data_generator DB that is used to track when outbound messages
     * are applied to their subscriber DBs. For simplicity, we then copy that back into the regular audit database so it's
     * in the same place as audit data for internal subscribers
     */
    private void copyAuditFromDataGenerator(String subscriberName) throws Exception {

        SubscriberConfig config = hmSubscriberConfigs.get(subscriberName);
        Integer remoteSubscriberId = config.getRemoteSubscriberId();

        //if not a remote subscriber feed, then skip
        if (remoteSubscriberId == null) {
            return;
        }

        SubscriberZipFileUUIDsDalI dataGeneratorDal = DalProvider.factorySubscriberZipFileUUIDs();
        List<LastDataInSubscriber> written = dataGeneratorDal.getLastMessagesWritten(remoteSubscriberId.intValue());
        if (written.isEmpty()) {
            return;
        }

        //update audit
        LastDataDalI auditDal = DalProvider.factoryLastDataDal();

        Date maxDateWritten = null;

        for (LastDataInSubscriber newAudit: written) {
            newAudit.setSubscriberConfigName(subscriberName); //the data_generator doesn't know this, so set it here
            auditDal.save(newAudit);

            //we need to track the most recent write date
            if (maxDateWritten == null
                    || newAudit.getWrittenDate().after(maxDateWritten)) {
                maxDateWritten = newAudit.getWrittenDate();
            }
        }

        //to keep the data_generator DB tidy, delete these audits out, now we've copied them
        dataGeneratorDal.deleteLastMessagesWritten(remoteSubscriberId.intValue(), maxDateWritten);
    }

    public SubscriberConfig getSubscriberConfig(String subscriberName) {
        return hmSubscriberConfigs.get(subscriberName);
    }

    public Set<String> getSubscriberNames() {
        return new HashSet<>(hmSubscriberConfigs.keySet());
    }

    public List<RemoteFilingRecord> getOutboundWriteErrors(UUID serviceId, UUID systemId, String subscriberName) {
        CacheKey key = new CacheKey(serviceId, systemId);
        Map<String, List<RemoteFilingRecord>> map = hmOutboundErrors.get(key);
        if (map == null) {
            return new ArrayList<>();
        }

        List<RemoteFilingRecord> l = map.get(subscriberName);
        if (l == null) {
            return new ArrayList<>();
        }

        return l;
    }


    static class CacheKey {
        private UUID serviceId;
        private UUID systemId;

        public CacheKey(UUID serviceId, UUID systemId) {
            this.serviceId = serviceId;
            this.systemId = systemId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (!serviceId.equals(cacheKey.serviceId)) return false;
            return systemId.equals(cacheKey.systemId);

        }

        @Override
        public int hashCode() {
            int result = serviceId.hashCode();
            result = 31 * result + systemId.hashCode();
            return result;
        }
    }
}
