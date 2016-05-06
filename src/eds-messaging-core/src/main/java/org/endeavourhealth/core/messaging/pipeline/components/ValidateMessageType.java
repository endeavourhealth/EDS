package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.ValidateMessageTypeConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;

public class ValidateMessageType implements PipelineComponent {
	private ValidateMessageTypeConfig config;

	public ValidateMessageType(ValidateMessageTypeConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		exchange.body = exchange.body + "Message validated" + System.lineSeparator();
	}
}
