package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.LoadDataDistributionProtocolsConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadDataDistributionProtocols implements PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(PostMessageToLog.class);

	private LoadDataDistributionProtocolsConfig config;

	public LoadDataDistributionProtocols(LoadDataDistributionProtocolsConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		LOG.info("Data distribution protocols loaded");
	}
}
