package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.OpenEnvelopeConfig;
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
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class OpenEnvelope implements PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(OpenEnvelope.class);

	private OpenEnvelopeConfig config;

	public OpenEnvelope(OpenEnvelopeConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {
		// Extract envelope properties to exchange properties
		String body = exchange.getBody();

		String format = (String)exchange.getProperty(PropertyKeys.Format);
		IParser parser;
		if (format != null && "text/xml".equals(format))
			parser = new XmlParser();
		else
			parser = new JsonParser();

		try {
			Bundle bundle = (Bundle)parser.parse(body);
			List<Bundle.BundleEntryComponent> components = bundle.getEntry();

			// find header and payload in bundle
			MessageHeader messageHeader = null;
			Binary binary = null;
			for (Bundle.BundleEntryComponent component : components) {
				if (component.hasResource()) {
					Resource resource = component.getResource();
					if (resource instanceof MessageHeader)
						messageHeader = (MessageHeader) resource;
					if (resource instanceof Binary)
						binary = (Binary) resource;
				}
			}

			if (messageHeader == null || binary == null) {
				throw new PipelineException("Invalid bundle.  Must contain both a MessageHeader and a Binary resource");
			}

			processHeader(exchange, messageHeader);
			processBody(exchange, binary);

		} catch (Exception e) {
			throw new PipelineException(e.getMessage());
		}

		LOG.debug("Message envelope processed");
	}

	private void processHeader(Exchange exchange, MessageHeader messageHeader) {
		exchange.setProperty(PropertyKeys.MessageId, messageHeader.getId());
		exchange.setProperty(PropertyKeys.Sender, messageHeader.getSource().getName());
		exchange.setProperty(PropertyKeys.ResponseUri, messageHeader.getSource().getEndpoint());
		exchange.setProperty(PropertyKeys.SourceSystem, messageHeader.getSource().getSoftware());

		processDestinations(exchange, messageHeader);
	}

	private void processBody(Exchange exchange, Binary binary) {
		if (binary.hasContent()) {
			exchange.setBody(new String(binary.getContent()));
		}
	}

	private void processDestinations(Exchange exchange, MessageHeader messageHeader) {
		List<String> destinationNameList = new ArrayList<>();
		List<String> destinationUriList = new ArrayList<>();

		if (messageHeader.hasDestination()) {
			List<MessageHeader.MessageDestinationComponent> messageDestinationComponents = messageHeader.getDestination();

			for (MessageHeader.MessageDestinationComponent messageDestinationComponent : messageDestinationComponents) {
				destinationNameList.add(messageDestinationComponent.getName());
				destinationUriList.add(messageDestinationComponent.getEndpoint());
			}

			// Routing key based on destination
			exchange.setProperty(PropertyKeys.RoutingKey, String.join(",",destinationNameList));
		} else {
			// If no destination then use source
			exchange.setProperty(PropertyKeys.RoutingKey, messageHeader.getSource().getName());
		}

		exchange.setProperty(PropertyKeys.DestinationAddress, destinationUriList);
	}
}
