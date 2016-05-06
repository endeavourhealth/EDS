package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.ReturnResponseAcknowledgementConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;

public class ReturnResponseAcknowledgement implements PipelineComponent {
	private ReturnResponseAcknowledgementConfig config;

	public ReturnResponseAcknowledgement(ReturnResponseAcknowledgementConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		exchange.body = exchange.body + "Acknowledgment sent" + System.lineSeparator();
	}
}
