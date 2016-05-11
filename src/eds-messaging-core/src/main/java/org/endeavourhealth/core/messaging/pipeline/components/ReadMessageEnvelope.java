package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.ReadMessageEnvelopeConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadMessageEnvelope implements PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(PostMessageToLog.class);

	private ReadMessageEnvelopeConfig config;

	public ReadMessageEnvelope(ReadMessageEnvelopeConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		LOG.info("Message envelope processed");
	}
}
