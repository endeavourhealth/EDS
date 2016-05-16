package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.EnvelopMessageConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvelopMessage implements PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(PostMessageToLog.class);

	private EnvelopMessageConfig config;

	public EnvelopMessage(EnvelopMessageConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		// Set envelope properties from exchange properties

		// Set envelope body from exchange

		// Set exchange body to envelope

		LOG.info("Message wrapped in envelope");
	}
}
