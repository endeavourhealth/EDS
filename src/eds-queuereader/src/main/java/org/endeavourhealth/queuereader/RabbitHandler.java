package org.endeavourhealth.queuereader;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.endeavourhealth.core.configuration.QueueReaderConfiguration;
import org.endeavourhealth.core.queueing.ConnectionManager;
import org.endeavourhealth.core.queueing.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitHandler {
	private static final Logger LOG = LoggerFactory.getLogger(RabbitHandler.class);

	private String configId = null;
	private QueueReaderConfiguration configuration;
	private Connection connection;
	private Channel channel;
	private RabbitConsumer consumer;

	public RabbitHandler(QueueReaderConfiguration configuration, String configId) throws Exception {
		LOG.info("Connecting to Rabbit queue {} at {}", configuration.getQueue(), RabbitConfig.getInstance().getNodes());

		this.configId = configId;
		this.configuration = configuration;

		// Connect to rabbit cluster
		connection = createRabbitConnection();

		// Create a channel
		channel = connection.createChannel();
		channel.basicQos(1);

		// Create consumer
		consumer = new RabbitConsumer(channel, configuration, configId, this);
	}

	private Connection createRabbitConnection() throws IOException, TimeoutException {

		//use same connection code as for publishing, to save on duplicating the code
		return ConnectionManager.getConnection(
				RabbitConfig.getInstance().getUsername(),
				RabbitConfig.getInstance().getPassword(),
				RabbitConfig.getInstance().getNodes(),
				RabbitConfig.getInstance().getSslProtocol(),
				false);

		/*ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setAutomaticRecoveryEnabled(true);
		connectionFactory.setTopologyRecoveryEnabled(true);
		connectionFactory.setUsername(RabbitConfig.getInstance().getUsername());
		connectionFactory.setPassword(RabbitConfig.getInstance().getPassword());

		String sslProtocol = RabbitConfig.getInstance().getSslProtocol();
		if (!Strings.isNullOrEmpty(sslProtocol)) {
			try {
				connectionFactory.useSslProtocol(sslProtocol);
			} catch (Exception ex) {
				throw new IOException("Failed to initialise SSL protocol [" + sslProtocol + "]", ex);
			}
		}

		Address[] addresses = Address.parseAddresses(RabbitConfig.getInstance().getNodes());
		return connectionFactory.newConnection(addresses);*/
	}

	public void start() throws IOException {
		// Begin consuming messages
		channel.queueDeclare(
				configuration.getQueue(),
				true,		// Durable
				false, 	// Exclusive
				false, 	// Auto delete
				null);

		//pass true for the exclusive parameter, so we can only have one consumer per queue
		//channel.basicConsume(configuration.getQueue(), false, consumer);
		channel.basicConsume(configuration.getQueue(), false, configId, false, true, null, consumer);
	}

	public void stop() throws IOException, TimeoutException {
		// Close channel
		channel.close();

		// Close connection
		connection.close();
	}
}
