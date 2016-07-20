package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.MessageTransformConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageTransform extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(MessageTransform.class);

	private MessageTransformConfig config;

	public MessageTransform(MessageTransformConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		LOG.debug("Message transformed");
	}
}
