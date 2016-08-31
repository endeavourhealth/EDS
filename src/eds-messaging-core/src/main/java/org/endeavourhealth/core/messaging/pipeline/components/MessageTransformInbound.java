package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.endeavourhealth.core.cache.ObjectMapperPool;
import org.endeavourhealth.core.configuration.MessageTransformInboundConfig;
import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.ActiveItem;
import org.endeavourhealth.core.data.admin.models.Item;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.json.JsonServiceInterfaceEndpoint;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.core.xml.QueryDocument.System;
import org.endeavourhealth.core.xml.QueryDocument.TechnicalInterface;
import org.endeavourhealth.core.xml.QueryDocumentSerializer;
import org.endeavourhealth.transform.common.exceptions.SoftwareNotSupportedException;
import org.endeavourhealth.transform.common.exceptions.VersionNotSupportedException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class MessageTransformInbound extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(MessageTransformInbound.class);

	private static final ServiceRepository serviceRepository = new ServiceRepository();
	private static final LibraryRepository libraryRepository = new LibraryRepository();

	private MessageTransformInboundConfig config;

	public MessageTransformInbound(MessageTransformInboundConfig config) {
		this.config = config;
	}

	private UUID findSystemId(Service service, String software, String messageVersion, UUID exchangeId) throws Exception {

		List<JsonServiceInterfaceEndpoint> endpoints = ObjectMapperPool.getInstance().readValue(service.getEndpoints(), new TypeReference<List<JsonServiceInterfaceEndpoint>>() {});
		for (JsonServiceInterfaceEndpoint endpoint: endpoints) {

			UUID endpointSystemId = endpoint.getSystemUuid();
			String endpointInterfaceId = endpoint.getTechnicalInterfaceUuid().toString();

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

		throw new PipelineException("Failed to find SystemId for service " + service.getId() + ", message "
				+ software + " and version " + messageVersion
				+ " when processing exchange " + exchangeId);
	}

	@Override
	public void process(Exchange exchange) throws PipelineException {

		try {

			UUID serviceId = UUID.fromString(exchange.getHeader(HeaderKeys.SenderServiceUuid));
			String software = exchange.getHeader(HeaderKeys.SourceSystem);
			String messageVersion = exchange.getHeader(HeaderKeys.SystemVersion);

			//find the organisation UUIDs covered by the service
			Service service = serviceRepository.getById(serviceId);

			//find the system ID by using values from the message header
			UUID systemId = findSystemId(service, software, messageVersion, exchange.getExchangeId());

			List<UUID> batchIds = null;

			if (software.equalsIgnoreCase("EMISCSV")) {
				batchIds = processEmisCsvTransform(exchange, serviceId, systemId, messageVersion, software);

			} else if (software.equalsIgnoreCase("EmisOpen")) {
				batchIds = processEmisOpenTransform(exchange, serviceId, systemId, messageVersion, software);

			} else if (software.equalsIgnoreCase("OpenHR")) {
				batchIds = processEmisOpenHrTransform(exchange, serviceId, systemId, messageVersion, software);

			} else if (software.equalsIgnoreCase("TPPExtractService")) {
				batchIds = processTppXmlTransform(exchange, serviceId, systemId, messageVersion, software);

			} else {
				throw new SoftwareNotSupportedException(software, messageVersion);
			}

			//update the Exchange with the batch IDs, for the next step in the pipeline
			String batchIdString = convertUUidsToStrings(batchIds);
			exchange.setHeader(HeaderKeys.BatchIds, batchIdString);

			LOG.trace("Message transformed (inbound)");

		} catch (Exception e) {
			exchange.setException(e);
			LOG.error("Error", e);
			throw new PipelineException("Error performing inbound transform", e);
		}
	}

	private static String convertUUidsToStrings(List<UUID> uuids) throws PipelineException {
		try {
			return ObjectMapperPool.getInstance().writeValueAsString(uuids.toArray());
		} catch (JsonProcessingException e) {
			throw new PipelineException("Could not serialize batch id list", e);
		}
	}

	private List<UUID> processEmisCsvTransform(Exchange exchange, UUID serviceId, UUID systemId, String version, String software) throws Exception {

		//validate the version
		if (!version.equalsIgnoreCase(EmisCsvTransformer.VERSION_TEST_PACK)
				&& !version.equalsIgnoreCase(EmisCsvTransformer.VERSION_5_1)) {
			throw new VersionNotSupportedException(software, version);
		}

		//for EMIS CSV, the exchange body will be a list of files received
		String decodedFileString = exchange.getBody();
		String[] decodedFiles = decodedFileString.split("\r\n");
		String sharedStoragePath = config.getSharedStoragePath();

		return EmisCsvTransformer.transform(version, sharedStoragePath, decodedFiles, exchange.getExchangeId(), serviceId, systemId);
	}

	private List<UUID> processTppXmlTransform(Exchange exchange, UUID serviceId, UUID systemId, String version, String software) throws Exception {
		//TODO - validate version for TPP XML
		//TODO - plug in TPP XML transform
		return null;
	}

	private List<UUID> processEmisOpenTransform(Exchange exchange, UUID serviceId, UUID systemId, String version, String software) throws Exception {
		//TODO - validate version for EMIS Open
		//TODO - plug in EMIS OPEN transform
		return null;
	}

	private List<UUID> processEmisOpenHrTransform(Exchange exchange, UUID serviceId, UUID systemId, String version, String software) throws Exception {
		//TODO - validate version for Open HR
		//TODO - plug in OpenHR transform
		return null;
	}
}
