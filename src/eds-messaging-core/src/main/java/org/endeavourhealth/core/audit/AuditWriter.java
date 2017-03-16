package org.endeavourhealth.core.audit;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.core.data.audit.AuditRepository;
import org.endeavourhealth.core.data.audit.models.ExchangeEvent;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AuditWriter {
    private static final Logger LOG = LoggerFactory.getLogger(AuditWriter.class);

    private static final AuditRepository repository = new AuditRepository();



    public static void writeExchangeEvent(Exchange ex, String event) {

        UUID uuid = ex.getExchangeId();

        ExchangeEvent eventToSave = new ExchangeEvent();
        eventToSave.setTimestamp(new Date());
        eventToSave.setExchangeId(uuid);
        eventToSave.setEventDesc(event);

        repository.save(eventToSave);
    }

    public static void writeExchange(Exchange ex) {

        UUID uuid = ex.getExchangeId();
        String body = ex.getBody();
        Date timestamp = ex.getTimestamp();

        //use jackson to write the headers to JSON
        String headersJson = null;
        Map<String, String> headers = ex.getHeaders();
        try {
            headersJson = ObjectMapperPool.getInstance().writeValueAsString(headers);
        } catch (JsonProcessingException e) {
            //not throwing this exception further up, since it should never happen
            //and means we don't need to litter try/catches everywhere this is called from
            LOG.error("Failed to write exchange headers to Json", e);
        }

        //always re-save the exchange, so any new/changed headers are stored in the DB
        org.endeavourhealth.core.data.audit.models.Exchange exchangeToSave = new org.endeavourhealth.core.data.audit.models.Exchange();
        exchangeToSave.setTimestamp(timestamp);
        exchangeToSave.setExchangeId(uuid);
        exchangeToSave.setHeaders(headersJson);
        exchangeToSave.setBody(body);

        repository.save(exchangeToSave);
    }

    public static Exchange readExchange(UUID exchangeId) throws IOException {
        org.endeavourhealth.core.data.audit.models.Exchange dbExchange = repository.getExchange(exchangeId);
        String body = dbExchange.getBody();
        String headersJson = dbExchange.getHeaders();
        Date timestamp = dbExchange.getTimestamp();

        Map<String, String> headersMap = ObjectMapperPool.getInstance().readValue(headersJson, HashMap.class);

        return new Exchange(exchangeId, body, headersMap, timestamp);
    }

    /*public static void writeAuditEvent(Exchange ex, AuditEvent event) throws Exception {

        org.endeavourhealth.core.data.audit.models.Exchange exchangeToSave = null;
        ExchangeEvent eventToSave = null;

        UUID uuid = ex.getExchangeId();
        //if logging the receive of an exchange, log the exchange itself
        if (event == AuditEvent.RECEIVE) {

            String body = ex.getBody();

            //use jackson to write the headers to JSON
            Map<String, String> headers = ex.getHeaders();
            String headersJson = ObjectMapperPool.getInstance().writeValueAsString(headers);

            exchangeToSave = new org.endeavourhealth.core.data.audit.models.Exchange();
            exchangeToSave.setTimestamp(new Date());
            exchangeToSave.setExchangeId(uuid);
            exchangeToSave.setHeaders(headersJson);
            exchangeToSave.setBody(body);
        }

        eventToSave = new ExchangeEvent();
        eventToSave.setTimestamp(new Date());
        eventToSave.setExchangeId(uuid);
        eventToSave.setEvent(new Integer(event.getValue()));

        repository.save(exchangeToSave, eventToSave);
    }*/


}
