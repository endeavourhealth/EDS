package org.endeavourhealth.messagingapi.pipeline;

import org.endeavourhealth.messaging.exchange.Exchange;
import org.endeavourhealth.messaging.pipeline.MessagePipeline;

public class PostSyncPipeline implements MessagePipeline {
	@Override
	public void process(Exchange exchange) {
		// Validate Sender
		// Validate Message Type
		// Post message to log
		// Post to inbound queue
		// Return response acknowledgement
		// Log any errors
	}
}
