package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.type.TypeReference;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.cache.ParserPool;
import org.endeavourhealth.core.audit.AuditWriter;
import org.endeavourhealth.core.configuration.OpenEnvelopeConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryDalI;
import org.endeavourhealth.core.database.dal.admin.OrganisationDalI;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.ActiveItem;
import org.endeavourhealth.core.database.dal.admin.models.Item;
import org.endeavourhealth.core.database.dal.admin.models.Organisation;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.fhirStorage.JsonServiceInterfaceEndpoint;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.core.xml.QueryDocument.System;
import org.endeavourhealth.core.xml.QueryDocument.TechnicalInterface;
import org.endeavourhealth.core.xml.QueryDocumentSerializer;
import org.hl7.fhir.instance.model.Binary;
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.MessageHeader;
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
		OrganisationDalI organisationRepository = DalProvider.factoryOrganisationDal();
		Organisation organisation = null;
		try {
			organisation = organisationRepository.getByNationalId(organisationOds);
		} catch (Exception ex) {
			throw new PipelineException("Failed to retrieve organisation for " + organisationOds, ex);
		}

		if (organisation == null) {
			throw new PipelineException("Organisation for national ID " + organisationOds + " could not be found");
		}

		//get the service
		Service service = null;
		//TODO - fix assumption that orgs can only have one service
		ServiceDalI serviceRepository = DalProvider.factoryServiceDal();
		for (UUID serviceId: organisation.getServices().keySet()) {
			try {
				service = serviceRepository.getById(serviceId);
			} catch (Exception ex) {
				throw new PipelineException("Failed to retrieve service " + serviceId);
			}
		}

		if (service == null) {
			throw new PipelineException("No service found for organisation " + organisation.getId() + " opening exchange " + exchange.getId());
		}

		String software = exchange.getHeader(HeaderKeys.SourceSystem);
		String version = exchange.getHeader(HeaderKeys.SystemVersion);
		UUID systemUuid = findSystemId(service, software, version);

		if (systemUuid == null) {
			throw new PipelineException("No system found for service " + service.getId() + " software " + software + " version " + version + " opening exchange " + exchange.getId());
		}

		exchange.setHeader(HeaderKeys.SenderServiceUuid, service.getId().toString());
		exchange.setHeader(HeaderKeys.SenderOrganisationUuid, organisation.getId().toString());
		exchange.setHeader(HeaderKeys.SenderSystemUuid, systemUuid.toString());

		//set this on the exchange to forice it to write to the exchange_by_service table in Cassandra
		exchange.setServiceId(service.getId());
		exchange.setSystemId(systemUuid);
	}


	private UUID findSystemId(Service service, String software, String messageVersion) throws PipelineException {

		List<JsonServiceInterfaceEndpoint> endpoints = null;
		try {
			endpoints = ObjectMapperPool.getInstance().readValue(service.getEndpoints(), new TypeReference<List<JsonServiceInterfaceEndpoint>>() {});

			for (JsonServiceInterfaceEndpoint endpoint: endpoints) {

				UUID endpointSystemId = endpoint.getSystemUuid();
				String endpointInterfaceId = endpoint.getTechnicalInterfaceUuid().toString();

				LibraryDalI libraryRepository = DalProvider.factoryLibraryDal();
				ActiveItem activeItem = libraryRepository.getActiveItemByItemId(endpointSystemId);
				Item item = libraryRepository.getItemByKey(endpointSystemId, activeItem.getAuditId());
				LibraryItem libraryItem = QueryDocumentSerializer.readLibraryItemFromXml(item.getXmlContent());
				System system = libraryItem.getSystem();
				for (TechnicalInterface technicalInterface: system.getTechnicalInterface()) {

					if (endpointInterfaceId.equals(technicalInterface.getUuid())
							&& technicalInterface.getMessageFormat().equalsIgnoreCase(software)
							&& technicalInterface.getMessageFormatVersion().equalsIgnoreCase(messageVersion)) {

						return endpointSystemId;
					}
				}
			}
		} catch (Exception e) {
			throw new PipelineException("Failed to process endpoints from service " + service.getId());
		}

		return null;
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
