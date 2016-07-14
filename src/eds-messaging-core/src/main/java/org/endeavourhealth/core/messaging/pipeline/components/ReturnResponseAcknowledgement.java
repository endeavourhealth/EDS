package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.ReturnResponseAcknowledgementConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.hl7.fhir.instance.formats.IParser;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.MessageHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.UUID;

public class ReturnResponseAcknowledgement extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(ReturnResponseAcknowledgement.class);

	private ReturnResponseAcknowledgementConfig config;

	public ReturnResponseAcknowledgement(ReturnResponseAcknowledgementConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {
		// NOTE: Doesnt actually return response here, just sets response body to be returned by AbstractEndpoint process()

		MessageHeader messageHeader = buildMessageHeader(exchange);

		try {
			String contentType = exchange.getHeader(HeaderKeys.ContentType);

			IParser parser = getParser(contentType);

			String message = parser.composeString(messageHeader);
			exchange.setBody(message);
		} catch (Exception e) {
			throw new PipelineException("Unable to serialize message header");
		}
		LOG.debug("Business acknowledgement sent");
	}

	private MessageHeader buildMessageHeader(Exchange exchange) {
		Coding coding = new Coding();
		coding.setDisplay("Business acknowledgement");

		MessageHeader.MessageHeaderResponseComponent response = new MessageHeader.MessageHeaderResponseComponent();
		response.setIdentifier(exchange.getHeader(HeaderKeys.MessageId));

		MessageHeader.MessageSourceComponent source = new MessageHeader.MessageSourceComponent();
		source.setName(exchange.getHeader(HeaderKeys.SenderLocalIdentifier));
		source.setEndpoint(exchange.getHeader(HeaderKeys.ResponseUri));
		source.setSoftware(exchange.getHeader(HeaderKeys.SourceSystem));

		MessageHeader messageHeader = new MessageHeader();
		messageHeader.setId(UUID.randomUUID().toString());
		messageHeader.setTimestamp(new Date());
		messageHeader.setEvent(coding);
		messageHeader.setResponse(response);
		messageHeader.setSource(source);

		return messageHeader;
	}
}
