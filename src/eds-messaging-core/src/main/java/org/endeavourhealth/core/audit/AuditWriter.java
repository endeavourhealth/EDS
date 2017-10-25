package org.endeavourhealth.core.audit;

import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.UUID;

public final class AuditWriter {
    private static final Logger LOG = LoggerFactory.getLogger(AuditWriter.class);

    private static final ExchangeDalI repository = DalProvider.factoryExchangeDal();

    public static void writeExchangeEvent(Exchange ex, String event) throws Exception {
        UUID uuid = ex.getId();

        ExchangeEvent eventToSave = new ExchangeEvent();
        eventToSave.setTimestamp(new Date());
        eventToSave.setExchangeId(uuid);
        eventToSave.setEventDesc(event);

        repository.save(eventToSave);
    }

    public static void writeExchange(Exchange ex) throws Exception {
        repository.save(ex);
    }

    public static Exchange readExchange(UUID exchangeId) throws Exception {
        return repository.getExchange(exchangeId);
    }


}
