package org.endeavourhealth.core.queueing;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class ConnectionManager {
	private static final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);
	private static final Map<Integer, Connection> connectionPool = new HashMap<>();

	public static Connection getConnection(String username, String password, String nodes) throws IOException, TimeoutException {
		Integer hash = (username + password + nodes).hashCode();

		Connection connection = connectionPool.get(hash);

		if (connection == null || !connection.isOpen()) {
			// Connection pooling
			ConnectionFactory connectionFactory = new ConnectionFactory();
			connectionFactory.setAutomaticRecoveryEnabled(true);
			connectionFactory.setTopologyRecoveryEnabled(true);
			connectionFactory.setUsername(username);
			connectionFactory.setPassword(password);

			Address[] addresses = Address.parseAddresses(nodes);

			connection = connectionFactory.newConnection(addresses);
			connectionPool.put(hash, connection);
		}

		return connection;
	}
}
