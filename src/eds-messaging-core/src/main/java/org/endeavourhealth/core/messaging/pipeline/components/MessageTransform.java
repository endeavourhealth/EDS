package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.MessageTransformConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageTransform implements PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(MessageTransform.class);

	private MessageTransformConfig config;

	public MessageTransform(MessageTransformConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		// Determine which subscribers want this format
		exchange.setHeader(HeaderKeys.SubscriberList, "http://192.168.100.1, http://192.168.100.2, http://192.168.100.3");
		LOG.debug("Message transformed");
	}
}
