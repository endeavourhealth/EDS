package org.endeavourhealth.core.messaging.pipeline;

import org.endeavourhealth.core.messaging.exchange.Exchange;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface PipelineComponent {
	void process(Exchange exchange) throws PipelineException, IOException, TimeoutException;
}
