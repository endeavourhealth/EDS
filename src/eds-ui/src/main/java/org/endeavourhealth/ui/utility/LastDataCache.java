package org.endeavourhealth.ui.utility;

import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.LastDataDalI;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeTransformErrorState;
import org.endeavourhealth.core.database.dal.audit.models.LastDataProcessed;
import org.endeavourhealth.core.database.dal.audit.models.LastDataReceived;
import org.endeavourhealth.core.database.dal.audit.models.LastDataToSubscriber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * utility object to temporarily store a bunch of audit data about publishing and subscribing
 */
public class LastDataCache {

    private Map<CacheKey, LastDataReceived> hmReceived = new HashMap<>();
    private Map<CacheKey, UUID> hmInboundErrorExchanges = new HashMap<>();
    private Map<CacheKey, LastDataProcessed> hmProcessedInbound = new HashMap<>();
    private Map<CacheKey, Map<String, LastDataToSubscriber>> hmProcessOutbound = new HashMap<>();

    public static LastDataCache getLastData() throws Exception {
        return new LastDataCache();
    }

    private LastDataCache() throws Exception {

        LastDataDalI lastDataDal = DalProvider.factoryLastDataDal();

        List<LastDataReceived> received = lastDataDal.getLastDataReceived();
        for (LastDataReceived r: received) {
            CacheKey key = new CacheKey(r.getServiceId(), r.getSystemId());
            hmReceived.put(key, r);
        }

        //hmInboundErrorExchanges
        ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
        List<ExchangeTransformErrorState> inboundErrors = exchangeDal.getAllErrorStates();
        for (ExchangeTransformErrorState e: inboundErrors) {
            CacheKey key = new CacheKey(e.getServiceId(), e.getSystemId());
            List<UUID> exchangeIds = e.getExchangeIdsInError();
            UUID firstExchangeId = exchangeIds.get(0);
            hmInboundErrorExchanges.put(key, firstExchangeId);
        }

        List<LastDataProcessed> processedIn = lastDataDal.getLastDataProcessed();
        for (LastDataProcessed p: processedIn) {
            CacheKey key = new CacheKey(p.getServiceId(), p.getSystemId());
            hmProcessedInbound.put(key, p);
        }

        List<LastDataToSubscriber> processedOut = lastDataDal.getLastDataToSubscriber();
        for (LastDataToSubscriber p: processedOut) {
            CacheKey key = new CacheKey(p.getServiceId(), p.getSystemId());
            String subscriberName = p.getSubscriberConfigName();
            Map<String, LastDataToSubscriber> map = hmProcessOutbound.get(key);
            if (map == null) {
                map = new HashMap<>();
                hmProcessOutbound.put(key, map);
            }
            map.put(subscriberName, p);
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
