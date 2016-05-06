package org.endeavourhealth.messagingapi.pipeline;

import org.endeavourhealth.messaging.exchange.Exchange;
import org.endeavourhealth.messaging.pipeline.MessagePipeline;
import org.endeavourhealth.messaging.pipeline.components.*;

public class PostAsyncPipeline implements MessagePipeline {
	@Override
	public void process(Exchange exchange) {
		// NOTE: All components implement the same interface (PipelineProcess).
		// Pipeline could therefore be implemented via config/reflection in a loop
		try {
			new ValidateSender().process(exchange);
			new ValidateMessageType().process(exchange);
			new PostMessageToLog().process(exchange);
			new PostMessageToInboundQueue().process(exchange);
			new ReturnResponseAcknowledgement().process(exchange);
		} catch (Exception e) {
			new LogErrors().process(exchange);
		}
	}
}
