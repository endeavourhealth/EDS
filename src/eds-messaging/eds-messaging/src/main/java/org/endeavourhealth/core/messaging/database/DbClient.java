package org.endeavourhealth.core.messaging.database;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.google.common.base.Strings;
import org.endeavourhealth.core.messaging.configuration.schema.engineConfiguration.Cassandra;
import org.endeavourhealth.core.messaging.configuration.schema.engineConfiguration.EngineConfiguration;
import org.endeavourhealth.core.messaging.configuration.schema.engineConfiguration.EngineConfigurationSerializer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DbClient {
    private static DbClient instance = new DbClient();

    private final Cluster cluster;
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Session, PreparedStatementCache> statementCaches = new ConcurrentHashMap<>();

    private DbClient() {

        EngineConfiguration config = EngineConfigurationSerializer.getConfig();
        Cassandra cassandraConfig = config.getCassandra();
        List<String> nodes = cassandraConfig.getNode();
        String username = cassandraConfig.getUsername();
        String password = cassandraConfig.getPassword();

        Cluster.Builder b = Cluster.builder();

        for (String node: nodes) {
            b = b.addContactPoint(node);
        }

        if (!Strings.isNullOrEmpty(username)
            && !Strings.isNullOrEmpty(password)) {
            b = b.withCredentials(username, password);
        }
        cluster = b.build();
    }

    public Session getSession(String keyspace) {
        return sessions.computeIfAbsent(keyspace, key -> cluster.connect(key));
    }

    public PreparedStatementCache getStatementCache(Session session) {
        return statementCaches.computeIfAbsent(session, key -> new PreparedStatementCache(key));
    }

    public static DbClient getInstance() {
        return instance;
    }

    public void close() {
        //clear cached statements
        for(PreparedStatementCache cache : statementCaches.values()) {
            cache.clear();
        }
        statementCaches.clear();

        //close sessions
        for(Session session : sessions.values()) {
            session.close();
        }
        sessions.clear();

        //close cluster
        cluster.close();
    }
}