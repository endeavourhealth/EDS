package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.configuration.OpenEnvelopeConfig;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.hl7.fhir.instance.formats.IParser;
import org.hl7.fhir.instance.model.Binary;
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.MessageHeader;
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class OpenEnvelope extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(OpenEnvelope.class);

	private OpenEnvelopeConfig config;

	public OpenEnvelope(OpenEnvelopeConfig config) {
		this.config = config;
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {
		// Extract envelope properties to exchange properties
		String body = exchange.getBody();

		String contentType = exchange.getHeader(HeaderKeys.ContentType);
		IParser parser = getParser(contentType);

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

	private void processHeader(Exchange exchange, MessageHeader messageHeader) throws PipelineException {

		exchange.setHeader(HeaderKeys.MessageId, messageHeader.getId());

		exchange.setHeader(HeaderKeys.SenderLocalIdentifier, messageHeader.getSource().getName());
		exchange.setHeader(HeaderKeys.SourceSystem, messageHeader.getSource().getSoftware());
		exchange.setHeader(HeaderKeys.SystemVersion, messageHeader.getSource().getVersion());

		exchange.setHeader(HeaderKeys.ResponseUri, messageHeader.getSource().getEndpoint());
		exchange.setHeader(HeaderKeys.MessageEvent, messageHeader.getEvent().getCode());

		getSenderUuid(exchange);
		processDestinations(exchange, messageHeader);
	}

	private void getSenderUuid(Exchange exchange) throws PipelineException {
		// Get service id
		String senderId = exchange.getHeader(HeaderKeys.SenderLocalIdentifier);

		// Get service id from sender id
		ServiceRepository serviceRepository = new ServiceRepository();
		Iterable<Service> services = serviceRepository.getByLocalIdentifier(senderId);

		if (!services.iterator().hasNext())
			throw new PipelineException("No service found for local identifier");

		Service service = services.iterator().next();
		exchange.setHeader(HeaderKeys.SenderUuid, service.getId().toString());
	}

	private void processBody(Exchange exchange, Binary binary) {
		exchange.setHeader(HeaderKeys.MessageFormat, binary.getContentType());
		if (binary.hasContent()) {
			exchange.setBody(new String(binary.getContent()));
		}
	}

	private void processDestinations(Exchange exchange, MessageHeader messageHeader) {
		List<String> destinationUriList = new ArrayList<>();

		if (messageHeader.hasDestination()) {
			List<MessageHeader.MessageDestinationComponent> messageDestinationComponents = messageHeader.getDestination();

			for (MessageHeader.MessageDestinationComponent messageDestinationComponent : messageDestinationComponents) {
				destinationUriList.add(messageDestinationComponent.getEndpoint());
			}
		}

		exchange.setHeader(HeaderKeys.DestinationAddress, String.join(",", destinationUriList));
	}
}
