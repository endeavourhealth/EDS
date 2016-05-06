package org.endeavourhealth.queuereader.pipeline;

import org.endeavourhealth.messaging.exchange.Exchange;
import org.endeavourhealth.messaging.pipeline.MessagePipeline;

public class InterimPipeline implements MessagePipeline {
	public void process(Exchange exchange) {
		// Run data distribution protocol
		// Message transform
		// Post message to log
		// Post to subscriber queue
		// Post bad message to dead letter
	}
}
