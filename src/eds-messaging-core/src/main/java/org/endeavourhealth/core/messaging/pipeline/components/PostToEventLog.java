package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.PostToEventLogConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;

public class PostToEventLog implements PipelineComponent {
	private PostToEventLogConfig config;

	public PostToEventLog(PostToEventLogConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		exchange.setBody(exchange.getBody() + "Message posted to log" + System.lineSeparator());
	}
}
