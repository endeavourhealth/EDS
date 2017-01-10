package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.audit.AuditWriter;
import org.endeavourhealth.core.cache.ObjectMapperPool;
import org.endeavourhealth.core.configuration.PostToSubscriberWebServiceConfig;
import org.endeavourhealth.core.data.admin.QueuedMessageRepository;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.messaging.pipeline.SubscriberBatch;
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
			SubscriberBatch subscriberBatch = ObjectMapperPool.getInstance().readValue(exchange.getHeader(HeaderKeys.SubscriberBatch), SubscriberBatch.class);
			// Load transformed message from DB
			String outboundMessage = new QueuedMessageRepository().getById(subscriberBatch.getOutputMessageId()).getMessageBody();
			exchange.setBody(outboundMessage);
			// Set list of destinations
			exchange.setHeader(HeaderKeys.DestinationAddress, String.join(",", subscriberBatch.getEndpoints()));

			//commit what we've just received to the DB
			AuditWriter.writeExchange(exchange);

		} catch (IOException e) {
			LOG.error("Error deserializing subscriber batch JSON", e);
			throw new PipelineException("Error deserializing subscriber batch JSON", e);
		}
		LOG.trace("Message subscribers identified");
	}
}
