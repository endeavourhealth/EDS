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

		try {
			Boolean canBeQueued = exchange.getHeaderAsBoolean(HeaderKeys.AllowQueueing);
			if (canBeQueued != null
					&& !canBeQueued.booleanValue()) {
				return false;
			}
		} catch (Exception ex) {
			throw new PipelineException("Error checking header keys", ex);
		}

		String routingKey = getRoutingKey(exchange, config);

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
		Map<String, Object> headers = new HashMap<>();
		for (String key : exchange.getHeaders().keySet()) {
			//skip the authorization header, since that's comparatively huge and there's no need to carry it through RabbbitMQ
			if (key.equalsIgnoreCase("authorization")) {
				continue;
			}
			headers.put(key, exchange.getHeader(key));
		}

		AMQP.BasicProperties properties = new AMQP.BasicProperties()
				.builder()
				.deliveryMode(2)    // Persistent message
				.headers(headers)
				.build();

		// Handle multicast
		String multicastHeader = config.getMulticastHeader();
		if (Strings.isNullOrEmpty(multicastHeader)) {

			//LOG.trace("Posting exchange {} to Rabbit exchange {} with routing key {}", messageUuid, config.getExchange(), routingKey);
			publishMessage(routingKey, messageUuid, channel, properties);

		} else {
			//LOG.trace("Posting exchange {} to Rabbit exchange {} with routing key {} and multicast header {}", messageUuid, config.getExchange(), routingKey, multicastHeader);
			String multicastData = exchange.getHeader(multicastHeader);

			//adding handler for when we're missing multicast header data
			if (Strings.isNullOrEmpty(multicastData)) {
				throw new PipelineException("No multicast data for " + multicastHeader + " to post exchange " + exchange.getId() + " to " + config.getExchange());
			}

			try {
				Object[] multicastItems = ObjectMapperPool.getInstance().readValue(multicastData, Object[].class);

				int perSecondThrottle = TransformConfig.instance().getRabbitMessagePerSecondThrottle();
				boolean applyThrottle = multicastItems.length > perSecondThrottle;
				if (applyThrottle) {
					LOG.info("Got " + multicastItems.length + " message to post for exchange " + exchange.getId() + ", but will throttle to " + perSecondThrottle + " /sec");
				}
				long startMs = System.currentTimeMillis();
				int doneThisSecond = 0;
				int doneTotal = 0;

				for (Object multicastItem : multicastItems) {
					String itemData = ObjectMapperPool.getInstance().writeValueAsString(multicastItem);
					// Replace header list with individual value
					headers.put(multicastHeader, itemData);
					properties = properties.builder().headers(headers).build();
					publishMessage(routingKey, messageUuid, channel, properties);

					if (applyThrottle) {
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
				}
			} catch (IOException e) {
				throw new PipelineException("Could not parse multicast data", e);
			}
		}

		waitForConfirmations(channel);
		closeChannel(channel);
		return true;
	}

	/**
	 * works out if the service and system have been set into "bulk" mode which is factored in to
	 * the routing, allowing us to route exchanges for services differently to how they otherwise would be
     */
	private static boolean isBulkMode(Exchange exchange) throws Exception {

		UUID serviceId = exchange.getHeaderAsUuid(HeaderKeys.SenderServiceUuid);
		UUID systemId = exchange.getHeaderAsUuid(HeaderKeys.SenderSystemUuid);

		ServiceDalI serviceDal = DalProvider.factoryServiceDal();
		Service service = serviceDal.getById(serviceId);
		for (ServiceInterfaceEndpoint serviceInterface: service.getEndpointsList()) {
			if (serviceInterface.getSystemUuid().equals(systemId)) {

				String publisherStatus = serviceInterface.getEndpoint();
				if (publisherStatus != null
						&& publisherStatus.equals(ServiceInterfaceEndpoint.STATUS_BULK_PROCESSING)) {
					return true;
				}
			}
		}

		return false;
	}

	private void closeChannel(Channel channel) {
		if (channel != null)
			try {
				channel.close();
			} catch (IOException e) {
				LOG.warn("Couldn't close Rabbit channel : ", e);
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
	}

	private void waitForConfirmations(Channel channel) throws PipelineException {
		try {
			if (!channel.waitForConfirms()) {
				throw new PipelineException("Messages posted but not confirmed");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void publishMessage(String routingKey, UUID messageUuid, Channel channel, AMQP.BasicProperties properties) throws PipelineException {
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

	public static String getRoutingKey(Exchange exchange, PostMessageToExchangeConfig config) throws PipelineException {

		List<String> routingValues = new ArrayList<>();

		//if the service/system has been set into bulk mode, then factor this into the routing key
		try {
			boolean bulkMode = isBulkMode(exchange);
			if (bulkMode) {
				routingValues.add("BULK");
			}
		} catch (Exception ex) {
			throw new PipelineException("Failed to determine if in bulk mode", ex);
		}

		//get the value we're routing on, which will be one or more headers from the exchange
		List<String> routingHeaders = config.getRoutingHeader();
		for (String routingHeader: routingHeaders) {
			String routingValue = exchange.getHeader(routingHeader);
			if (Strings.isNullOrEmpty(routingValue)) {
				throw new PipelineException("Failed to find routing value for " + routingHeader + " in exchange " + exchange);
			}
			routingValues.add(routingValue);
		}

		String fullRoutingValue = String.join(":", routingValues);
		String exchangeName = config.getExchange();
		return RoutingManager.getInstance().getRoutingKeyForIdentifier(exchangeName, fullRoutingValue);
	}

}
