package org.endeavourhealth.core.audit;


import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.endeavourhealth.core.data.CassandraConnector;
import org.endeavourhealth.core.data.PreparedStatementCache;
import org.endeavourhealth.core.data.audit.AuditRepository;
import org.endeavourhealth.core.data.audit.models.ExchangeEvent;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public final class AuditWriter {
    private static final Logger LOG = LoggerFactory.getLogger(AuditWriter.class);

    private static final AuditRepository repository = new AuditRepository();

    public static void writeAuditEvent(Exchange ex, AuditEvent event) throws Exception {

        org.endeavourhealth.core.data.audit.models.Exchange exchangeToSave = null;
        ExchangeEvent eventToSave = null;

        UUID uuid = ex.getExchangeId();
        //if the UUID is null, the assign a new ID and save the Exchange itself
        if (uuid == null) {
            uuid = UUID.randomUUID();
            ex.setExchangeId(uuid);
            String body = ex.getBody();

            //use jackson to write the headers to JSON
            Map<String, String> headers = ex.getHeaders();
            ObjectMapper mapper = new ObjectMapper();
            String headersJson = mapper.writeValueAsString(headers);

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
    }


}
