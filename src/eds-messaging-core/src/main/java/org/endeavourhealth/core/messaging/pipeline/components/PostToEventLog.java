package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.PostToEventLogConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostToEventLog implements PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(PostToEventLog.class);

	private PostToEventLogConfig config;

	public PostToEventLog(PostToEventLogConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		LOG.debug("Message posted to event log");
	}
}
