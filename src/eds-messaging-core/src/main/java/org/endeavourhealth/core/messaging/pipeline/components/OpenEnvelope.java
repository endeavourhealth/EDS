package org.endeavourhealth.core.messaging.pipeline.components;

import org.endeavourhealth.core.audit.AuditWriter;
import org.endeavourhealth.core.cache.ParserPool;
import org.endeavourhealth.core.configuration.OpenEnvelopeConfig;
import org.endeavourhealth.core.data.admin.OrganisationRepository;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.Organisation;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.data.audit.AuditRepository;
import org.endeavourhealth.core.data.audit.models.ExchangeByService;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.hl7.fhir.instance.model.Binary;
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.MessageHeader;
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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

		try {
			Bundle bundle = (Bundle)new ParserPool().parse(contentType, body);
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

			//commit what we've just received to the DB
			AuditWriter.writeExchange(exchange);

		} catch (Exception e) {
			throw new PipelineException(e.getMessage(), e);
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

		String organisationOds = exchange.getHeader(HeaderKeys.SenderLocalIdentifier);

		//get the organisation
		OrganisationRepository organisationRepository = new OrganisationRepository();
		Organisation organisation = organisationRepository.getByNationalId(organisationOds);
		if (organisation == null) {
			throw new PipelineException("Organisation for national ID " + organisationOds + " could not be found");
		}

		//get the service
		Service service = null;
		//TODO - fix assumption that orgs can only have one service
		ServiceRepository serviceRepository = new ServiceRepository();
		for (UUID serviceId: organisation.getServices().keySet()) {
			service = serviceRepository.getById(serviceId);
		}

		if (service == null) {
			throw new PipelineException("No service found for organisation " + organisation.getId());
		}

		//record the service-exchange linkage, so we can retrieve exchanges by service
		ExchangeByService exchangeByService = new ExchangeByService();
		exchangeByService.setExchangeId(exchange.getExchangeId());
		exchangeByService.setServiceId(service.getId());
		exchangeByService.setTimestamp(new Date());
		new AuditRepository().save(exchangeByService);

		exchange.setHeader(HeaderKeys.SenderServiceUuid, service.getId().toString());
		exchange.setHeader(HeaderKeys.SenderOrganisationUuid, organisation.getId().toString());

		//commit what we've just added to the DB
		AuditWriter.writeExchange(exchange);
	}
	/*private void getSenderUuid(Exchange exchange) throws PipelineException {

		// Get service id
		String senderId = exchange.getHeader(HeaderKeys.SenderLocalIdentifier);

		//the sender ID is composed of the service ID and the organisation ODS code
		String[] tokens = senderId.split("//");
		String serviceLocalIdentifier = tokens[0];
		String organisationOds = tokens[1];

		// Get service id from sender id
		ServiceRepository serviceRepository = new ServiceRepository();
		Service service = serviceRepository.getByLocalIdentifier(serviceLocalIdentifier);

		if (service == null) {
			throw new PipelineException("No service found for local identifier " + senderId);
		}

		//the service retrieved by local identifier only contains the ID, so we need to re-retrieve using
		//that ID to get the name, orgs etc.
		service = serviceRepository.getById(service.getId());

		//validate that the organisation is a member of the service
		Set<UUID> orgIds = service.getOrganisations().keySet();
		Set<Organisation> orgs = new OrganisationRepository().getByIds(orgIds);
		Organisation organisation = orgs
				.stream()
				.filter(t -> t.getNationalId().equalsIgnoreCase(organisationOds))
				.collect(StreamExtension.firstOrNullCollector());

		if (organisation == null) {
			throw new PipelineException("Organisation " + organisationOds + " is not a member of service " + service.getName());
		}

		exchange.setHeader(HeaderKeys.SenderServiceUuid, service.getId().toString());
		exchange.setHeader(HeaderKeys.SenderOrganisationUuid, organisation.getId().toString());

	}*/

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
