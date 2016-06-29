package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.PostToEventStoreConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostToEventStore implements PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(PostToEventStore.class);

	private PostToEventStoreConfig config;

	public PostToEventStore(PostToEventStoreConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		LOG.debug("Message posted to event log");
	}
}
