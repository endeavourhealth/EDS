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
import org.endeavourhealth.core.data.audit.models.ExchangeTransformAudit;
import org.endeavourhealth.core.data.audit.models.ExchangeTransformErrorState;
import org.endeavourhealth.core.json.JsonServiceInterfaceEndpoint;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.xml.QueryDocument.LibraryItem;
import org.endeavourhealth.core.xml.QueryDocument.System;
import org.endeavourhealth.core.xml.QueryDocument.TechnicalInterface;
import org.endeavourhealth.core.xml.QueryDocumentSerializer;
import org.endeavourhealth.core.xml.TransformErrorSerializer;
import org.endeavourhealth.core.xml.TransformErrorUtility;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.common.exceptions.SoftwareNotSupportedException;
import org.endeavourhealth.transform.emis.EmisCsvTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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

			List<UUID> batchIds = transform(serviceId, systemId, exchange, software, messageVersion);

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

	private List<UUID> transform(UUID serviceId,
								UUID systemId,
								Exchange exchange,
								String software,
								String messageVersion) throws Exception {

		List<UUID> batchIds = null;

		//create the object that audits the transform and stores any errors
		ExchangeTransformAudit transformAudit = createTransformAudit(serviceId, systemId, exchange.getExchangeId());

		//find the current error state for the source of our data
		ExchangeTransformErrorState errorState = new AuditRepository().getErrorState(serviceId, systemId);

		if (canTransformExchange(errorState, exchange.getExchangeId())) {

			//retrieve the audit of any errors from the last time we processed this exchange ID
			TransformError previousErrors = findPreviousErrors(serviceId, systemId, exchange.getExchangeId());

			try {

				if (software.equalsIgnoreCase("EMISCSV")) {
					batchIds = processEmisCsvTransform(exchange, serviceId, systemId, messageVersion, software, transformAudit, previousErrors);

				} else if (software.equalsIgnoreCase("EmisOpen")) {
					batchIds = processEmisOpenTransform(exchange, serviceId, systemId, messageVersion, software, transformAudit, previousErrors);

				} else if (software.equalsIgnoreCase("OpenHR")) {
					batchIds = processEmisOpenHrTransform(exchange, serviceId, systemId, messageVersion, software, transformAudit, previousErrors);

				} else if (software.equalsIgnoreCase("TPPExtractService")) {
					batchIds = processTppXmlTransform(exchange, serviceId, systemId, messageVersion, software, transformAudit, previousErrors);

				} else {
					throw new SoftwareNotSupportedException(software, messageVersion);
				}

			}
			catch (Exception ex) {

				LOG.error("Error processing exchange " + exchange.getExchangeId() + " from service " + serviceId + " and system " + systemId, ex);

				//record the exception as a fatal error with the exchange
				Map<String, String> args = new HashMap<>();
				args.put(TransformErrorUtility.ARG_FATAL_ERROR, ex.getMessage());
				TransformErrorUtility.addTransformError(transformAudit, ex, args);
			}


		} else {
			LOG.info("NOT performing transform for Exchange {} because previous Exchange went into error", exchange.getExchangeId());

			//record the exception as a fatal error with the exchange
			Map<String, String> args = new HashMap<>();
			args.put(TransformErrorUtility.ARG_WAITING, null);
			TransformErrorUtility.addTransformError(transformAudit, null, args);
		}

		//if we had any errors with the transform, update the error state for this service and system, so
		//we don't attempt to run any further exchanges from the same source until the error is resolved
		updateErrorState(errorState, serviceId, systemId, exchange.getExchangeId(), transformAudit);

		//save our audit of the transform
		transformAudit.setEnded(new Date());
		new AuditRepository().save(transformAudit);

		return batchIds;
	}

	/**
	 * if a transform fails, we need to record the error state to prevent further exchanges from the same source
	 * being processed until the error is resolved
	 */
	private void updateErrorState(ExchangeTransformErrorState errorState,
								  UUID serviceId,
								  UUID systemId,
								  UUID exchangeId,
								  ExchangeTransformAudit transformAudit) {

		boolean hadError = transformAudit.getErrorXml() != null;

		if (errorState == null) {

			if (!hadError) {
				//if we aren't in an error state, and didn't have any new error, then there's nothing to do
				return;

			} else {
				//if we've just had our first error, create the error state, and add the exchange ID to it
				errorState = new ExchangeTransformErrorState();
				errorState.setServiceId(serviceId);
				errorState.setSystemId(systemId);
				errorState.getExchangeIdsInError().add(exchangeId);
				new AuditRepository().save(errorState);
			}

		} else {

			//whether we had an error or not we need to say we re-processed this exchange (if this was a brand
			//new exchange, then this won't hurt)
			new AuditRepository().deleteExchangeIdToReProcess(errorState, exchangeId);

			if (!hadError) {
				//if we didn't have an error, remove our exchange ID from the error state object
				errorState.getExchangeIdsInError().remove(exchangeId);

			} else {
				//if we did have an error, then add to the error state (unless it's already there)
				if (!errorState.getExchangeIdsInError().contains(exchangeId)) {
					errorState.getExchangeIdsInError().add(exchangeId);
				}
			}

			//if the error state object is now empty, delete it
			if (errorState.getExchangeIdsInError().isEmpty()) {
				new AuditRepository().delete(errorState);
			} else {
				new AuditRepository().save(errorState);
			}
		}
	}

	private TransformError findPreviousErrors(UUID serviceId, UUID systemId, UUID exchangeId) {

		ExchangeTransformAudit previous = new AuditRepository().getMostRecentExchangeTransform(serviceId, systemId, exchangeId);
		if (previous == null) {
			return null;
		}

		try {
			return TransformErrorSerializer.readFromXml(previous.getErrorXml());
		} catch (Exception ex) {
			LOG.error("Error parsing XML " + previous.getErrorXml(), ex);
			return null;
		}
	}

	/**
	 * checks the last received exchange for this service and system and sees if it completed without errors or not
	 * If the last one didn't have errors (or is the same exchange as we're running now), then it'll let this
	 * new exchange be processed.
     */
	private static boolean canTransformExchange(ExchangeTransformErrorState errorState, UUID exchangeId) {

		//if our service and system aren't in error, then run the transform
		if (errorState == null) {
			return true;
		}

		//if we are in an error state, then we can only allow re-running of the first Exchange that caused us to go into error
		UUID firstExchangeInError = errorState.getExchangeIdsInError().get(0);
		if (firstExchangeInError.equals(exchangeId)) {
			return true;
		}

		//if our service and system are in error, but our exchange hasn't been re-submitted,
		//then we can't process any further exchanges from that source, until the first error is fixed
		return false;
	}

	private static ExchangeTransformAudit createTransformAudit(UUID serviceId, UUID systemId, UUID exchangeId) {
		ExchangeTransformAudit transformAudit = new ExchangeTransformAudit();
		transformAudit.setServiceId(serviceId);
		transformAudit.setSystemId(systemId);
		transformAudit.setExchangeId(exchangeId);
		transformAudit.setVersion(UUIDs.timeBased());
		transformAudit.setStarted(new Date());
		return transformAudit;
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
											   String software, ExchangeTransformAudit transformAudit,
											   TransformError previousErrors) throws Exception {

		//for EMIS CSV, the exchange body will be a list of files received
		String decodedFileString = exchange.getBody();
		String[] decodedFiles = decodedFileString.split(java.lang.System.lineSeparator());
		String sharedStoragePath = config.getSharedStoragePath();

		return EmisCsvTransformer.transform(version, sharedStoragePath, decodedFiles,
											exchange.getExchangeId(), serviceId, systemId,
											transformAudit, previousErrors);
	}

	private List<UUID> processTppXmlTransform(Exchange exchange, UUID serviceId, UUID systemId, String version,
											  String software, ExchangeTransformAudit transformAudit,
											  TransformError previousErrors) throws Exception {
		//TODO - plug in TPP XML transform
		return null;
	}

	private List<UUID> processEmisOpenTransform(Exchange exchange, UUID serviceId, UUID systemId, String version,
												String software, ExchangeTransformAudit transformAudit,
												TransformError previousErrors) throws Exception {
		//TODO - plug in EMIS OPEN transform
		return null;
	}

	private List<UUID> processEmisOpenHrTransform(Exchange exchange, UUID serviceId, UUID systemId, String version,
												  String software, ExchangeTransformAudit transformAudit,
												  TransformError previousErrors) throws Exception {
		//TODO - plug in OpenHR transform
		return null;
	}
}
