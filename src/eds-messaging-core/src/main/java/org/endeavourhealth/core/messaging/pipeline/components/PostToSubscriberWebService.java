package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.cache.ObjectMapperPool;
import org.endeavourhealth.core.configuration.PostToSubscriberWebServiceConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.messaging.pipeline.SubscriberBatch;
import org.endeavourhealth.core.messaging.pipeline.TransformBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PostToSubscriberWebService extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(PostToSubscriberWebService.class);

	private PostToSubscriberWebServiceConfig config;

	public PostToSubscriberWebService(PostToSubscriberWebServiceConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {
		try {
			TransformBatch transformBatch = ObjectMapperPool.getInstance().readValue(exchange.getHeader(HeaderKeys.TransformBatch), TransformBatch.class);
			SubscriberBatch subscriberBatch = ObjectMapperPool.getInstance().readValue(exchange.getHeader(HeaderKeys.SubscriberBatch), SubscriberBatch.class);
			// Load transformed message from DB
			exchange.setBody(
					String.format(
							"TRANSFORMED OUTPUT\nBatchId : %s\nProtocolId : %s\nFormat : %s\nVersion : %s\nEndpoints : %s",
							transformBatch.getBatchId().toString(),
							transformBatch.getProtocolId().toString(),
							subscriberBatch.getTechnicalInterface().getMessageFormat(),
							subscriberBatch.getTechnicalInterface().getMessageFormatVersion(),
							String.join(",", subscriberBatch.getEndpoints())
					)
			);
			// Set list of destinations
			exchange.setHeader(HeaderKeys.DestinationAddress, String.join(",", subscriberBatch.getEndpoints()));
		} catch (IOException e) {
			LOG.error("Error deserializing subscriber batch JSON");
			throw new PipelineException("Error deserializing subscriber batch JSON", e);
		}
		LOG.trace("Message subscribers identified");
	}
}
