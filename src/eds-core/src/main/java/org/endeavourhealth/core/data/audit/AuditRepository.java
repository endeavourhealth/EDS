package org.endeavourhealth.core.data.audit;

import com.datastax.driver.mapping.Mapper;
import com.google.common.collect.Lists;
import org.endeavourhealth.core.data.Repository;
import org.endeavourhealth.core.data.audit.accessors.AuditAccessor;
import org.endeavourhealth.core.data.audit.models.Exchange;
import org.endeavourhealth.core.data.audit.models.ExchangeEvent;
import org.endeavourhealth.core.data.audit.models.ExchangeTransformAudit;
import org.endeavourhealth.core.data.audit.models.ExchangeTransformErrorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class AuditRepository extends Repository{

    private static final Logger LOG = LoggerFactory.getLogger(AuditRepository.class);


    public void save(Exchange exchange, ExchangeEvent event) {

        //exchange will only be non-null when writing the first event for an exchange
        if (exchange != null) {
            //NOTE: the exchanges can be large, so don't use a batch to save it, as it will fail
            Mapper<Exchange> mapperExchange = getMappingManager().mapper(Exchange.class);
            mapperExchange.save(exchange);
        }


        Mapper<ExchangeEvent> mapperEvent = getMappingManager().mapper(ExchangeEvent.class);
        mapperEvent.save(event);
    }

    public Exchange getExchange(UUID exchangeId) {
        Mapper<Exchange> mapper = getMappingManager().mapper(Exchange.class);
        return mapper.get(exchangeId);
    }

    public void save(ExchangeTransformAudit exchangeTransformAudit) {

        Mapper<ExchangeTransformAudit> mapper = getMappingManager().mapper(ExchangeTransformAudit.class);
        mapper.save(exchangeTransformAudit);
    }

    public void save(ExchangeTransformErrorState errorState) {

        Mapper<ExchangeTransformErrorState> mapper = getMappingManager().mapper(ExchangeTransformErrorState .class);
        mapper.save(errorState);
    }

    public void delete(ExchangeTransformErrorState errorState) {

        Mapper<ExchangeTransformErrorState> mapper = getMappingManager().mapper(ExchangeTransformErrorState .class);
        mapper.delete(errorState);
    }

    public ExchangeTransformAudit getMostRecentExchangeTransform(UUID serviceId, UUID systemId, UUID exchangeId) {

        AuditAccessor accessor = getMappingManager().createAccessor(AuditAccessor.class);
        Iterator<ExchangeTransformAudit> iterator = accessor.getMostRecentExchangeTransform(serviceId, systemId, exchangeId).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    public List<ExchangeTransformAudit> getAllExchangeTransform(UUID serviceId, UUID systemId, UUID exchangeId) {

        AuditAccessor accessor = getMappingManager().createAccessor(AuditAccessor.class);
        return Lists.newArrayList(accessor.getAllExchangeTransform(serviceId, systemId, exchangeId));
    }

    public ExchangeTransformErrorState getErrorState(UUID serviceId, UUID systemId) {

        AuditAccessor accessor = getMappingManager().createAccessor(AuditAccessor.class);
        Iterator<ExchangeTransformErrorState> iterator = accessor.getErrorState(serviceId, systemId).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    public List<ExchangeTransformErrorState> getAllErrorStates() {

        AuditAccessor accessor = getMappingManager().createAccessor(AuditAccessor.class);
        return Lists.newArrayList(accessor.getAllErrorStates());
    }

    public boolean isServiceStarted(UUID serviceId, UUID systemId) {

        //find the FIRST exchange we received for the parameters
        AuditAccessor accessor = getMappingManager().createAccessor(AuditAccessor.class);
        Iterator<ExchangeTransformAudit> iterator = accessor.getFirstExchange(serviceId, systemId).iterator();

        //if we've never transformed an exchange for this service/system before, then it's definitely not started
        if (!iterator.hasNext()) {
            return false;
        }

        //if we have processed an exchange for the service/system, then make sure it was processed ok
        ExchangeTransformAudit firstExchangeAudit = iterator.next();
        if (firstExchangeAudit.getErrorXml() == null) {
            return true;
        }

        //if it wasn't processed ok, then make sure there was a subsequent audit of that same exchange being processed ok
        iterator = accessor.getMostRecentExchangeTransform(serviceId, systemId, firstExchangeAudit.getExchangeId()).iterator();
        ExchangeTransformAudit subsequentExchangeAudit = iterator.next();
        if (subsequentExchangeAudit.getErrorXml() == null) {
            return true;
        }

        //if the first exchange for our service/system was never processed OK, we've not properly started receiving for this service
        return false;
    }
}
