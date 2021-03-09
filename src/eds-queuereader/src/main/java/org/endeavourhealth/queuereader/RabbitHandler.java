package org.endeavourhealth.queuereader;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.endeavourhealth.common.utility.MetricsHelper;
import org.endeavourhealth.core.application.ApplicationHeartbeatHelper;
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
	private boolean exclusiveReadingOnly;

	public RabbitHandler(QueueReaderConfiguration configuration, String configId) throws Exception {

		this.configId = configId;
		this.configuration = configuration;

		//some queues are allowed to be read by multiple Queue Readers, and the config tells us. If the config
		//doesn't, then assume it's exclusive reading only.
		this.exclusiveReadingOnly = configuration.isExclusive() == null || configuration.isExclusive().booleanValue();

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

	}

	public void start() throws Exception {

		LOG.info("Connecting to Rabbit queue {} at {} ", configuration.getQueue(), RabbitConfig.getInstance().getNodes());

		// Begin consuming messages
		AMQP.Queue.DeclareOk response = channel.queueDeclare(
				configuration.getQueue(),
				true,		// Durable
				false, 	// Exclusive (but not the same as the exclusive parameter used below)
				false, 	// Auto delete
				null);

		//the above return value tells us how many consumers that queue has, which we use in the heartbeat table
		int consumerCount = response.getConsumerCount();
		consumer.setInstanceNumber(consumerCount+1);
		LOG.info("Consumer number  :=" + consumerCount+1);

		//pass true for the exclusive parameter, so we can only have one consumer per queue
		//channel.basicConsume(configuration.getQueue(), false, consumer);
		channel.basicConsume(configuration.getQueue(), false, configId, false, exclusiveReadingOnly, null, consumer);

		//now we're running, start these
		MetricsHelper.startHeartbeat();
		ApplicationHeartbeatHelper.start(consumer);
	}

	public void stop() throws IOException, TimeoutException {
		// Close channel
		channel.close();

		// Close connection
		connection.close();
	}
}
