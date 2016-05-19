package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.EnvelopMessageConfig;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.PropertyKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.hl7.fhir.instance.formats.IParser;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.XmlParser;
import org.hl7.fhir.instance.model.Binary;
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.MessageHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class EnvelopMessage implements PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(EnvelopMessage.class);

	private EnvelopMessageConfig config;

	public EnvelopMessage(EnvelopMessageConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {
		MessageHeader messageHeader = buildMessageHeader(exchange);
		Binary binary = buildBinary(exchange);

		Bundle bundle = buildBundle(exchange, messageHeader, binary);

		try {
			String format = (String)exchange.getProperty(PropertyKeys.Format);

			IParser parser = null;
			if (format != null && "text/xml".equals(format))
				parser = new XmlParser();
			else
				parser = new JsonParser();

			String bundleXml = parser.composeString(bundle);
			exchange.setBody(bundleXml);
		} catch (Exception e) {
			throw new PipelineException("Unable to serialize message bundle");
		}


		LOG.debug("Message wrapped in envelope");
	}

	private MessageHeader buildMessageHeader(Exchange exchange) {
		MessageHeader messageHeader = new MessageHeader();

		MessageHeader.MessageSourceComponent source = new MessageHeader.MessageSourceComponent();

		source.setName((String)exchange.getProperty(PropertyKeys.Sender));
		source.setEndpoint((String)exchange.getProperty(PropertyKeys.ResponseUri));
		source.setSoftware((String)exchange.getProperty(PropertyKeys.SourceSystem));
		messageHeader.setSource(source);

		String addresses = (String)exchange.getProperty(PropertyKeys.DestinationAddress);
		List<String> addressList = Arrays.asList(addresses.split("\\s*,\\s*"));

		if (addresses != null) {
			for (String address : addressList) {
				MessageHeader.MessageDestinationComponent destination = new MessageHeader.MessageDestinationComponent();
				destination.setEndpoint(address);
				messageHeader.addDestination(destination);
			}
		}

		return messageHeader;
	}

	private Binary buildBinary(Exchange exchange) {
		Binary binary = new Binary();
		binary.setContent(exchange.getBody().getBytes());
		return binary;
	}

	private Bundle buildBundle(Exchange exchange, MessageHeader messageHeader, Binary binary) {
		Bundle bundle = new Bundle();
		bundle.setType(Bundle.BundleType.COLLECTION);

		Bundle.BundleEntryComponent messageHeaderComponent = new Bundle.BundleEntryComponent();
		messageHeaderComponent.setResource(messageHeader);
		bundle.addEntry(messageHeaderComponent);

		Bundle.BundleEntryComponent binaryComponent = new Bundle.BundleEntryComponent();
		binaryComponent.setResource(binary);
		bundle.addEntry(binaryComponent);

		return bundle;
	}
}
