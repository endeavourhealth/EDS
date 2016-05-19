package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.LoadSenderConfigurationConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadSenderConfiguration implements PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(LoadSenderConfiguration.class);

	private LoadSenderConfigurationConfig config;

	public LoadSenderConfiguration(LoadSenderConfigurationConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		LOG.debug("Sender specific configuration loaded");
	}
}
