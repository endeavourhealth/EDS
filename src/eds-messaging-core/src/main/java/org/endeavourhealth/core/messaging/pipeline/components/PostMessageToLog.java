package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.PostMessageToLogConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;

public class PostMessageToLog implements PipelineComponent {
	private PostMessageToLogConfig config;

	public PostMessageToLog(PostMessageToLogConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		exchange.body = exchange.body + "Message posted to log" + System.lineSeparator();
	}
}
