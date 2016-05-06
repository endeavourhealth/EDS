package org.endeavourhealth.messaging.pipeline;

import org.endeavourhealth.messaging.exchange.Exchange;

public interface MessagePipeline {
	void process(Exchange exchange);
}
