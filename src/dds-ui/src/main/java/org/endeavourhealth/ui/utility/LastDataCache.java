package org.endeavourhealth.ui.utility;

import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.LastDataDalI;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeTransformErrorState;
import org.endeavourhealth.core.database.dal.audit.models.LastDataInSubscriber;
import org.endeavourhealth.core.database.dal.audit.models.LastDataProcessed;
import org.endeavourhealth.core.database.dal.audit.models.LastDataReceived;
import org.endeavourhealth.core.database.dal.audit.models.LastDataToSubscriber;
import org.endeavourhealth.core.database.dal.datagenerator.SubscriberZipFileUUIDsDalI;
import org.endeavourhealth.core.database.dal.datagenerator.models.RemoteFilingRecord;
import org.endeavourhealth.transform.subscriber.SubscriberConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * utility object to temporarily store a bunch of audit data about publishing and subscribing
 */
public class LastDataCache {

    public static final int MAX_OUTBOUND_WRITE_ERRORS = 10;

    private Map<CacheKey, LastDataReceived> hmReceived = new HashMap<>();
    private Map<CacheKey, UUID> hmInboundErrorExchanges = new HashMap<>();
    private Map<CacheKey, LastDataProcessed> hmProcessedInbound = new HashMap<>();
    private Map<CacheKey, Map<String, LastDataToSubscriber>> hmProcessOutbound = new HashMap<>();
    private Map<CacheKey, Map<String, LastDataInSubscriber>> hmWrittenOutbound = new HashMap<>();
    private Set<String> subscriberNames = new HashSet<>();
    private Map<String, SubscriberConfig> subscriberConfigs = new HashMap<>();

    public static final int MAX_OUTBOUND_WRITE_ERRORS_CONST = 10;

    public static LastDataCache getLastData() throws Exception {
        return new LastDataCache();
    }

    public LastDataCache(String specificSubscriber) throws Exception {
        if (specificSubscriber == null) {
            loadAllData();
        } else {
            loadSpecificSubscriber(specificSubscriber);
        }
    }

    private LastDataCache() throws Exception {
        loadAllData();
    }

    private void loadAllData() throws Exception {
        LastDataDalI lastDataDal = DalProvider.factoryLastDataDal();

        List<LastDataReceived> received = lastDataDal.getLastDataReceived();
        for (LastDataReceived r : received) {
            CacheKey key = new CacheKey(r.getServiceId(), r.getSystemId());
            hmReceived.put(key, r);
        }

        ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
        List<ExchangeTransformErrorState> inboundErrors = exchangeDal.getAllErrorStates();
        for (ExchangeTransformErrorState e : inboundErrors) {
            CacheKey key = new CacheKey(e.getServiceId(), e.getSystemId());
            List<UUID> exchangeIds = e.getExchangeIdsInError();
            UUID firstExchangeId = exchangeIds.get(0);
            hmInboundErrorExchanges.put(key, firstExchangeId);
        }

        List<LastDataProcessed> processedIn = lastDataDal.getLastDataProcessed();
        for (LastDataProcessed p : processedIn) {
            CacheKey key = new CacheKey(p.getServiceId(), p.getSystemId());
            hmProcessedInbound.put(key, p);
        }

        List<LastDataToSubscriber> processedOut = lastDataDal.getLastDataToSubscriber();
        for (LastDataToSubscriber p : processedOut) {
            CacheKey key = new CacheKey(p.getServiceId(), p.getSystemId());
            String subscriberName = p.getSubscriberConfigName();
            subscriberNames.add(subscriberName);
            Map<String, LastDataToSubscriber> map = hmProcessOutbound.get(key);
            if (map == null) {
                map = new HashMap<>();
                hmProcessOutbound.put(key, map);
            }
            map.put(subscriberName, p);
        }

        List<LastDataInSubscriber> writtenOut = lastDataDal.getLastDataInSubscriber();
        for (LastDataInSubscriber w : writtenOut) {
            CacheKey key = new CacheKey(w.getServiceId(), w.getSystemId());
            String subscriberName = w.getSubscriberConfigName();
            subscriberNames.add(subscriberName);
            Map<String, LastDataInSubscriber> map = hmWrittenOutbound.get(key);
            if (map == null) {
                map = new HashMap<>();
                hmWrittenOutbound.put(key, map);
            }
            map.put(subscriberName, w);
        }

        for (String subName : subscriberNames) {
            try {
                SubscriberConfig config = SubscriberConfig.readFromConfig(subName);
                if (config != null) {
                    subscriberConfigs.put(subName, config);
                }
            } catch (Exception e) {
                // skip configs that can't be loaded
            }
        }
    }

    private void loadSpecificSubscriber(String subscriberName) throws Exception {
        LastDataDalI lastDataDal = DalProvider.factoryLastDataDal();

        List<LastDataReceived> received = lastDataDal.getLastDataReceived();
        for (LastDataReceived r : received) {
            CacheKey key = new CacheKey(r.getServiceId(), r.getSystemId());
            hmReceived.put(key, r);
        }

        ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
        List<ExchangeTransformErrorState> inboundErrors = exchangeDal.getAllErrorStates();
        for (ExchangeTransformErrorState e : inboundErrors) {
            CacheKey key = new CacheKey(e.getServiceId(), e.getSystemId());
            List<UUID> exchangeIds = e.getExchangeIdsInError();
            UUID firstExchangeId = exchangeIds.get(0);
            hmInboundErrorExchanges.put(key, firstExchangeId);
        }

        List<LastDataProcessed> processedIn = lastDataDal.getLastDataProcessed();
        for (LastDataProcessed p : processedIn) {
            CacheKey key = new CacheKey(p.getServiceId(), p.getSystemId());
            hmProcessedInbound.put(key, p);
        }

        List<LastDataToSubscriber> processedOut = lastDataDal.getLastDataToSubscriber();
        for (LastDataToSubscriber p : processedOut) {
            String configName = p.getSubscriberConfigName();
            if (configName.equals(subscriberName)) {
                CacheKey key = new CacheKey(p.getServiceId(), p.getSystemId());
                subscriberNames.add(subscriberName);
                Map<String, LastDataToSubscriber> map = hmProcessOutbound.get(key);
                if (map == null) {
                    map = new HashMap<>();
                    hmProcessOutbound.put(key, map);
                }
                map.put(subscriberName, p);
            }
        }

        List<LastDataInSubscriber> writtenOut = lastDataDal.getLastDataInSubscriber();
        for (LastDataInSubscriber w : writtenOut) {
            String configName = w.getSubscriberConfigName();
            if (configName.equals(subscriberName)) {
                CacheKey key = new CacheKey(w.getServiceId(), w.getSystemId());
                subscriberNames.add(subscriberName);
                Map<String, LastDataInSubscriber> map = hmWrittenOutbound.get(key);
                if (map == null) {
                    map = new HashMap<>();
                    hmWrittenOutbound.put(key, map);
                }
                map.put(subscriberName, w);
            }
        }

        try {
            SubscriberConfig config = SubscriberConfig.readFromConfig(subscriberName);
            if (config != null) {
                subscriberConfigs.put(subscriberName, config);
            }
        } catch (Exception e) {
            // skip configs that can't be loaded
        }
    }

    public Set<String> getSubscriberNames() {
        return subscriberNames;
    }

    public SubscriberConfig getSubscriberConfig(String name) {
        return subscriberConfigs.get(name);
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
        return hmProcessedInbound.get(key);
    }

    public LastDataToSubscriber getLastProcessedOutbound(UUID serviceId, UUID systemId, String subscriberConfigName) {
        CacheKey key = new CacheKey(serviceId, systemId);
        Map<String, LastDataToSubscriber> map = hmProcessOutbound.get(key);
        if (map == null) {
            return null;
        } else {
            return map.get(subscriberConfigName);
        }
    }

    public LastDataInSubscriber getLastWrittenOutbound(UUID serviceId, UUID systemId, String subscriberConfigName) {
        CacheKey key = new CacheKey(serviceId, systemId);
        Map<String, LastDataInSubscriber> map = hmWrittenOutbound.get(key);
        if (map == null) {
            return null;
        } else {
            return map.get(subscriberConfigName);
        }
    }

    public List<RemoteFilingRecord> getOutboundWriteErrors(UUID serviceId, UUID systemId, String subscriberName) {
        return new ArrayList<>();
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
