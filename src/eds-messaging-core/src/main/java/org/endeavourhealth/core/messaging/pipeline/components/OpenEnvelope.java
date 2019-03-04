package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.cache.ParserPool;
import org.endeavourhealth.core.configuration.OpenEnvelopeConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryDalI;
import org.endeavourhealth.core.database.dal.admin.OrganisationDalI;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.ActiveItem;
import org.endeavourhealth.core.database.dal.admin.models.Item;
import org.endeavourhealth.core.database.dal.admin.models.Organisation;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.audit.models.LastDataReceived;
import org.endeavourhealth.core.fhirStorage.JsonServiceInterfaceEndpoint;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.core.xml.QueryDocument.System;
import org.endeavourhealth.core.xml.QueryDocument.TechnicalInterface;
import org.endeavourhealth.core.xml.QueryDocumentSerializer;
import org.endeavourhealth.transform.common.AuditWriter;
import org.endeavourhealth.transform.common.MessageFormat;
import org.hl7.fhir.instance.model.Binary;
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.MessageHeader;
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class OpenEnvelope extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(OpenEnvelope.class);

	public static final String DATA_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

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
			calculateLastDate(exchange); //work out when the data was from

			//commit what we've just received to the DB
			AuditWriter.writeExchange(exchange);

		} catch (Exception e) {
			throw new PipelineException(e.getMessage(), e);
		}

		LOG.debug("Message envelope processed");
	}

	private void calculateLastDate(Exchange exchange) throws PipelineException {

		UUID serviceId = exchange.getServiceId();
		UUID systemId = exchange.getSystemId();
		if (serviceId == null
				|| systemId == null) {
			return;
		}

		String body = exchange.getBody();
		if (Strings.isNullOrEmpty(body)) {
			return;
		}

		try {
			String software = exchange.getHeader(HeaderKeys.SourceSystem);
			String version = exchange.getHeader(HeaderKeys.SystemVersion);
			Date lastDataDate = calculateLastDataDate(software, version, body);

			//sometimes we can't work out a date
			if (lastDataDate == null) {
				return;
			}

			//set the date in the exchange header
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATA_DATE_FORMAT);
			exchange.setHeader(HeaderKeys.DataDate, simpleDateFormat.format(lastDataDate));

			//and save the date to the special table so we can retrieve it quicker
			LastDataReceived obj = new LastDataReceived();
			obj.setServiceId(serviceId);
			obj.setSystemId(systemId);
			obj.setExchangeId(exchange.getId());
			obj.setReceivedDate(new Date());
			obj.setDataDate(lastDataDate);

			ExchangeDalI exchangeDal = DalProvider.factoryExchangeDal();
			exchangeDal.save(obj);

		} catch (Throwable t) {
			//any exception, just log it out without throwing further up
			LOG.error("Failed to work out last extract date for " + exchange.getId(), t);
		}
	}

	/**
	 * works out the date of the newly published data, which is calculated from the exchange body
     */
	public static Date calculateLastDataDate(String software, String version, String body) throws Exception {

		//unlike all the others, the HL7 exchanges contain a FHIR resourse in JSON, with a timestamp in its body
		if (software.equalsIgnoreCase(MessageFormat.HL7V2)) {

			String timestampStr = findFirstElement(body, "timestamp");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			return sdf.parse(timestampStr);
		}

		//all other systems have the body containing a list of files in JSON, which contain
		//the date in the path somewhere
		String dateFormat = null;

		if (software.equalsIgnoreCase(MessageFormat.EMIS_CSV)) {

			//the custom extracts are one-off and receipt of them shouldn't interfere with the
			//normal logging of when data is received, so return null for them
			if (version.equalsIgnoreCase("CUSTOM")) {
				return null;
			}

			dateFormat = "yyyy-MM-dd'T'HH.mm.ss";

		} else if (software.equalsIgnoreCase(MessageFormat.BARTS_CSV)) {
			dateFormat = "yyyy-MM-dd";

		} else if (software.equalsIgnoreCase(MessageFormat.TPP_CSV)) {
			dateFormat = "yyyy-MM-dd'T'HH.mm.ss";

		} else if (software.equalsIgnoreCase(MessageFormat.HOMERTON_CSV)) {
			dateFormat = "yyyy-MM-dd";

		} else if (software.equalsIgnoreCase(MessageFormat.VISION_CSV)) {
			dateFormat = "yyyy-MM-dd'T'HH'.'mm'.'ss";

		} else if (software.equalsIgnoreCase(MessageFormat.ADASTRA_CSV)) {
			dateFormat = "yyyy-MM-dd'T'HH'.'mm'.'ss";

			//NOTE: If adding support for a new publisher software, remember to add to the OpenEnvelope class too
		} else {
			throw new Exception("Software [" + software + "} not supported for calculating last data date");
		}

		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

		//find the first file path in the JSON body and simply work down the path to find a directory
		//named in a way that matches the date format appropriate for the published software
		String firstFile = findFirstElement(body, "path"); //e.g. sftpReader/BARTSDW/2019-01-23/susecd.190259
		File f = new File(firstFile);
		while (f != null) {
			String name = f.getName();
			try {
				Date d = sdf.parse(name);
				return d;
			} catch (Exception ex) {
				f = f.getParentFile();
			}
		}

		throw new Exception("Failed to work out data date from " + firstFile);
	}

	/**
	 * the exchange body is always JSON, but rather than parsing the entire string into a Json structure, this
	 * function will find the first element for a given name without needing to do all that parsing
     */
	private static String findFirstElement(String json, String elementName) {
		elementName = "\"" + elementName + "\"";
		int index = json.indexOf(elementName);
		index = json.indexOf("\"", index + elementName.length()+1);
		int endIndex = json.indexOf("\"", index+1);
		String elementValue = json.substring(index+1, endIndex);
		return elementValue;
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

					String technicalInterfaceId = technicalInterface.getUuid();
					String technicalInterfaceFormat = technicalInterface.getMessageFormat();

					//the system version is now a regex that allows multiple versions to be supported in one system
					if (endpointInterfaceId.equals(technicalInterfaceId)
							&& technicalInterfaceFormat.equalsIgnoreCase(software)) {

						String technicalInterfaceVersion = technicalInterface.getMessageFormatVersion();
						if (Pattern.matches(technicalInterfaceVersion, messageVersion)) {
							return endpointSystemId;
						}
					}

					/*if (endpointInterfaceId.equals(technicalInterface.getUuid())
							&& technicalInterface.getMessageFormat().equalsIgnoreCase(software)
							&& technicalInterface.getMessageFormatVersion().equalsIgnoreCase(messageVersion)) {

						return endpointSystemId;
					}*/
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
