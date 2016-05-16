package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.ValidateSenderConfig;
import org.endeavourhealth.core.contracts.Contracts;
import org.endeavourhealth.core.messaging.EDSMethod;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.PropertyKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidateSender implements PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(PostMessageToLog.class);

	private ValidateSenderConfig config;

	public ValidateSender(ValidateSenderConfig config) {
		this.config = config;
	}
	@Override
	public void process(Exchange exchange) throws PipelineException {
		String sender = (String)exchange.getProperty(PropertyKeys.Sender);
		EDSMethod method = (EDSMethod) exchange.getProperty(PropertyKeys.Method);

		if (Contracts.SenderHasPermissionForMethod(sender, method))
			LOG.info("Sender validated");
		else {
			LOG.error("Sender does not have permission to call this method");
			throw new PipelineException("No permission");
		}
	}
}
