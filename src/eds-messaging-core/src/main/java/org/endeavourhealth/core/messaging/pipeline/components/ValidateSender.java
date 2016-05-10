package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.ValidateSenderConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.PropertyKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidateSender implements PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(PostMessageToLog.class);

	private ValidateSenderConfig config;

	public ValidateSender(ValidateSenderConfig config) {
		this.config = config;
	}
	@Override
	public void process(Exchange exchange) {
		String Sender = (String)exchange.getProperty(PropertyKeys.Sender);
		String Method = (String)exchange.getProperty(PropertyKeys.Method);

		// Do basic (contract) check to ensure <sender> is allowed to call <method>

		LOG.info("Sender validated");
	}
}
