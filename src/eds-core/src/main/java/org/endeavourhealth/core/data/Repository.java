package org.endeavourhealth.core.data;

import com.datastax.driver.core.Session;

public abstract class Repository {
    private Session session;
    private PreparedStatementCache statementCache;

    protected Session getSession() {
        if (session == null) {
            session = CassandraConnector.getInstance().getSession();
        }

        return session;
    }

    protected PreparedStatementCache getStatementCache() {
        if (statementCache == null) {
            statementCache = CassandraConnector.getInstance().getStatementCache();
        }

        return statementCache;
    }
}
