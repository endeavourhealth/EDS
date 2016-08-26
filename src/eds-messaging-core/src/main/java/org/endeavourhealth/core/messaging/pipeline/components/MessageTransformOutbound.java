package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.MessageTransformOutboundConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MessageTransformOutbound extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(MessageTransformOutbound.class);


	private MessageTransformOutboundConfig config;

	public MessageTransformOutbound(MessageTransformOutboundConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		exchange.setBody("Transformed to destination output format(s)");
		LOG.trace("Message transformed (outbound)");
	}
}
