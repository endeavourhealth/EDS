package org.endeavourhealth.queuereader.pipeline;

import org.endeavourhealth.messaging.exchange.Exchange;
import org.endeavourhealth.messaging.pipeline.MessagePipeline;
import org.endeavourhealth.messaging.pipeline.components.MessageTransform;

public class InboundPipeline implements MessagePipeline {
	public void process(Exchange exchange) {
		new MessageTransform().process(exchange);
		// Post to event log
		// Post to response queue
		// Post to interim queue
	}
}
