package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.ValidateMessageTypeConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidateMessageType extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(ValidateMessageType.class);

	private ValidateMessageTypeConfig config;

	public ValidateMessageType(ValidateMessageTypeConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) {
		String sender = exchange.getHeader(HeaderKeys.Sender);
		String contentType = exchange.getHeader(HeaderKeys.ContentType);
		String sourceSystem = exchange.getHeader(HeaderKeys.SourceSystem);
		String messageEvent = exchange.getHeader(HeaderKeys.MessageEvent);
		String body = exchange.getBody();

		// Determine type of message

		// Ensure type is valid for sender

		LOG.debug("Message validated");
	}
}
