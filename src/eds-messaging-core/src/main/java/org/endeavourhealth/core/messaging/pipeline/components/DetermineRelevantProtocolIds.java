package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.DetermineRelevantProtocolIdsConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DetermineRelevantProtocolIds implements PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(DetermineRelevantProtocolIds.class);

	private DetermineRelevantProtocolIdsConfig config;

	public DetermineRelevantProtocolIds(DetermineRelevantProtocolIdsConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		// Determine relevant protocol ids
		exchange.setHeader(HeaderKeys.ProtocolId, "1,2,3,4");

		LOG.debug("Data distribution protocols Loaded");
	}
}
