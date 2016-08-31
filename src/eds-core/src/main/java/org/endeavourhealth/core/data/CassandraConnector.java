package org.endeavourhealth.core.data;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import org.endeavourhealth.core.engineConfiguration.Cassandra;
import org.endeavourhealth.core.engineConfiguration.EngineConfigurationSerializer;

public class CassandraConnector {
    private static CassandraConnector instance = null;

    private final Cluster cluster;
    private final Session session;
    private final PreparedStatementCache statementCache;
    private final MappingManager mappingManager;

    private CassandraConnector() {
        Cassandra config = EngineConfigurationSerializer.getConfig().getCassandra();

        Cluster.Builder builder = Cluster.builder()
                .withCredentials(config.getUsername(), config.getPassword());

        for (String node: config.getNode()) {
            builder = builder.addContactPoint(node);
        }

        //turn on Quorum consistency for everything by default
        builder.withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.QUORUM));

        cluster = builder.build();
        session = cluster.connect();
        statementCache =  new PreparedStatementCache(session);
        mappingManager = new MappingManager(session);

        registerCustomCodecs();
    }

    public Session getSession() {
        return session;
    }

    public PreparedStatementCache getStatementCache() {
        return statementCache;
    }

    public MappingManager getMappingManager() {
        return mappingManager;
    }

    private void registerCustomCodecs() {
        org.endeavourhealth.core.data.ehr.EnumRegistry.register();
    }

    public static CassandraConnector getInstance() {
        if (instance == null)
            instance = new CassandraConnector();

        return instance;
    }

    public void close() {
        //clear cached statements
        statementCache.clear();

        session.close();

        //close cluster
        cluster.close();
    }
}
