package org.endeavourhealth.core.messaging.pipeline;

import org.endeavourhealth.core.messaging.exchange.Exchange;

public interface PipelineComponent {
	void process(Exchange exchange) throws Exception;
}
