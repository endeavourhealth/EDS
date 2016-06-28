package org.endeavourhealth.core.data;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import org.endeavourhealth.core.data.ehr.models.PersonResource;
import org.endeavourhealth.core.data.ehr.models.PersonResourceEventStore;

public abstract class Repository {

    //TODO - should these Cassandra variables be static, so we only have a single session etc.?
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
