package org.endeavourhealth.messagingapi.pipeline;

import org.endeavourhealth.core.messaging.exchange.Exchange;

public class GetAsyncPipeline {
	public void process(Exchange exchange) {
		// Validate Sender
		// Validate Message Type
		// Post message to log
		// Post to inbound queue
		// Return response acknowledgement
		// Log any errors
	}
}
