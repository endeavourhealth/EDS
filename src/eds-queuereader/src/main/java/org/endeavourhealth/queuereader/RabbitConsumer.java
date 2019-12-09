package org.endeavourhealth.queuereader;

import com.google.common.base.Strings;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.utility.MetricsHelper;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.core.application.ApplicationHeartbeatCallbackI;
import org.endeavourhealth.core.configuration.QueueReaderConfiguration;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.QueuedMessageDalI;
import org.endeavourhealth.core.database.dal.audit.models.ApplicationHeartbeat;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineProcessor;
import org.endeavourhealth.transform.common.TransformConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RabbitConsumer extends DefaultConsumer
							implements ApplicationHeartbeatCallbackI {
	private static final Logger LOG = LoggerFactory.getLogger(RabbitConsumer.class);

	private static final String FILE_EXT_KILL = "kill";
	private static final String FILE_EXT_KILL_QUIET = "killQuiet";

	//variables to prevent duplicate processing in the event of Rabbit fail-overs
	private static final List<RabbitConsumer_State> messagesBeingProcessed = new ArrayList<>();

	//members
	private final QueueReaderConfiguration configuration;
	private final String configId;
	private final RabbitHandler handler;
	private final PipelineProcessor pipeline;

	private RabbitConsumer_State lastExchangeAttempted;
	private int lastExchangeAttempts;
	private KillCheckingRunnable killCheckingRunnable;

	public RabbitConsumer(Channel channel, QueueReaderConfiguration configuration, String configId, RabbitHandler handler) {
		super(channel);

		this.configuration = configuration;
		this.configId = configId;
		this.handler = handler;
		this.pipeline = new PipelineProcessor(configuration.getPipeline());

		//call this to delete any pre-existing kill file
		checkIfKillFileExists(FILE_EXT_KILL);
		checkIfKillFileExists(FILE_EXT_KILL_QUIET);

		startKillCheckingThread();
	}

	/**
	 * starts off the thread that periodically checks for a kill file, idenpendently of whether RabbitMQ is
	 * giving us anything to service
	 */
	private void startKillCheckingThread() {
		this.killCheckingRunnable = new KillCheckingRunnable();
		Thread t = new Thread(killCheckingRunnable);
		t.setName("KillChecker");
		t.start();
	}


	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] bytes) throws IOException {

		MetricsHelper.recordEvent("message_received");

		RabbitConsumer_State processingState = findProcessingState(bytes, properties.getHeaders());

		//check if we should process the message or not
		long deliveryTag = envelope.getDeliveryTag();
		if (!processMessageNow(processingState, deliveryTag)) {
			return;
		}

		boolean shouldAck = false;

		Exchange exchange = decodeExchange(properties, bytes);
		if (exchange != null) {
			processingState.setExchangeId(exchange.getId());
			LOG.info("Received " + configId + " exchange " + exchange.getId() + " from Rabbit");

			// Process the message
			if (pipeline.execute(exchange)) {
				shouldAck = true;

				//when we successfully process something, clear this
				lastExchangeAttempted = null;

			} else {
				//keep track of rejections and send slack messages if required
				updateAttemptsOnFailure(processingState, exchange.getException());
			}
		}

		//update our state and send the ack or nak
		processingState.setShouldAck(new Boolean(shouldAck));
		ackOrNak(processingState, deliveryTag);

		//tidy up and remove our processing state object
		processingFinished(processingState);

		//see if we've been told to finish
		checkIfKillFileExistsAndStopIfSo();
	}

	private static void processingFinished(RabbitConsumer_State processingState) {

		int countThreadsProcessing = processingState.getProcessingCount().decrementAndGet();
		if (countThreadsProcessing == 0) {
			//if there are no more threads processing this delivery tag, then we just need to remove from the map
			removeProcessingState(processingState);
			return;
		}

		//if we have one or more threads waiting on our result, then notify them now
		synchronized (processingState) {
			processingState.notifyAll();
		}
	}


	private boolean processMessageNow(RabbitConsumer_State processingState, long deliveryTag) throws IOException {

		int countThreadsProcessing = processingState.getProcessingCount().incrementAndGet();

		//if there's only one thread processing the message, then return true to let the pipeline processing happen
		if (countThreadsProcessing == 1) {
			return true;
		}

		//if'we already processing this delivery tag, then we've had a RabbitMQ restart or failover,
		//in which case we don't want to just start processing the message, since it will already be in progress.
		//Instead, we increment our wait count on the state object and then wait until the first request is complete
		LOG.warn("Already processing message for exchange " + processingState.getExchangeId() + " so will wait until it completes - possible RabbitMQ restart or failover?");

		//keep waiting until we know whether we should ack or nak
		synchronized (processingState) {
			while (processingState.getShouldAck() == null) {
				try {
					processingState.wait();
				} catch (InterruptedException ex) {
					LOG.error("", ex);
				}
			}
		}

		LOG.warn("Original processing of exchange " + processingState.getExchangeId() + " is complete, so will now ACK/NAK the re-request");
		ackOrNak(processingState, deliveryTag);

		processingFinished(processingState);

		return false;
	}

	private static RabbitConsumer_State findProcessingState(byte[] body, Map<String, Object> headers) {

		synchronized (messagesBeingProcessed) {

			RabbitConsumer_State potentialNewState = new RabbitConsumer_State(body, headers);
			for (RabbitConsumer_State existingState: messagesBeingProcessed) {
				if (existingState.equals(potentialNewState)) {
					return existingState;
				}
			}
			messagesBeingProcessed.add(potentialNewState);
			return potentialNewState;
		}
	}

	private static void removeProcessingState(RabbitConsumer_State processingState) {
		synchronized (messagesBeingProcessed) {
			messagesBeingProcessed.remove(processingState);
		}
	}


	private void ackOrNak(RabbitConsumer_State processingState, long deliveryTag) throws IOException {

		boolean shouldAck = processingState.getShouldAck().booleanValue();
		UUID exchangeId = processingState.getExchangeId();

		if (shouldAck) {
			//LOG.info("Successfully processed exchange " + exchangeId);
			this.getChannel().basicAck(deliveryTag, false);
			//LOG.info("Have sent ACK for exchange {}", exchange.getExchangeId());

		} else {

			this.getChannel().basicReject(deliveryTag, true);
			LOG.error("Have sent REJECT for exchange " + exchangeId);
		}
	}

	private static Exchange decodeExchange(AMQP.BasicProperties properties, byte[] bytes) throws IOException {

		// Decode the message id
		String exchangeId = new String(bytes, "UTF-8");
		UUID exchangeUuid = UUID.fromString(exchangeId);

		// Get the message from the db
		String queuedMessageBody = null;

		try {
			QueuedMessageDalI queuedMessageDal = DalProvider.factoryQueuedMessageDal();
			queuedMessageBody = queuedMessageDal.getById(exchangeUuid);
		} catch (Exception ex) {
			LOG.error("Failed to retrieve queued message " + exchangeId, ex);
			return null;
		}

		//seem to get brokwn messages in dev environments, so handle for now
		if (queuedMessageBody == null) {
			LOG.error("Received queued message ID " + exchangeId + " with no actual message");
			return null;
		}


		Exchange exchange = new Exchange();
		exchange.setId(exchangeUuid);
		exchange.setBody(queuedMessageBody);
		exchange.setTimestamp(new Date());
		exchange.setHeaders(new HashMap<>());

		Map<String, Object> headers = properties.getHeaders();
		if (headers != null) {
			headers.keySet().stream()
					.filter(headerKey -> headers.get(headerKey) != null)
					.forEach(headerKey -> exchange.setHeader(headerKey, headers.get(headerKey).toString()));
		}

		//populate these fields
		exchange.setServiceId(exchange.getHeaderAsUuid(HeaderKeys.SenderServiceUuid));
		exchange.setSystemId(exchange.getHeaderAsUuid(HeaderKeys.SenderSystemUuid));

		return exchange;
	}

	private void updateAttemptsOnFailure(RabbitConsumer_State processingState, Exception exception) {

		//if it's the same exchange ID as last time, increment the number of attempts
		if (lastExchangeAttempted != null
				&& lastExchangeAttempted.equals(processingState)) {
			this.lastExchangeAttempts ++;

		} else {
			//if it's a different change to last time, reset the attempt to one
			this.lastExchangeAttempted = processingState;
			this.lastExchangeAttempts = 1;

			//send a slack message for the first failure
			String queueName = configuration.getQueue();
			String s = "Exchange " + processingState.getExchangeId() + " rejected in " + queueName;
			SlackHelper.sendSlackMessage(SlackHelper.Channel.QueueReaderAlerts, s, exception);
		}

		//if we've failed on the same exchange X times, then halt the queue reader
		if (lastExchangeAttempts >= TransformConfig.instance().getAttemptsPermmitedPerExchange()) {
			String reason = "Failed " + lastExchangeAttempts + " times on exchange " + lastExchangeAttempted.getExchangeId() + " so halting queue reader";
			stop(reason, true);
		}
	}

	private void stop(String reason, boolean sendSlackMessageIfPossible) {

		//stop our checking thread
		this.killCheckingRunnable.stop();

		//close down the rabbit connection and channel
		try {
			handler.stop();
		} catch (Exception ex) {
			LOG.error("Failed to close Rabbit channel or connection", ex);
		}

		//tell us this has happened
		if (sendSlackMessageIfPossible) {
			String host;
			try {
				host = MetricsHelper.getHostName();
			} catch (IOException ioe) {
				host = "UNKNOWN";
			}
			SlackHelper.sendSlackMessage(SlackHelper.Channel.QueueReaderAlerts, "Queue Reader " + configId + " Stopping on " + host + ":\r\n" + reason);
		}

		//and halt
		LOG.info("Queue Reader " + ConfigManager.getAppId() + " exiting: " + reason);
		System.exit(0);
	}

	/**
	 * This fn is called from multiple threads, so it syncronised to ensure that checks can't overlap
	 * and ensures only the first check will return true, as the file is deleted
	 */
	private synchronized void checkIfKillFileExistsAndStopIfSo() {
		if (checkIfKillFileExists(FILE_EXT_KILL)) {
			String reason = "Detected kill file";
			stop(reason, true);

		} else if (checkIfKillFileExists(FILE_EXT_KILL_QUIET)) {
			String reason = "Detected quiet kill file";
			stop(reason, false);
		}
	}

	/**
	 * checks to see if a file exists that tells us to finish processing and stop
     */
	private boolean checkIfKillFileExists(String extension) {

		String killFileLocation = TransformConfig.instance().getKillFileLocation();
		if (Strings.isNullOrEmpty(killFileLocation)) {
			LOG.error("No kill file location set in common queue reader config");
			return false;
		}

		File killFile = new File(killFileLocation, configId + "." + extension);
		if (killFile.exists()) {

			//delete so we don't need to manually delete it
			killFile.delete();
			return true;

		} else {
			//LOG.trace("Kill file not found: " + killFile); //for investigation
			return false;
		}
	}

	/**
	 * this fn is called every minute from the heartbeat thread, to see if we're busy
     */
	@Override
	public void populateIsBusy(ApplicationHeartbeat applicationHeartbeat) {
		applicationHeartbeat.setBusy(new Boolean(isBusy()));
	}

	private boolean isBusy() {
		boolean busy;
		synchronized (messagesBeingProcessed) {
			busy = !messagesBeingProcessed.isEmpty();
		}
		return busy;
	}

	/**
	 * runnable to check for the kill file every 10s. The normal Rabbit processing checks at the end of each
	 * message, but if the app is idle, this thread takes over.
	 */
	class KillCheckingRunnable implements Runnable {

		private AtomicInteger stop = new AtomicInteger();

		public void stop() {
			stop.incrementAndGet();
		}

		public boolean isStopped() {
			return stop.get() > 0;
		}

		@Override
		public void run() {

			while (!isStopped()) {

				//wait a bit
				try {
					Thread.sleep(10000);
				} catch (InterruptedException ie) {
					//do nothing
				}

				//if our rabbit processor is running something, then do nothing
				//since we don't want to kill anything mid-way through running
				if (isBusy()) {
					continue;
				}

				checkIfKillFileExistsAndStopIfSo();
			}
		}
	}
}

/**
 * storage object used to track the outcome of rabbit messages and
 * how we responded. Purely used to get around receiving the same message
 * twice when RabbitMQ fails over or restarts
 */
class RabbitConsumer_State {

	private byte[] body = null;
	private Map<String, Object> headers = null;
	private AtomicInteger processingCount = new AtomicInteger(); //count of threads processing the same delivery tag
	private Boolean shouldAck;
	private UUID exchangeId;

	public RabbitConsumer_State(byte[] body, Map<String, Object> headers) {
		this.body = body;
		this.headers = headers;
	}

	@Override
	public boolean equals(Object o) {

		if (o == this) {
			return true;
		}

		if (!(o instanceof RabbitConsumer_State)) {
			return false;
		}

		RabbitConsumer_State other = (RabbitConsumer_State)o;
		if (body.length != other.body.length) {
			return false;
		}

		for (int i=0; i<body.length; i++) {
			if (body[i] != other.body[i]) {
				return false;
			}
		}

		if (headers.size() != other.headers.size()) {
			return false;
		}

		for (String key: headers.keySet()) {
			Object val = headers.get(key);
			Object otherVal = other.headers.get(key);
			if ((val == null) != (otherVal == null)) {
				return false;
			}
			if (val != null
					&& !val.equals(otherVal)) {
				return false;
			}
		}

		return true;
	}

	public AtomicInteger getProcessingCount() {
		return processingCount;
	}

	public Boolean getShouldAck() {
		return shouldAck;
	}

	public void setShouldAck(Boolean shouldAck) {
		this.shouldAck = shouldAck;
	}

	public UUID getExchangeId() {
		return exchangeId;
	}

	public void setExchangeId(UUID exchangeId) {
		this.exchangeId = exchangeId;
	}


}