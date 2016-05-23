package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.ReturnResponseAcknowledgementConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.PropertyKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.hl7.fhir.instance.formats.IParser;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.XmlParser;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.MessageHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ReturnResponseAcknowledgement implements PipelineComponent {
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
			String format = (String)exchange.getProperty(PropertyKeys.Format);

			IParser parser = null;
			if (format != null && "text/xml".equals(format))
				parser = new XmlParser();
			else
				parser = new JsonParser();

			String bundleXml = parser.composeString(messageHeader);
			exchange.setBody(bundleXml);
		} catch (Exception e) {
			throw new PipelineException("Unable to serialize message header");
		}
		LOG.debug("Message posted to log");
	}

	private MessageHeader buildMessageHeader(Exchange exchange) {
		Coding coding = new Coding();
		coding.setDisplay("Business acknowledgement");

		MessageHeader.MessageHeaderResponseComponent response = new MessageHeader.MessageHeaderResponseComponent();
		response.setIdentifier((String)exchange.getProperty(PropertyKeys.MessageId));

		MessageHeader.MessageSourceComponent source = new MessageHeader.MessageSourceComponent();
		source.setName((String)exchange.getProperty(PropertyKeys.Sender));
		source.setEndpoint((String)exchange.getProperty(PropertyKeys.ResponseUri));
		source.setSoftware((String)exchange.getProperty(PropertyKeys.SourceSystem));

		MessageHeader messageHeader = new MessageHeader();
		messageHeader.setId(UUID.randomUUID().toString());
		messageHeader.setTimestamp(new Date());
		messageHeader.setEvent(coding);
		messageHeader.setResponse(response);
		messageHeader.setSource(source);

		return messageHeader;
	}
}
