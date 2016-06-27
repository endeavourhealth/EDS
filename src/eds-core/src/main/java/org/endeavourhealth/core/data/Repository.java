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

    /**
     * generic functions for inserting, updating or deleting single records
     */
    protected <T> void insert(T toSave) {
        saveOrDelete(toSave, EventStoreMode.insert);
    }
    protected <T> void update(T toSave) {
        saveOrDelete(toSave, EventStoreMode.update);
    }
    protected <T> void delete(T toSave) {
        saveOrDelete(toSave, EventStoreMode.delete);
    }
    protected <T> void saveOrDelete(T toSave, EventStoreMode storeMode) {

        Class cls = (Class<T>)toSave.getClass();
        Mapper<T> mapper = getMappingManager().mapper(cls);

        BatchStatement batch = new BatchStatement();
        switch (storeMode) {
            case insert:
                batch.add(mapper.saveQuery(toSave));
                break;
            case update:
                batch.add(mapper.saveQuery(toSave)); //updates use saveQuery(..) as well as inserts
                break;
            case delete:
                batch.add(mapper.deleteQuery(toSave));
                break;
            default:
                throw new IllegalArgumentException("Invalid store mode " + storeMode);
        }

        getSession().execute(batch);
    }
}
