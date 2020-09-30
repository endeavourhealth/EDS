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
import org.endeavourhealth.core.configuration.ComponentConfig;
import org.endeavourhealth.core.configuration.Pipeline;
import org.endeavourhealth.core.configuration.QueueReaderConfiguration;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.QueuedMessageDalI;
import org.endeavourhealth.core.database.dal.audit.models.ApplicationHeartbeat;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineProcessor;
import org.endeavourhealth.core.messaging.pipeline.components.MessageTransformInbound;
import org.endeavourhealth.transform.common.TransformConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RabbitConsumer extends DefaultConsumer
							implements ApplicationHeartbeatCallbackI {
	private static final Logger LOG = LoggerFactory.getLogger(RabbitConsumer.class);

	private static final String FILE_EXT_KILL = "kill";
	private static final String FILE_EXT_RESTART = "restart";

	private static final int EXIT_CODE_STOP = 0;
	private static final int EXIT_CODE_RESTART = 1025;

	//variables to prevent duplicate processing in the event of Rabbit fail-overs
	private static final List<RabbitConsumer_State> messagesBeingProcessed = new ArrayList<>();

	//members-
	private final QueueReaderConfiguration configuration;
	private final String configId;
	private final RabbitHandler handler;
	private final PipelineProcessor pipeline;
	private int instanceNumber;
	private Boolean cachedIsInboundQueueReader;

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
		checkIfKillFileExists(FILE_EXT_RESTART);
		deleteExitCodeFile();

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
			LOG.info(configId + " received exchange " + exchange.getId() + " for " + exchange.getHeader(HeaderKeys.SenderLocalIdentifier));

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
			for (RabbitConsumer_State existingState : messagesBeingProcessed) {
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
			this.lastExchangeAttempts++;

		} else {
			//if it's a different change to last time, reset the attempt to one
			this.lastExchangeAttempted = processingState;
			this.lastExchangeAttempts = 1;

			//send a slack message for the first failure
			String queueName = configuration.getQueue();
			String hostName = getHostName();
			String s = "Exchange " + processingState.getExchangeId() + " rejected in " + queueName + " on " + hostName;
			SlackHelper.sendSlackMessage(SlackHelper.Channel.QueueReaderAlerts, s, exception);
		}

		//if we've failed on the same exchange X times, then halt the queue reader
		if (lastExchangeAttempts >= TransformConfig.instance().getAttemptsPermmitedPerExchange()) {
			String reason = "Failed " + lastExchangeAttempts + " times on exchange " + lastExchangeAttempted.getExchangeId() + " so halting queue reader";
			stop(reason, 1024); //just use non-standard exit code
		}
	}

	private static String getHostName() {
		try {
			return MetricsHelper.getHostName();
		} catch (IOException ioe) {
			LOG.error("Error getting host name", ioe);
			return "UNKNOWN_HOST";
		}
	}

	private void stop(String reason, int exitCode) {

		//stop our checking thread
		this.killCheckingRunnable.stop();

		//close down the rabbit connection and channel
		try {
			handler.stop();
		} catch (Exception ex) {
			LOG.error("Failed to close Rabbit channel or connection", ex);
		}

		//and halt
		LOG.info("Queue Reader " + ConfigManager.getAppId() + " exiting: " + reason);

		//write out exit code to file - due to RabbitMQ and/or Hikari, we believe the exit code passed to System.exit(..) gets overwritten
		writeExitCodeFileAndExit(exitCode);
	}

	private void writeExitCodeFileAndExit(int exitCode) {
		File f = getExitCodeFile();
		if (f != null) {
			String s = "" + exitCode;
			byte[] bytes = s.getBytes();
			try {
				Files.write(f.toPath(), bytes, StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException ioe) {
				LOG.error("Failed to write exit file", ioe);
			}
		}

		System.exit(exitCode);
	}

	private void deleteExitCodeFile() {
		File f = getExitCodeFile();
		if (f != null
				&& f.exists()) {
			f.delete();
		}
	}

	private File getExitCodeFile() {
		String killFileLocation = TransformConfig.instance().getKillFileLocation();
		if (Strings.isNullOrEmpty(killFileLocation)) {
			LOG.error("No kill file location set in common queue reader config");
			return null;
		}

		return new File(killFileLocation, configId + ".exit");
	}

	/**
	 * This fn is called from multiple threads, so it syncronised to ensure that checks can't overlap
	 * and ensures only the first check will return true, as the file is deleted
	 */
	private synchronized void checkIfKillFileExistsAndStopIfSo() {
		if (checkIfKillFileExists(FILE_EXT_KILL)) {
			String reason = "Detected kill file";
			stop(reason, EXIT_CODE_STOP);

		} else if (checkIfKillFileExists(FILE_EXT_RESTART)) {
			String reason = "Detected restart file";
			stop(reason, EXIT_CODE_RESTART);
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

		String desc = isBusyDesc();

		boolean isBusy = !Strings.isNullOrEmpty(desc);
		applicationHeartbeat.setBusy(new Boolean(isBusy));
		applicationHeartbeat.setIsBusyDetail(desc); //give some more detail about what we're doing
	}

	@Override
	public void populateInstanceNumber(ApplicationHeartbeat applicationHeartbeat) {
		applicationHeartbeat.setApplicationInstanceNumber(this.instanceNumber);
	}

	private boolean isBusy() {
		return !Strings.isNullOrEmpty(isBusyDesc());
	}

	public int getInstanceNumber() {
		return instanceNumber;
	}

	public void setInstanceNumber(int instanceNumber) {
		this.instanceNumber = instanceNumber;
	}

	/**
	 * provides a short summary of what the QR is currently doing
	 */
	private String isBusyDesc() {

		StringBuilder sb = new StringBuilder();

		List<RabbitConsumer_State> messages = getMessagesBeingProcessedCopy();
		for (RabbitConsumer_State state : messages) {
			Object odsCode = state.getHeader(HeaderKeys.SenderLocalIdentifier);
			Object dataDate = state.getHeader(HeaderKeys.DataDate);
			String dtCreatedDesc = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(state.getDtCreated());

			if (dataDate != null) {
				sb.append(dataDate.toString());
				sb.append(" ");
			}
			sb.append("data for "); //please do not change this
			sb.append(odsCode.toString());
			sb.append(" since ");
			sb.append(dtCreatedDesc);
		}

		if (sb.length() == 0) {
			return null;
		} else {
			return sb.toString();
		}
	}

	private boolean isInboundQueueReader() {
		if (cachedIsInboundQueueReader == null) {
			Pipeline pipeline = configuration.getPipeline();
			List<ComponentConfig> components = pipeline.getPipelineComponents();
			for (ComponentConfig config : components) {
				String xmlTagName = config.getClass().getSimpleName();
				if (xmlTagName.equals("MessageTransformInboundConfig")) {
					cachedIsInboundQueueReader = Boolean.TRUE;
					break;
				}
			}

			if (cachedIsInboundQueueReader == null) {
				cachedIsInboundQueueReader = Boolean.FALSE;
			}
		}
		return cachedIsInboundQueueReader.booleanValue();
	}

	/**
	 * we sometimes need to make a service "auto-fail" to get it out of a queue that it's blocking
	 * rather than doing that and then manually restarting the Queue Reader, this function will run
	 * every minute and will check to see if that's happened and will restart the app if so
     */
	private void checkIfServiceHasBecomeAutoFailAndStopIfSo() throws Exception {

		//this is only applicable if we're an INBOUND queue reader, so make sure our configuration matches
		if (!isInboundQueueReader()) {
			return;
		}

		List<RabbitConsumer_State> messages = getMessagesBeingProcessedCopy();
		for (RabbitConsumer_State state : messages) {

			Object serviceIdObj = state.getHeader(HeaderKeys.SenderServiceUuid);
			Object systemIdObj = state.getHeader(HeaderKeys.SenderSystemUuid);
			if (serviceIdObj == null) {
				LOG.error("NULL service ID in exchange " + state.getExchangeId());
				return;
			}
			if (systemIdObj == null) {
				LOG.error("NULL system ID in exchange " + state.getExchangeId());
				return;
			}
			UUID serviceId = UUID.fromString(serviceIdObj.toString());
			UUID systemId = UUID.fromString(systemIdObj.toString());

			boolean isAutoFailNow = MessageTransformInbound.shouldAutoFailExchange(serviceId, systemId);
			Boolean wasAutoFail = state.getServiceWasAutoFail();

			if (wasAutoFail == null) {
				//if null, then this is our first time checking this service, so just work out if it's auto fail initially
				state.setServiceWasAutoFail(new Boolean(isAutoFailNow));

			} else {
				//if non-null then compare and see if it's changed since we started processing
				if (isAutoFailNow && !wasAutoFail.booleanValue()) {
					stop("Auto-fail state changed on service " + serviceId, EXIT_CODE_RESTART);
				}
			}
		}
	}

	private List<RabbitConsumer_State> getMessagesBeingProcessedCopy() {

		List<RabbitConsumer_State> ret = null;

		synchronized (messagesBeingProcessed) {
			ret = new ArrayList<>(messagesBeingProcessed);
		}

		return ret;
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

			try {

				while (!isStopped()) {

					//wait a bit
					try {
						Thread.sleep(10000);
					} catch (InterruptedException ie) {
						//do nothing
					}

					if (isBusy()) {
						//if we're mid-way through processing an exchange, check to see if the service it's for has
						//BECOME auto-fail since we started processing it. If so, just exit and restart so it
						//gets picked up from fresh and goes into error
						checkIfServiceHasBecomeAutoFailAndStopIfSo();

					} else {
						//if our rabbit processor is processing an exchange, then don't check for the kill file
						//since we don't want to kill anything mid-way through running
						checkIfKillFileExistsAndStopIfSo();
					}
				}

			} catch (Throwable t) {
				LOG.error("", t);
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
	private Date dtCreated;
	private Boolean serviceWasAutoFail;

	public RabbitConsumer_State(byte[] body, Map<String, Object> headers) {
		this.body = body;
		this.headers = headers;
		this.dtCreated = new Date();
	}

	public byte[] getBody() {
		return body;
	}

	public Object getHeader(String key) {
		return headers.get(key);
	}

	public Date getDtCreated() {
		return dtCreated;
	}

	@Override
	public boolean equals(Object o) {

		if (o == this) {
			return true;
		}

		if (!(o instanceof RabbitConsumer_State)) {
			return false;
		}

		RabbitConsumer_State other = (RabbitConsumer_State) o;
		if (body.length != other.body.length) {
			return false;
		}

		for (int i = 0; i < body.length; i++) {
			if (body[i] != other.body[i]) {
				return false;
			}
		}

		if (headers.size() != other.headers.size()) {
			return false;
		}

		for (String key : headers.keySet()) {
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

	public Boolean getServiceWasAutoFail() {
		return serviceWasAutoFail;
	}

	public void setServiceWasAutoFail(Boolean serviceWasAutoFail) {
		this.serviceWasAutoFail = serviceWasAutoFail;
	}
}
