package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.RunDataDistributionProtocolsConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;

public class RunDataDistributionProtocols implements PipelineComponent {
	private RunDataDistributionProtocolsConfig config;

	public RunDataDistributionProtocols(RunDataDistributionProtocolsConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		exchange.body = exchange.body + "Message posted to log" + System.lineSeparator();
	}
}
