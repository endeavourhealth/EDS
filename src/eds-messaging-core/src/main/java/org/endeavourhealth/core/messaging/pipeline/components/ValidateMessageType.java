package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;

public class ValidateMessageType implements PipelineComponent {
	private org.endeavourhealth.core.configuration.ValidateMessageType config;

	public ValidateMessageType(org.endeavourhealth.core.configuration.ValidateMessageType config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		exchange.body = exchange.body + "Message validated" + System.lineSeparator();
	}
}
