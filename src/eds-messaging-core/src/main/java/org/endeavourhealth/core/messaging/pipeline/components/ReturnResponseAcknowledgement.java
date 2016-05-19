package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.ReturnResponseAcknowledgementConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReturnResponseAcknowledgement implements PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(ReturnResponseAcknowledgement.class);

	private ReturnResponseAcknowledgementConfig config;

	public ReturnResponseAcknowledgement(ReturnResponseAcknowledgementConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		// NOTE: Doesnt actually return response here, just sets response body to be returned by AbstractEndpoint process()
		exchange.setBody("Insert confirmation message body here");
		LOG.debug("Message posted to log");
	}
}
