package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;

public class ValidateSender implements PipelineComponent {
	private org.endeavourhealth.core.configuration.ValidateSender config;

	public ValidateSender(org.endeavourhealth.core.configuration.ValidateSender config) {
		this.config = config;
	}
	@Override
	public void process(Exchange exchange) {
		exchange.body = exchange.body + "Sender validated" + System.lineSeparator();
	}
}
