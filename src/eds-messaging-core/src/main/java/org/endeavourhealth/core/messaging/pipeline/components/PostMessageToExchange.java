package org.endeavourhealth.core.messaging.pipeline.components;

import com.google.common.base.Strings;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.core.configuration.PostMessageToExchangeConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.QueuedMessageDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.audit.models.QueuedMessageType;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.queueing.ConnectionManager;
import org.endeavourhealth.core.queueing.RabbitConfig;
import org.endeavourhealth.core.queueing.RoutingManager;
import org.endeavourhealth.transform.common.ExchangeHelper;
import org.endeavourhealth.transform.common.TransformConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class PostMessageToExchange extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(PostMessageToExchange.class);

	private PostMessageToExchangeConfig config;

	public PostMessageToExchange(PostMessageToExchangeConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {
		postToRabbit(exchange);
	}

	public boolean postToRabbit(Exchange exchange) throws PipelineException {

		//if the exchange is flagged so as to prevent re-queuing, then ignore it
		if (!ExchangeHelper.isAllowRequeueing(exchange)) {
			LOG.warn("Not queueing exchange " + exchange.getId() + " because flagged to prevent it");
			return false;
		}

		// Generate message identifier and store message in db
		UUID messageUuid = exchange.getId();
		try {
			QueuedMessageDalI queuedMessageDal = DalProvider.factoryQueuedMessageDal();
			queuedMessageDal.save(messageUuid, exchange.getBody(), QueuedMessageType.InboundData);
		} catch (Exception ex) {
			throw new PipelineException("Failed to save queued message", ex);
		}

		Connection connection = getConnection();
		Channel channel = getChannel(connection);

		//copy the headers from the exchange, since we may be changing it
		Map<String, String> headers = copyHeaders(exchange);
		boolean wasLastMessage = ExchangeHelper.isLastMessage(exchange);

		boolean bulkMode = isBulkMode(exchange);

		// Handle multicast
		String multicastHeader = config.getMulticastHeader();
		if (Strings.isNullOrEmpty(multicastHeader)) {

			//if we have no multicast setting, then just send a single message
			String routingKey = getRoutingKey(bulkMode, headers, config);
			publishMessage(routingKey, messageUuid, channel, headers, wasLastMessage, true);

		} else {
			//if we have a multicast setting, then get it out of the exchange header map
			String multicastData = exchange.getHeader(multicastHeader);
			if (Strings.isNullOrEmpty(multicastData)) {
				throw new PipelineException("No multicast data for " + multicastHeader + " to post exchange " + exchange.getId() + " to " + config.getExchange());
			}

			try {
				Object[] multicastItems = ObjectMapperPool.getInstance().readValue(multicastData, Object[].class);
				Throttle throttle = Throttle.factory(multicastItems.length, exchange.getId());

				for (int i=0; i<multicastItems.length; i++) {
					Object multicastItem = multicastItems[i];
					String itemData = ObjectMapperPool.getInstance().writeValueAsString(multicastItem);
					boolean isLastMessage = (i+1) == multicastItems.length;

					//replace multicast header with individual item
					headers.put(multicastHeader, itemData);
					String routingKey = getRoutingKey(bulkMode, headers, config);
					publishMessage(routingKey, messageUuid, channel, headers, wasLastMessage, isLastMessage);

					throttle.applyBreaks();
				}

			} catch (IOException e) {
				throw new PipelineException("Could not parse multicast data", e);
			}
		}

		waitForConfirmations(channel);
		closeChannel(channel);
		return true;
	}

	private static Map<String, String> copyHeaders(Exchange exchange) {
		Map<String, String> ret = new HashMap<>();
		for (String key : exchange.getHeaders().keySet()) {

			//skip the authorization header, since that's comparatively huge and there's no need to carry it through RabbbitMQ
			if (key.equalsIgnoreCase("authorization")) {
				continue;
			}

			ret.put(key, exchange.getHeader(key));
		}
		return ret;
	}

	private boolean isAddLastMessageFlag() {
		Boolean b = this.config.isAddLastMessageFlag();
		return b != null && b.booleanValue();
	}


	/**
	 * works out if the service and system have been set into "bulk" mode which is factored in to
	 * the routing, allowing us to route exchanges for services differently to how they otherwise would be
     */
	private static boolean isBulkMode(Exchange exchange) throws PipelineException {

		UUID serviceId = exchange.getHeaderAsUuid(HeaderKeys.SenderServiceUuid);
		UUID systemId = exchange.getHeaderAsUuid(HeaderKeys.SenderSystemUuid);

		try {
			ServiceDalI serviceDal = DalProvider.factoryServiceDal();
			Service service = serviceDal.getById(serviceId);
			for (ServiceInterfaceEndpoint serviceInterface : service.getEndpointsList()) {
				if (serviceInterface.getSystemUuid().equals(systemId)) {

					String publisherStatus = serviceInterface.getEndpoint();
					if (publisherStatus != null
							&& publisherStatus.equals(ServiceInterfaceEndpoint.STATUS_BULK_PROCESSING)) {
						return true;
					}
				}
			}
			return false;

		} catch (Exception ex) {
			throw new PipelineException("Failed to detect bulk mode for exchange " + exchange.getId(), ex);
		}

	}

	private void closeChannel(Channel channel) {
		if (channel != null)
			try {
				channel.close();
			} catch (IOException e) {
				LOG.warn("Couldn't close Rabbit channel : ", e);
			} catch (TimeoutException e) {
				LOG.error("", e);
			}
	}

	private void waitForConfirmations(Channel channel) throws PipelineException {
		try {
			if (!channel.waitForConfirms()) {
				throw new PipelineException("Messages posted but not confirmed");
			}
		} catch (InterruptedException e) {
			LOG.error("", e);
		}
	}

	private void publishMessage(String routingKey, UUID messageUuid, Channel channel, Map<String, String> headers, boolean wasLastMessage, boolean isLastMessage) throws PipelineException {

		//set or remove the "last message" flag accordingly:
		//if this IS the last message and we're configured to add the flag, do so
		//if this WAS the last message, then persist the flag
		if ((isLastMessage && isAddLastMessageFlag()) || wasLastMessage) {
			headers.put(HeaderKeys.LastMessageForExchange, Boolean.TRUE.toString());
		}

		//we need a map of String to Object, so copy and change
		Map<String, Object> headerCopy = new HashMap<>();
		for (String headerKey: headers.keySet()) {
			String headerVal = headers.get(headerKey);
			headerCopy.put(headerKey, headerVal);
		}

		//build the RabbitMQ property object from our headers
		AMQP.BasicProperties properties = new AMQP.BasicProperties()
				.builder()
				.deliveryMode(2) //Persistent message
				.headers(headerCopy)
				.build();

		//and post to RabbitMQ
		try {
			channel.basicPublish(
					config.getExchange(),
					routingKey,
					properties,
					messageUuid.toString().getBytes());

		} catch (IOException e) {
			LOG.error("Unable to publish message", e);
			throw new PipelineException("Unable to publish message: " + e.getMessage(), e);
		}
	}

	private Channel getChannel(Connection connection) throws PipelineException {
		Channel channel;
		try {
			channel = ConnectionManager.getPublishChannel(connection, config.getExchange());
			channel.confirmSelect();
		} catch (IOException e) {
			throw new PipelineException("Unable to get Rabbit channel for exchange " + config.getExchange(), e);
		}
		return channel;
	}

	/**
	 * re-written to get Rabbit credentials from global Rabbit config
     */
	private Connection getConnection() throws PipelineException {

		try {
			String nodes = RabbitConfig.getInstance().getNodes();
			String username = RabbitConfig.getInstance().getUsername();
			String password = RabbitConfig.getInstance().getPassword();
			String sslProtocol = RabbitConfig.getInstance().getSslProtocol();

			return ConnectionManager.getConnection(username, password, nodes, sslProtocol);

		} catch (IOException e) {
			LOG.error("Unable to connect to rabbit", e);
			throw new PipelineException("Unable to connect to Rabbit : " + e.getMessage(), e);
		} catch (TimeoutException e) {
			LOG.error("Connection to Rabbit timed out", e);
			throw new PipelineException("Connection to rabbit timed out : " + e.getMessage(), e);
		}
	}

	public static String getRoutingKey(boolean bulkMode, Map<String, String> headers, PostMessageToExchangeConfig config) throws PipelineException {

		List<String> routingValues = new ArrayList<>();

		//if the service/system has been set into bulk mode, then factor this into the routing key as the first element
		if (bulkMode) {
			routingValues.add("BULK");
		}

		//get the value we're routing on, which will be one or more headers from the exchange
		List<String> routingHeaders = config.getRoutingHeader();
		for (String routingHeader: routingHeaders) {
			String routingValue = headers.get(routingHeader);
			if (Strings.isNullOrEmpty(routingValue)) {
				throw new PipelineException("Failed to find routing value for " + routingHeader);
			}
			routingValues.add(routingValue);
		}

		String fullRoutingValue = String.join(":", routingValues);
		String exchangeName = config.getExchange();
		return RoutingManager.getInstance().getRoutingKeyForIdentifier(exchangeName, fullRoutingValue);
	}

	static class Throttle {

		private int perSecondThrottle;
		private long startMs;
		private int doneThisSecond;
		private int doneTotal;

		private Throttle(int perSecondThrottle) {
			this.perSecondThrottle = perSecondThrottle;
			this.startMs = System.currentTimeMillis();
			this.doneThisSecond = 0;
			this.doneTotal = 0;
		}

		public void applyBreaks() {

			//if we don't want to apply any throttling, this will be set to -1
			if (perSecondThrottle == -1) {
				return;
			}

			doneThisSecond++;
			doneTotal++;

			if (doneThisSecond > perSecondThrottle) {
				long now = System.currentTimeMillis();
				long sleep = 1000 - (now - startMs);

				if (sleep > 0) {
					try {
						Thread.sleep(sleep);
					} catch (Exception ex) {
						LOG.error("", ex);
					}
				}

				startMs = System.currentTimeMillis();
				doneThisSecond = 0;
			}

			if (doneTotal % perSecondThrottle == 0) {
				LOG.info("Done " + doneTotal);
			}
		}

		public static Throttle factory(int numItems, UUID exchangeId) {
			int perSecondThrottle = TransformConfig.instance().getRabbitMessagePerSecondThrottle();
			boolean applyThrottle = numItems > perSecondThrottle;
			if (applyThrottle) {
				LOG.info("Got " + numItems + " message to post for exchange " + exchangeId + ", but will throttle to " + perSecondThrottle + " /sec");
				return new Throttle(perSecondThrottle);

			} else {
				//if not throttling, still return an instance, but set to -1 so we know to not actually apply any breaks
				return new Throttle(-1);
			}
		}

	}
}
