package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.ValidateSenderConfig;
import org.endeavourhealth.core.messaging.EDSMethod;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.PropertyKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidateSender implements PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(ValidateSender.class);

	private ValidateSenderConfig config;

	public ValidateSender(ValidateSenderConfig config) {
		this.config = config;
	}
	@Override
	public void process(Exchange exchange) throws PipelineException {
		String sender = (String)exchange.getProperty(PropertyKeys.Sender);
		EDSMethod method = (EDSMethod) exchange.getProperty(PropertyKeys.Method);

		// Load data distribution protocols

		// Check sender is allowed to make the call to the method

		LOG.debug("Sender validated");
	}
}
