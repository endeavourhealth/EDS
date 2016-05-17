package org.endeavourhealth.core.audit;


import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.endeavourhealth.core.data.CassandraConnector;
import org.endeavourhealth.core.data.PreparedStatementCache;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public final class AuditWriter {
    private static final Logger LOG = LoggerFactory.getLogger(AuditWriter.class);

    private static final String CQL_EXCHANGE = "INSERT INTO audit.exchange ("
            + "timestamp, exchangeId, headers, body) VALUES (?, ?, ?, ?)";

    private static final String CQL_ECHANGE_EVENT = "INSERT INTO audit.exchangeEvent ("
            + "timestamp, exchangeId, event) VALUES (?, ?, ?)";

    private static void writeAudit(Exchange ex) throws Exception {

        //if an id is already assigned, the exchange has already been audited
        UUID uuid = ex.getExchangeId();
        if (uuid != null) {
            return;
        }

        uuid = UUID.randomUUID();
        String body = ex.getBody();

        //use jackson to write the headers to JSON
        Map<String, String> headers = ex.getHeaders();
        ObjectMapper mapper = new ObjectMapper();
        String headersJson = mapper.writeValueAsString(headers);

        Session session = CassandraConnector.getInstance().getSession();
        PreparedStatementCache cache = CassandraConnector.getInstance().getStatementCache();
        PreparedStatement preparedStatement = cache.getOrAdd(CQL_EXCHANGE);

        BoundStatement boundStatement = preparedStatement.
                bind().
                setTimestamp(0, new Date()).
                setUUID(1, uuid).
                setString(2, headersJson).
                setString(3, body);

        ResultSet rs = session.execute(boundStatement);
        if (!rs.wasApplied()) {
            throw new RuntimeException("Failed to write CQL " + CQL_EXCHANGE);
        }

        ex.setExchangeId(uuid);
    }

    public static void writeAuditEvent(Exchange ex, AuditEvent event) throws Exception {
        UUID uuid = ex.getExchangeId();
        if (uuid == null) {
            writeAudit(ex);
            uuid = ex.getExchangeId();
        }

        Session session = CassandraConnector.getInstance().getSession();
        PreparedStatementCache cache = CassandraConnector.getInstance().getStatementCache();
        PreparedStatement preparedStatement = cache.getOrAdd(CQL_ECHANGE_EVENT);

        BoundStatement boundStatement = preparedStatement.
                bind().
                setTimestamp(0, new Date()).
                setUUID(1, uuid).
                setInt(2, event.getValue());

        ResultSet rs = session.execute(boundStatement);
        if (!rs.wasApplied()) {
            throw new RuntimeException("Failed to write CQL " + CQL_EXCHANGE);
        }
    }
}
