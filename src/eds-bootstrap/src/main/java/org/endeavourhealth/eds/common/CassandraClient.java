package org.endeavourhealth.eds.common;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.endeavourhealth.eds.bootstrap.models.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraClient {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraClient.class);

    private Cluster cluster;

    private Session session;

    public void connect(Config config) {

        int port = config.getCassandra().getPort();
        String[] contactPoints = new String[] { config.getCassandra().getUrl() };

        cluster = Cluster.builder()
                .addContactPoints(contactPoints).withPort(port)
                .build();

        // TODO: add username and password

        LOG.info("Connected to cluster: {}", cluster.getMetadata().getClusterName());

        session = cluster.connect();
    }

    public void close() {
        session.close();
        cluster.close();
    }

    public Session getSession() {
        return session;
    }
}
