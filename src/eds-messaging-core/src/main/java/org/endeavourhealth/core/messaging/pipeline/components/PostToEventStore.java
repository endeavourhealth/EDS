package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.PostToEventStoreConfig;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostToEventStore extends PipelineComponent {
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
