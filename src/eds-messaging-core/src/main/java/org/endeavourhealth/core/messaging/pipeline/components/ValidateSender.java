package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.ValidateSenderConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;

public class ValidateSender implements PipelineComponent {
	private ValidateSenderConfig config;

	public ValidateSender(ValidateSenderConfig config) {
		this.config = config;
	}
	@Override
	public void process(Exchange exchange) {
		exchange.setBody(exchange.getBody() + "Sender validated" + System.lineSeparator());
	}
}
