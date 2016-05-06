package org.endeavourhealth.messaging.pipeline;

import org.endeavourhealth.messaging.exchange.Exchange;

public interface PipelineProcess {
	void process(Exchange exchange);
}
