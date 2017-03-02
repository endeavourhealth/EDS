package org.endeavourhealth.common.cassandra;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.Session;

import java.util.concurrent.ConcurrentHashMap;

public class PreparedStatementCache {
    private final Session session;
    private final ConcurrentHashMap<String, PreparedStatement> statementCache = new ConcurrentHashMap<>();

    public PreparedStatementCache(Session session) {
        this.session = session;
    }

    public PreparedStatement getOrAdd(String statement) {
        return statementCache.computeIfAbsent(statement, key -> session.prepare(key));
    }

    public PreparedStatement getOrAdd(RegularStatement statement) {
        return getOrAdd(statement.toString());
    }

    public void clear() {
        statementCache.clear();
    }
}