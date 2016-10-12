package org.endeavourhealth.core.messaging.pipeline.components;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.endeavourhealth.core.cache.ObjectMapperPool;
import org.endeavourhealth.core.configuration.MessageTransformInboundConfig;
import org.endeavourhealth.core.data.admin.LibraryRepository;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.ActiveItem;
import org.endeavourhealth.core.data.admin.models.Item;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.data.audit.AuditRepository;
import org.endeavourhealth.core.data.audit.models.ExchangeTransform;
import org.endeavourhealth.core.json.JsonServiceInterfaceEndpoint;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.core.xml.QueryDocument.System;
import org.endeavourhealth.core.xml.QueryDocument.TechnicalInterface;
import org.endeavourhealth.core.xml.QueryDocumentSerializer;
import org.endeavourhealth.core.xml.TransformErrorsSerializer;
import org.endeavourhealth.core.xml.transformErrors.TransformError;
import org.endeavourhealth.transform.common.exceptions.SoftwareNotSupportedException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
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

			//create the object that audits the transform and stores any errors
			ExchangeTransform transformAudit = createTransformAudit(serviceId, systemId, exchange.getExchangeId());
			TransformError transformError = new TransformError();

			List<UUID> batchIds = null;

			if (software.equalsIgnoreCase("EMISCSV")) {
				batchIds = processEmisCsvTransform(exchange, serviceId, systemId, messageVersion, software, transformError);

			} else if (software.equalsIgnoreCase("EmisOpen")) {
				batchIds = processEmisOpenTransform(exchange, serviceId, systemId, messageVersion, software, transformError);

			} else if (software.equalsIgnoreCase("OpenHR")) {
				batchIds = processEmisOpenHrTransform(exchange, serviceId, systemId, messageVersion, software, transformError);

			} else if (software.equalsIgnoreCase("TPPExtractService")) {
				batchIds = processTppXmlTransform(exchange, serviceId, systemId, messageVersion, software, transformError);

			} else {
				throw new SoftwareNotSupportedException(software, messageVersion);
			}

			//update the Exchange with the batch IDs, for the next step in the pipeline
			String batchIdString = convertUUidsToStrings(batchIds);
			exchange.setHeader(HeaderKeys.BatchIds, batchIdString);

			//save our audit of the transform
			transformAudit.setErrorXml(TransformErrorsSerializer.writeToXml(transformError));
			transformAudit.setEnded(new Date());
			new AuditRepository().save(transformAudit);

			LOG.trace("Message transformed (inbound)");

		} catch (Exception e) {
			exchange.setException(e);
			LOG.error("Error", e);
			throw new PipelineException("Error performing inbound transform", e);

		}
	}

	private static ExchangeTransform createTransformAudit(UUID serviceId, UUID systemId, UUID exchangeId) {
		ExchangeTransform tranformAudit = new ExchangeTransform();
		tranformAudit.setServiceId(serviceId);
		tranformAudit.setSystemId(systemId);
		tranformAudit.setExchangeId(exchangeId);
		tranformAudit.setVersion(UUIDs.timeBased());
		tranformAudit.setStarted(new Date());
		return tranformAudit;
	}

	private static String convertUUidsToStrings(List<UUID> uuids) throws PipelineException {

		//transforms may return null lists, if they didn't insert any new data, so just handle the null
		if (uuids == null) {
			uuids = new ArrayList<>();
		}

		try {
			return ObjectMapperPool.getInstance().writeValueAsString(uuids.toArray());
		} catch (JsonProcessingException e) {
			throw new PipelineException("Could not serialize batch id list", e);
		}
	}

	private List<UUID> processEmisCsvTransform(Exchange exchange, UUID serviceId, UUID systemId, String version,
											   String software, TransformError transformError) throws Exception {

		//for EMIS CSV, the exchange body will be a list of files received
		String decodedFileString = exchange.getBody();
		String[] decodedFiles = decodedFileString.split(java.lang.System.lineSeparator());
		String sharedStoragePath = config.getSharedStoragePath();

		return EmisCsvTransformer.transform(version, sharedStoragePath, decodedFiles,
											exchange.getExchangeId(), serviceId, systemId, transformError);
	}

	private List<UUID> processTppXmlTransform(Exchange exchange, UUID serviceId, UUID systemId, String version,
											  String software, TransformError transformError) throws Exception {
		//TODO - plug in TPP XML transform
		return null;
	}

	private List<UUID> processEmisOpenTransform(Exchange exchange, UUID serviceId, UUID systemId, String version,
												String software, TransformError transformError) throws Exception {
		//TODO - plug in EMIS OPEN transform
		return null;
	}

	private List<UUID> processEmisOpenHrTransform(Exchange exchange, UUID serviceId, UUID systemId, String version,
												  String software, TransformError transformError) throws Exception {
		//TODO - plug in OpenHR transform
		return null;
	}
}
