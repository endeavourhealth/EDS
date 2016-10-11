package org.endeavourhealth.core.data.audit;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.mapping.Mapper;
import com.google.common.collect.Lists;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.audit.accessors.ExchangeAccessor;
import org.endeavourhealth.core.data.audit.models.Exchange;
import org.endeavourhealth.core.data.audit.models.ExchangeEvent;
import org.endeavourhealth.core.data.audit.models.ExchangeTransform;
import org.endeavourhealth.core.data.audit.models.ExchangeTransformByServiceAndSystem;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class AuditRepository extends Repository{

    public void save(Exchange exchange, ExchangeEvent event) {

        BatchStatement batch = new BatchStatement();

        //exchange will only be non-null when writing the first event for an exchange
        if (exchange != null) {
            Mapper<Exchange> mapperExchange = getMappingManager().mapper(Exchange.class);
            batch.add(mapperExchange.saveQuery(exchange));
        }

        Mapper<ExchangeEvent> mapperEvent = getMappingManager().mapper(ExchangeEvent.class);
        batch.add(mapperEvent.saveQuery(event));

        getSession().execute(batch);
    }

    public void save(ExchangeTransform exchangeTransform) {

        Mapper<ExchangeTransform> mapper = getMappingManager().mapper(ExchangeTransform.class);
        mapper.save(exchangeTransform);
    }

    public ExchangeTransform getMostRecentExchangeTransform(UUID serviceId, UUID systemId, UUID exchangeId) {

        ExchangeAccessor accessor = getMappingManager().createAccessor(ExchangeAccessor.class);
        Iterator<ExchangeTransform> iterator = accessor.getMostRecentExchangeTransform(serviceId, systemId, exchangeId).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    public List<ExchangeTransform> getAllExchangeTransform(UUID serviceId, UUID systemId, UUID exchangeId) {

        ExchangeAccessor accessor = getMappingManager().createAccessor(ExchangeAccessor.class);
        return Lists.newArrayList(accessor.getAllExchangeTransform(serviceId, systemId, exchangeId));
    }

    public ExchangeTransformByServiceAndSystem getMostRecentExchangeTransform(UUID serviceId, UUID systemId) {

        ExchangeAccessor accessor = getMappingManager().createAccessor(ExchangeAccessor.class);
        Iterator<ExchangeTransformByServiceAndSystem> iterator = accessor.getMostRecentExchangeTransformByServiceAndSystem(serviceId, systemId).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    public List<ExchangeTransformByServiceAndSystem> getAllExchangeTransform(UUID serviceId, UUID systemId) {

        ExchangeAccessor accessor = getMappingManager().createAccessor(ExchangeAccessor.class);
        return Lists.newArrayList(accessor.getAllExchangeTransformByServiceAndSystem(serviceId, systemId));
    }
}
