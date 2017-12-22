package org.endeavourhealth.queuereader;

import com.google.common.base.Strings;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.core.configuration.QueueReaderConfiguration;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.QueuedMessageDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RabbitConsumer extends DefaultConsumer {
	private static final Logger LOG = LoggerFactory.getLogger(RabbitConsumer.class);

	private static final int DEFAULT_MAX_ATTEMPTS = 10;

	private final QueueReaderConfiguration configuration;
	private final String configId;
	private final RabbitHandler handler;
	private final PipelineProcessor pipeline;
	private final int attemptsBeforeFailure;

	private UUID lastExchangeAttempted;
	private int lastExchangeAttempts;

	public RabbitConsumer(Channel channel, QueueReaderConfiguration configuration, String configId, RabbitHandler handler) {
		super(channel);

		this.configuration = configuration;
		this.configId = configId;
		this.handler = handler;
		this.pipeline = new PipelineProcessor(configuration.getPipeline());

		if (configuration.getAttemptsPermitted() == null) {
			this.attemptsBeforeFailure = DEFAULT_MAX_ATTEMPTS;
		} else {
			this.attemptsBeforeFailure = configuration.getAttemptsPermitted().intValue();
		}
	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] bytes) throws IOException {
		// Decode the message id
		String messageId = new String(bytes, "UTF-8");
		UUID messageUuid = UUID.fromString(messageId);

		// Get the message from the db
		String queuedMessageBody = null;

		try {
			QueuedMessageDalI queuedMessageDal = DalProvider.factoryQueuedMessageDal();
			queuedMessageBody = queuedMessageDal.getById(messageUuid);
		} catch (Exception ex) {
			LOG.error("Failed to retrieve queued message " + messageId, ex);
			this.getChannel().basicAck(envelope.getDeliveryTag(), false);
			return;
		}

		//seem to get brokwn messages in dev environments, so handle for now
		if (queuedMessageBody == null) {
			LOG.warn("Received queued message ID " + messageId + " with no actual message");
			this.getChannel().basicAck(envelope.getDeliveryTag(), false);
			return;
		}

		Exchange exchange = new Exchange();
		exchange.setId(messageUuid);
		exchange.setBody(queuedMessageBody);
		exchange.setTimestamp(new Date());
		exchange.setHeaders(new HashMap<>());

		Map<String, Object> headers = properties.getHeaders();
		if (headers != null) {
			headers.keySet().stream()
					.filter(headerKey -> headers.get(headerKey) != null)
					.forEach(headerKey -> exchange.setHeader(headerKey, headers.get(headerKey).toString()));
		}
		LOG.info("Received exchange {} from Rabbit", exchange.getId());

		// Process the message
		if (pipeline.execute(exchange)) {
			//LOG.info("Successfully processed exchange {}", exchange.getExchangeId());
			this.getChannel().basicAck(envelope.getDeliveryTag(), false);
			//LOG.info("Have sent ACK for exchange {}", exchange.getExchangeId());

			//when we successfully process something, clear this
			lastExchangeAttempted = null;

		} else {
			//LOG.error("Failed to process exchange {}", exchange.getExchangeId());
			this.getChannel().basicReject(envelope.getDeliveryTag(), true);
			LOG.error("Have sent REJECT for exchange {}", exchange.getId());

			//keep track of rejections
			updateAttemptsOnFailure(exchange);
		}

		//see if we've been told to finish
		if (checkIfKillFileExists()) {
			String reason = "Detected kill file";
			stop(reason);
		}
	}

	private void updateAttemptsOnFailure(Exchange exchange) {
		UUID exchangeIdAttempted = exchange.getId();

		//if it's the same exchange ID as last time, increment the number of attempts
		if (lastExchangeAttempted != null
				&& lastExchangeAttempted.equals(exchangeIdAttempted)) {
			this.lastExchangeAttempts ++;

		} else {
			//if it's a different change to last time, reset the attempt to one
			this.lastExchangeAttempted = exchangeIdAttempted;
			this.lastExchangeAttempts = 1;

			//send a slack message for the first failure
			String queueName = configuration.getQueue();
			String s = "Exchange " + exchangeIdAttempted + " rejected in " + queueName;
			SlackHelper.sendSlackMessage(SlackHelper.Channel.QueueReaderAlerts, s, exchange.getException());
		}

		//if we've failed on the same exchange X times, then halt the queue reader
		if (lastExchangeAttempts >= attemptsBeforeFailure) {
			String reason = "Failed " + lastExchangeAttempts + " times on exchange " + lastExchangeAttempted + " so halting queue reader";
			stop(reason);
		}
	}

	private void stop(String reason) {

		//close down the rabbit connection and channel
		try {
			handler.stop();
		} catch (Exception ex) {
			LOG.error("Failed to close Rabbit channel or connection", ex);
		}

		//tell us this has happened
		SlackHelper.sendSlackMessage(SlackHelper.Channel.QueueReaderAlerts, "Queue Reader " + configId + " Stopping:\r\n" + reason);

		//and halt
		LOG.info("Queue Reader " + ConfigManager.getAppId() + " exiting: " + reason);
		System.exit(0);
	}

	/**
	 * checks to see if a file exists that tells us to finish processing and stop
     */
	private boolean checkIfKillFileExists() {

		String killFileLocation = configuration.getKillFileLocation();
		if (Strings.isNullOrEmpty(killFileLocation)) {
			LOG.error("No kill file location set in app configuration XML");
			return false;
		}

		File killFile = new File(killFileLocation, configId + ".kill");
		if (killFile.exists()) {
			LOG.info("Kill file detected: " + killFile);
			//and delete so we don't need to manually delete it
			killFile.delete();
			return true;

		} else {
			//LOG.trace("Kill file not found: " + killFile); //for investigation
			return false;
		}
	}
}
