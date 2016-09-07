package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.ValidateSenderConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidateSender extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(ValidateSender.class);

	private ValidateSenderConfig config;

	public ValidateSender(ValidateSenderConfig config) {
		this.config = config;
	}
	@Override
	public void process(Exchange exchange) throws PipelineException {
		String protocols = exchange.getHeader(HeaderKeys.Protocols);

		if (protocols == null || protocols.isEmpty()) {
			LOG.debug("Sender invalid");
			throw new PipelineException("No valid protocols found for source/publisher");
		} else {
			LOG.debug("Sender valid");
		}
	}
}
