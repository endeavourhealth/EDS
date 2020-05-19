package org.endeavourhealth.core.messaging.pipeline.components;

import com.google.common.base.Strings;
import org.endeavourhealth.common.cache.ParserPool;
import org.endeavourhealth.common.fhir.schema.OrganisationType;
import org.endeavourhealth.common.ods.OdsOrganisation;
import org.endeavourhealth.common.ods.OdsWebService;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.core.configuration.OpenEnvelopeConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.SystemHelper;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.audit.models.LastDataReceived;
import org.endeavourhealth.core.database.dal.usermanager.caching.OrganisationCache;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.xml.QueryDocument.System;
import org.endeavourhealth.core.xml.QueryDocument.TechnicalInterface;
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
import java.util.*;

public class OpenEnvelope extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(OpenEnvelope.class);

	private static final String TAG_BULK = "Bulk";

	private OpenEnvelopeConfig config;

	private static ServiceDalI serviceDal = DalProvider.factoryServiceDal();

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
			calculateLastDataDate(exchange); //work out when the data was from
			populateBulkTag(exchange);

			//commit what we've just received to the DB
			AuditWriter.writeExchange(exchange);

		} catch (Exception e) {
			throw new PipelineException(e.getMessage(), e);
		}

		LOG.debug("Message envelope processed");
	}

	/**
	 * if the exchange is flagged as a bulk, then populate the tag on the service
     */
	private void populateBulkTag(Exchange exchange) throws Exception {
		Boolean isBulk = exchange.getHeaderAsBoolean(HeaderKeys.IsBulk);
		if (isBulk == null
				|| !isBulk.booleanValue()) {
			return;
		}

		UUID serviceId = exchange.getServiceId();
		Service service = serviceDal.getById(serviceId);

		Map<String, String> tags = service.getTags();
		if (tags == null) {
			tags = new HashMap<>();
		}

		//if this is another bulk, don't bother updating the service again
		if (tags.containsKey(TAG_BULK)) {
			return;
		}

		Date dataData = exchange.getHeaderAsDate(HeaderKeys.DataDate);
		if (dataData == null) {
			tags.put(TAG_BULK, "Exchange " + exchange.getId());
		} else {
			tags.put(TAG_BULK, new SimpleDateFormat("yyyy-MM-dd").format(dataData));
		}

		service.setTags(tags);
		serviceDal.save(service);
	}

	private void calculateLastDataDate(Exchange exchange) throws PipelineException {

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
			exchange.setHeaderAsDate(HeaderKeys.DataDate, lastDataDate);

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

		} else if (software.equalsIgnoreCase(MessageFormat.BHRUT_CSV)) {
			dateFormat = "yyyy-MM-dd'T'HH'.'mm'.'ss";

		  //NOTE: If adding support for a new publisher software, remember to add to the MessageTransformInbound class too
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

		//just carry over fields from the request to the exchange
		exchange.setHeader(HeaderKeys.MessageId, messageHeader.getId());
		exchange.setHeader(HeaderKeys.SenderLocalIdentifier, messageHeader.getSource().getName());
		exchange.setHeader(HeaderKeys.SourceSystem, messageHeader.getSource().getSoftware());
		exchange.setHeader(HeaderKeys.SystemVersion, messageHeader.getSource().getVersion());
		exchange.setHeader(HeaderKeys.ResponseUri, messageHeader.getSource().getEndpoint());
		exchange.setHeader(HeaderKeys.MessageEvent, messageHeader.getEvent().getCode());

		//validate that the sender is one we know off
		processSender(exchange);

		//carry over any explicit destinations
		processDestinations(exchange, messageHeader);
	}

	private void processSender(Exchange exchange) throws PipelineException {

		String organisationOds = exchange.getHeader(HeaderKeys.SenderLocalIdentifier);
		String software = exchange.getHeader(HeaderKeys.SourceSystem);
		String version = exchange.getHeader(HeaderKeys.SystemVersion);

		//ensure we can match to a Service
		Service service = findOrCreateService(organisationOds);

		//ensure we can match to a System at that Service
		UUID systemUuid = findSystemId(service, software, version);

		exchange.setHeader(HeaderKeys.SenderServiceUuid, service.getId().toString());
		exchange.setHeader(HeaderKeys.SenderSystemUuid, systemUuid.toString());

		exchange.setServiceId(service.getId());
		exchange.setSystemId(systemUuid);
	}

	private Service findOrCreateService(String organisationOds) throws PipelineException {
		try {
			//see if we've already got a service
			Service s = serviceDal.getByLocalIdentifier(organisationOds);
			if (s != null) {
				return s;
			}

			//if no service already exists, then see if we can create one, but only if we have a DPA
			Boolean hasDpa = OrganisationCache.doesOrganisationHaveDPA(organisationOds);
			if (!hasDpa.booleanValue()) {
				throw new PipelineException("Data received for ODS code " + organisationOds + " but will not auto-create bacause no DPA exists");
			}

			OdsOrganisation odsOrg = OdsWebService.lookupOrganisationViaRest(organisationOds);
			if (odsOrg == null) {
				throw new PipelineException("Data received for ODS code " + organisationOds + " but could not find on ODS, so service needs manually setting up in DDS-UI");
			}

			s = new Service();
			s.setLocalId(organisationOds);

			String name = odsOrg.getOrganisationName();
			s.setName(name);

			String postcode = odsOrg.getPostcode();
			s.setPostcode(postcode);

			OrganisationType type = odsOrg.getOrganisationType();
			s.setOrganisationType(type);

			String parentCode = OdsOrganisation.getBestParentCode(odsOrg);
			s.setCcgCode(parentCode);

			//tell us because we need to manually do a couple of steps
			String msg = "Auto-created Service for ODS code " + organisationOds + " in Messaging API\r\n"
					+ s.toString()
					+ "\r\nPublisher config name and tags need setting in DDS-UI";
			SlackHelper.sendSlackMessage(SlackHelper.Channel.QueueReaderAlerts, msg);

			serviceDal.save(s);

			return s;

		} catch (PipelineException pe) {
			//any pipeline exceptions, just throw them as is
			throw pe;

		} catch (Exception ex) {
			//any other exception (e.g. from database error), then we need to wrap up
			throw new PipelineException("Failed at auto-creating Service for ODS code " + organisationOds, ex);
		}
	}




	private UUID findSystemId(Service service, String software, String messageVersion) throws PipelineException {

		try {
			ServiceInterfaceEndpoint endpoint = SystemHelper.findEndpointForSoftwareAndVersion(service, software, messageVersion);

			if (endpoint == null) {
				//create new draft service
				createDraftEndpoint(service, software, messageVersion);
				throw new PipelineException("Endpoint for " + software + " has been automatically added to service " + service.getLocalId() + " but in draft mode - manually change when data ready to process");

			} else if (endpoint.getEndpoint() != null
					&& endpoint.getEndpoint().equals(ServiceInterfaceEndpoint.STATUS_DRAFT)) {
				//endpoint exists but is in DRAFT mode
				throw new PipelineException("Endpoint for " + software + " already exists in draft mode - manually change to process data");

			} else {
				//endpoint exists and is OK
				return endpoint.getSystemUuid();
			}

		} catch (PipelineException pe) {
			//any pipeline exceptions, just throw them as is
			throw pe;

		} catch (Exception e) {
			throw new PipelineException("Failed to find or create system for service " + service.getId() + " software " + software + " version " + messageVersion);
		}
	}

	private void createDraftEndpoint(Service service, String software, String messageVersion) throws Exception {

		System system = SystemHelper.findSystemForSoftwareAndVersion(software, messageVersion);
		TechnicalInterface technicalInterface = SystemHelper.getTechnicalInterface(system);
		String technicalInterfaceId = technicalInterface.getUuid();

		ServiceInterfaceEndpoint endpoint = new ServiceInterfaceEndpoint();
		endpoint.setEndpoint(ServiceInterfaceEndpoint.STATUS_DRAFT); //set in draft mode
		endpoint.setSystemUuid(UUID.fromString(system.getUuid()));
		endpoint.setTechnicalInterfaceUuid(UUID.fromString(technicalInterfaceId));

		List<ServiceInterfaceEndpoint> endpoints = service.getEndpointsList();
		endpoints.add(endpoint);
		service.setEndpointsList(endpoints);

		serviceDal.save(service);
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

		if (!destinationUriList.isEmpty()) {
			exchange.setHeader(HeaderKeys.DestinationAddress, String.join(",", destinationUriList));
		}
	}
}
