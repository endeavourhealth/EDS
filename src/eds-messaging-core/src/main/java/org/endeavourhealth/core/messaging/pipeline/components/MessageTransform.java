package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.MessageTransformConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;

public class MessageTransform implements PipelineComponent {
	private MessageTransformConfig config;
	public MessageTransform(MessageTransformConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		// Figure out the transformer
		// Call it
		// Replace message body in echange
	}
}
