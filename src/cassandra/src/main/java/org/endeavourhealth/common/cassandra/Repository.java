package org.endeavourhealth.common.cassandra;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;

public abstract class Repository {
    private Session session;
    private PreparedStatementCache statementCache;
    private MappingManager mappingManager;

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

    protected MappingManager getMappingManager() {
        if (mappingManager == null) {
            mappingManager = CassandraConnector.getInstance().getMappingManager();
        }

        return mappingManager;
    }

}
