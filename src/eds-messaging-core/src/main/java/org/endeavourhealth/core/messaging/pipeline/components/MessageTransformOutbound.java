package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import org.endeavourhealth.core.cache.ObjectMapperPool;
import org.endeavourhealth.core.configuration.MessageTransformOutboundConfig;
import org.endeavourhealth.core.data.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.data.admin.QueuedMessageRepository;
import org.endeavourhealth.core.data.admin.ServiceRepository;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.core.json.JsonServiceInterfaceEndpoint;
import org.endeavourhealth.core.messaging.exchange.Exchange;
import org.endeavourhealth.core.messaging.exchange.HeaderKeys;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.messaging.pipeline.SubscriberBatch;
import org.endeavourhealth.core.messaging.pipeline.TransformBatch;
import org.endeavourhealth.core.xml.QueryDocument.ServiceContract;
import org.endeavourhealth.core.xml.QueryDocument.TechnicalInterface;
import org.endeavourhealth.subscriber.EnterpriseFiler;
import org.endeavourhealth.transform.common.MessageFormat;
import org.endeavourhealth.transform.enterprise.FhirToEnterpriseCsvTransformer;
import org.endeavourhealth.transform.vitrucare.FhirToVitruCareXmlTransformer;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class MessageTransformOutbound extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(MessageTransformOutbound.class);


	private MessageTransformOutboundConfig config;

	public MessageTransformOutbound(MessageTransformOutboundConfig config) {
		this.config = config;
	}

	/**
	 * hacked version of this fn to force data into Enterprise DB
     */
/*
	@Override
	public void process(Exchange exchange) throws PipelineException {

		TransformBatch transformBatch = getTransformBatch(exchange);

		String serviceIdStr = exchange.getHeader(HeaderKeys.SenderServiceUuid);
		UUID serviceId = UUID.fromString(serviceIdStr);
		String orgIdStr = exchange.getHeader(HeaderKeys.SenderOrganisationUuid);
		UUID orgId = UUID.fromString(orgIdStr);

		try {
			String outbound = EnterpriseFhirTransformer.transformFromFhir(serviceId, orgId, transformBatch.getBatchId(), null);
			EnterpriseFiler.file(outbound);

		} catch (Exception ex) {
			throw new PipelineException("Exception saving to Enterprise DB", ex);
		}
	}
*/

	@Override
	public void process(Exchange exchange) throws PipelineException {
		// Get the transformation data from the exchange
		// List of resources and subscriber service contracts
		TransformBatch transformBatch = getTransformBatch(exchange);
		UUID batchId = transformBatch.getBatchId();
		Map<ResourceType, List<UUID>> resourceIds = transformBatch.getResourceIds();

		// Run the transform, creating a subscriber batch for each
		// (Holds transformed message id and destination endpoints)
		List<SubscriberBatch> subscriberBatches = new ArrayList<>();

		for (ServiceContract serviceContract: transformBatch.getSubscribers()) {

			String technicalInterfaceUuidStr = serviceContract.getTechnicalInterface().getUuid();
			String systemUuidStr = serviceContract.getSystem().getUuid();
			TechnicalInterface technicalInterface = null;
			try {
				technicalInterface = LibraryRepositoryHelper.getTechnicalInterfaceDetails(systemUuidStr, technicalInterfaceUuidStr);
			} catch (Exception ex) {
				throw new PipelineException("Failed to retrieve technical interface", ex);
			}

			String software = technicalInterface.getMessageFormat();
			String softwareVersion = technicalInterface.getMessageFormatVersion();

			String outboundData = null;
			try {
				outboundData = transform(exchange, batchId, software, softwareVersion, resourceIds);
			} catch (Exception ex) {
				throw new PipelineException("Failed to transform exchange " + exchange + " and batch " + batchId, ex);
			}

			//not all transforms may actually decide to generate any outbound content, so check for null and empty
			if (!Strings.isNullOrEmpty(outboundData)) {

				// Store transformed message
				UUID messageUuid = UUID.randomUUID();
				new QueuedMessageRepository().save(messageUuid, outboundData);

				SubscriberBatch subscriberBatch = new SubscriberBatch();
				subscriberBatch.setTechnicalInterface(technicalInterface);
				subscriberBatch.setEndpoints(getSubscriberEndpoints(transformBatch));
				subscriberBatch.setOutputMessageId(messageUuid);

				subscriberBatches.add(subscriberBatch);
			}
		}

		String subscriberBatchesJson = null;
		try {
			subscriberBatchesJson = ObjectMapperPool.getInstance().writeValueAsString(subscriberBatches);
		} catch (JsonProcessingException e) {
			LOG.error("Error serializing subscriber batch JSON", e);
			throw new PipelineException("Error serializing subscriber batch JSON", e);
		}
		exchange.setHeader(HeaderKeys.SubscriberBatch, subscriberBatchesJson);
		LOG.trace("Message transformed (outbound)");
	}

	private String transform(Exchange exchange, UUID batchId, String software, String softwareVersion, Map<ResourceType, List<UUID>> resourceIds) throws Exception {

		if (software.equals(MessageFormat.ENTERPRISE_CSV)) {

			UUID senderOrganisationUuid = exchange.getHeaderAsUuid(HeaderKeys.SenderOrganisationUuid);

			String zippedCsvs = FhirToEnterpriseCsvTransformer.transformFromFhir(senderOrganisationUuid, batchId, resourceIds);

			//we file the data directly, so return null to end the pipeline
			EnterpriseFiler.file(zippedCsvs);
			return null;

		} else if (software.equals(MessageFormat.VITRUICARE_XML)) {

			String xml = FhirToVitruCareXmlTransformer.transformFromFhir(batchId, resourceIds);
			return xml;

		} else {
			throw new PipelineException("Unsupported outbound software " + software + " for exchange " + exchange.getExchangeId());
		}
	}

	/*@Override
	public void process(Exchange exchange) throws PipelineException {
		// Get the transformation data from the exchange
		// List of resources and subscriber service contracts
		TransformBatch transformBatch = getTransformBatch(exchange);

		// Get distinct list of technical interfaces that these resources need transforming to
		List<TechnicalInterface> interfaces = transformBatch.getSubscribers().stream()
				.map(sc -> sc.getTechnicalInterface())
				.distinct()
				.collect(Collectors.toList());

		// Run the transform, creating a subscriber batch for each
		// (Holds transformed message id and destination endpoints)
		List<SubscriberBatch> subscriberBatches = new ArrayList<>();

		for (TechnicalInterface technicalInterface : interfaces) {
			SubscriberBatch subscriberBatch = new SubscriberBatch();
			subscriberBatch.setTechnicalInterface(technicalInterface);
			List<String> endpoints = getSubscriberEndpoints(transformBatch);
			subscriberBatch.getEndpoints().addAll(endpoints);

			try {
				String serviceIdStr = exchange.getHeader(HeaderKeys.SenderServiceUuid);
				UUID serviceId = UUID.fromString(serviceIdStr);
				String orgIdStr = exchange.getHeader(HeaderKeys.SenderOrganisationUuid);
				UUID orglId = UUID.fromString(orgIdStr);

				String outbound = EnterpriseFhirTransformer.transformFromFhir(serviceId, orgId, transformBatch.getBatchId(), null);
				EnterpriseFiler.file(outbound);

				throw new PipelineException("Transform out not implemented", ex);

				// Store transformed message
				UUID messageUuid = UUID.randomUUID();
				new QueuedMessageRepository().save(messageUuid, outbound);
				subscriberBatch.setOutputMessageId(messageUuid);

				subscriberBatches.add(subscriberBatch);
			} catch (Exception ex) {
				throw new PipelineException("Exception tranforming to CEG CSV", ex);
			}
		}

		String subscriberBatchesJson = null;
		try {
			subscriberBatchesJson = ObjectMapperPool.getInstance().writeValueAsString(subscriberBatches);
		} catch (JsonProcessingException e) {
			LOG.error("Error serializing subscriber batch JSON", e);
			throw new PipelineException("Error serializing subscriber batch JSON", e);
		}
		exchange.setHeader(HeaderKeys.SubscriberBatch, subscriberBatchesJson);
		LOG.trace("Message transformed (outbound)");
	}*/

	private List<String> getSubscriberEndpoints(TransformBatch transformBatch) throws PipelineException {
		// Find the relevant endpoints for those subscribers/technical interface
		List<String> endpoints = new ArrayList<>();
		try {
			ServiceRepository serviceRepository = new ServiceRepository();
			for (ServiceContract contract : transformBatch.getSubscribers()) {
				Service service = serviceRepository.getById(UUID.fromString(contract.getService().getUuid()));
				List<JsonServiceInterfaceEndpoint> serviceEndpoints = ObjectMapperPool.getInstance().readValue(service.getEndpoints(), new TypeReference<List<JsonServiceInterfaceEndpoint>>() {});
				String endpoint = serviceEndpoints.stream()
						.filter(ep -> ep.getTechnicalInterfaceUuid().toString().equals(contract.getTechnicalInterface().getUuid()))
						.map(JsonServiceInterfaceEndpoint::getEndpoint)
						.findFirst()
						.get();
				endpoints.add(endpoint);
			}
		} catch (IOException e) {
			throw new PipelineException(e.getMessage(), e);
		}
		return endpoints;
	}

	private TransformBatch getTransformBatch(Exchange exchange) throws PipelineException {
		String transformBatchJson = exchange.getHeader(HeaderKeys.TransformBatch);
		try {
			return ObjectMapperPool.getInstance().readValue(transformBatchJson, TransformBatch.class);
		} catch (IOException e) {
			LOG.error("Error deserializing transformation batch JSON", e);
			throw new PipelineException("Error deserializing transformation batch JSON", e);
		}
	}



}
