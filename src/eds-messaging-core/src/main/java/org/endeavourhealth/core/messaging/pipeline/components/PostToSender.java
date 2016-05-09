package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.PostToSenderConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;

public class PostToSender implements PipelineComponent {
	private PostToSenderConfig config;

	public PostToSender(PostToSenderConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		exchange.body = exchange.body + "Message posted to log" + System.lineSeparator();
	}
}
