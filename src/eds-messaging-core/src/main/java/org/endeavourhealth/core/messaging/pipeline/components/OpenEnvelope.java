package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.OpenEnvelopeConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenEnvelope implements PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(PostMessageToLog.class);

	private OpenEnvelopeConfig config;

	public OpenEnvelope(OpenEnvelopeConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		// Extract envelope properties to exchange properties

		// Set exchange body to body of envelope

		LOG.info("Message envelope processed");
	}
}
