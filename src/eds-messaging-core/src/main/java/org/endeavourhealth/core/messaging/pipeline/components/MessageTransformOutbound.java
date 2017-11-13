package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.core.configuration.MessageTransformOutboundConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.QueuedMessageDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.audit.models.QueuedMessageType;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.dal.subscriberTransform.ExchangeBatchExtraResourceDalI;
import org.endeavourhealth.core.fhirStorage.JsonServiceInterfaceEndpoint;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.messaging.pipeline.SubscriberBatch;
import org.endeavourhealth.core.messaging.pipeline.TransformBatch;
import org.endeavourhealth.core.xml.QueryDocument.ServiceContract;
import org.endeavourhealth.core.xml.QueryDocument.TechnicalInterface;
import org.endeavourhealth.transform.common.MessageFormat;
import org.endeavourhealth.transform.enterprise.FhirToEnterpriseCsvTransformer;
import org.endeavourhealth.transform.vitrucare.FhirToVitruCareXmlTransformer;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;


public class MessageTransformOutbound extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(MessageTransformOutbound.class);


	private MessageTransformOutboundConfig config;

	public MessageTransformOutbound(MessageTransformOutboundConfig config) {
		this.config = config;
	}


	@Override
	public void process(Exchange exchange) throws PipelineException {
		// Get the transformation data from the exchange
		// List of resources and subscriber service contracts
		TransformBatch transformBatch = getTransformBatch(exchange);
		UUID batchId = transformBatch.getBatchId();
		UUID protocolId = transformBatch.getProtocolId();
		Map<ResourceType, List<UUID>> resourceIds = transformBatch.getResourceIds();

		// Run the transform, creating a subscriber batch for each
		// (Holds transformed message id and destination endpoints)
		List<SubscriberBatch> subscriberBatches = new ArrayList<>();

		for (ServiceContract serviceContract : transformBatch.getSubscribers()) {

			String endpoint = getSubscriberEndpoint(serviceContract);

			//subscribers that we don't actively push to (e.g. patient explorer) won't have an endpoint set, so skip it
			if (Strings.isNullOrEmpty(endpoint)) {
				continue;
			}

			String technicalInterfaceUuidStr = serviceContract.getTechnicalInterface().getUuid();
			String systemUuidStr = serviceContract.getSystem().getUuid();
			TechnicalInterface technicalInterface = null;
			try {
				//use a function that caches them for a minute at a time
				technicalInterface = LibraryRepositoryHelper.getTechnicalInterfaceDetailsUsingCache(systemUuidStr, technicalInterfaceUuidStr);
				//technicalInterface = LibraryRepositoryHelper.getTechnicalInterfaceDetails(systemUuidStr, technicalInterfaceUuidStr);
			} catch (Exception ex) {
				throw new PipelineException("Failed to retrieve technical interface for system " + systemUuidStr + " and technical interface " + technicalInterfaceUuidStr + " for protocol " + transformBatch.getProtocolId(), ex);
			}
			/*LOG.debug("Technical interface found for system " + systemUuidStr + " and interface id " + technicalInterfaceUuidStr + " = " + (technicalInterface != null));
			LOG.debug("Name {} UUID {} Frequency {} MessageType {} MessageFormat {} MessageFormatVersion {}",
					technicalInterface.getName(), technicalInterface.getUuid(), technicalInterface.getFrequency(),
					technicalInterface.getMessageType(), technicalInterface.getMessageFormat(), technicalInterface.getMessageFormatVersion());*/

			String software = technicalInterface.getMessageFormat();
			String softwareVersion = technicalInterface.getMessageFormatVersion();


			String outboundData = null;
			try {
				outboundData = transform(exchange, batchId, software, softwareVersion, resourceIds, endpoint, protocolId);
			} catch (Exception ex) {
				throw new PipelineException("Failed to transform exchange " + exchange.getId() + " and batch " + batchId, ex);
			}

			//not all transforms may actually decide to generate any outbound content, so check for null and empty
			if (!Strings.isNullOrEmpty(outboundData)) {

				// Store transformed message
				UUID messageUuid = UUID.randomUUID();

				try {
					QueuedMessageDalI queuedMessageDal = DalProvider.factoryQueuedMessageDal();
					queuedMessageDal.save(messageUuid, outboundData, QueuedMessageType.OutboundData);
				} catch (Exception ex) {
					throw new PipelineException("Failed to save queued message", ex);
				}

				SubscriberBatch subscriberBatch = new SubscriberBatch();
				subscriberBatch.setQueuedMessageId(messageUuid);
				subscriberBatch.setEndpoint(endpoint);
				subscriberBatch.setSoftware(software);
				subscriberBatch.setSoftwareVersion(softwareVersion);
				subscriberBatch.setTechnicalInterfaceId(UUID.fromString(technicalInterfaceUuidStr));

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
		//LOG.trace("Message transformed (outbound)");
	}

	private String transform(Exchange exchange,
							 UUID batchId,
							 String software,
							 String softwareVersion,
							 Map<ResourceType, List<UUID>> resourceIds,
							 String endpoint,
							 UUID protocolId) throws Exception {

		//retrieve our resources
		UUID exchangeId = exchange.getId();
		List<ResourceWrapper> filteredResources = getResources(exchangeId, batchId, resourceIds, endpoint);
		if (filteredResources.isEmpty()) {
			return null;
		}

		if (software.equals(MessageFormat.ENTERPRISE_CSV)) {

			UUID serviceId = exchange.getHeaderAsUuid(HeaderKeys.SenderServiceUuid);
			UUID systemId = exchange.getHeaderAsUuid(HeaderKeys.SenderSystemUuid);

			//have to pass in the exchange body now
			String body = exchange.getBody();
			return FhirToEnterpriseCsvTransformer.transformFromFhir(serviceId, systemId, exchangeId, batchId, filteredResources, endpoint, protocolId, body);

		} else if (software.equals(MessageFormat.VITRUICARE_XML)) {
			return FhirToVitruCareXmlTransformer.transformFromFhir(batchId, filteredResources, endpoint);

		} else {
			throw new PipelineException("Unsupported outbound software " + software + " for exchange " + exchange.getId());
		}
	}

	/*private static void sendHttpPost(String payload, String url) throws Exception {

		//String url = "http://127.0.0.1:8002/notify";
		//String url = "http://localhost:8002";
		//String url = "http://posttestserver.com/post.php";

		if (url == null || url.length() <= "http://".length()) {
			LOG.trace("No/invalid url : [" + url + "]");
			return;
		}

		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);


		// add header
		//post.setHeader("User-Agent", USER_AGENT);

		*//*List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("sn", "C02G8416DRJM"));
		urlParameters.add(new BasicNameValuePair("cn", ""));
		urlParameters.add(new BasicNameValuePair("locale", ""));
		urlParameters.add(new BasicNameValuePair("caller", ""));
		urlParameters.add(new BasicNameValuePair("num", "12345"));

		post.setEntity(new UrlEncodedFormEntity(urlParameters));*//*

		HttpEntity entity = new ByteArrayEntity(payload.getBytes("UTF-8"));
		post.setEntity(entity);

		LOG.trace("Sending 'POST' request to URL : " + url);
		LOG.trace("Post parameters : " + post.getEntity());

		HttpResponse response = client.execute(post);
		LOG.trace("Response Code : " + response.getStatusLine().getStatusCode());

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}

		LOG.trace(result.toString());

		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != HttpStatus.SC_OK) {
			throw new IOException("Failed to post to " + url);
		}
	}*/

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

	private String getSubscriberEndpoint(ServiceContract contract) throws PipelineException {

		try {
			UUID serviceId = UUID.fromString(contract.getService().getUuid());
			UUID technicalInterfaceId = UUID.fromString(contract.getTechnicalInterface().getUuid());

			ServiceDalI serviceRepository = DalProvider.factoryServiceDal();

			Service service = serviceRepository.getById(serviceId);
			List<JsonServiceInterfaceEndpoint> serviceEndpoints = ObjectMapperPool.getInstance().readValue(service.getEndpoints(), new TypeReference<List<JsonServiceInterfaceEndpoint>>() {});
			for (JsonServiceInterfaceEndpoint serviceEndpoint: serviceEndpoints) {
				if (serviceEndpoint.getTechnicalInterfaceUuid().equals(technicalInterfaceId)) {
					return serviceEndpoint.getEndpoint();
				}
			}

			return null;

		} catch (Exception ex) {
			throw new PipelineException("Failed to get endpoint for contract", ex);
		}
	}

	/*private List<String> getSubscriberEndpoints(TransformBatch transformBatch) throws PipelineException {
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
	}*/

	private TransformBatch getTransformBatch(Exchange exchange) throws PipelineException {
		String transformBatchJson = exchange.getHeader(HeaderKeys.TransformBatch);
		try {
			return ObjectMapperPool.getInstance().readValue(transformBatchJson, TransformBatch.class);
		} catch (IOException e) {
			throw new PipelineException("Error deserializing transformation batch JSON", e);
		}
	}


	private static List<ResourceWrapper> getResources(UUID exchangeId, UUID batchId, Map<ResourceType, List<UUID>> resourceIds, String subscriberConfigName) throws Exception {

		//get the resources actually in the batch we're transforming
		ResourceDalI resourceDal = DalProvider.factoryResourceDal();
		List<ResourceWrapper> resources = resourceDal.getResourcesForBatch(batchId);

		//then add in any EXTRA resources we've previously calculated we'll need to include
		ExchangeBatchExtraResourceDalI exchangeBatchExtraResourceDalI = DalProvider.factoryExchangeBatchExtraResourceDal(subscriberConfigName);
		Map<ResourceType, List<UUID>> extraReourcesByType = exchangeBatchExtraResourceDalI.findExtraResources(exchangeId, batchId);
		for (ResourceType resourceType: extraReourcesByType.keySet()) {
			List<UUID> extraIds = extraReourcesByType.get(resourceType);
			for (UUID extraId: extraIds) {
				ResourceWrapper extraResource = resourceDal.getCurrentVersion(resourceType.toString(), extraId);
				resources.add(extraResource);
			}
		}

		//then filter to remove duplicates and ones the data set has filtered out
		resources = filterResources(resources, resourceIds);
		resources = pruneOlderDuplicates(resources);

		return resources;
	}

	/**
	 * we can end up with multiple instances of the same resource in a batch (or at least the Emis test data can)
	 * so strip out all but the latest version of each resource, so we're not wasting time sending over
	 * data that will immediately be overwritten and also we don't need to make sure to process them in order
	 */
	private static List<ResourceWrapper> pruneOlderDuplicates(List<ResourceWrapper> resources) {

		HashMap<UUID, UUID> hmLatestVersion = new HashMap<>();
		List<ResourceWrapper> ret = new ArrayList<>();

		for (ResourceWrapper resource: resources) {
			UUID id = resource.getResourceId();
			UUID version = resource.getVersion();

			//we'll have a null version for resource wrappers that we've added as "extra" to the exchange
			//batch using the exchange_batch_extra_resource table, as we just load the most recent version,
			//so just skip these as we'll automatically keep anything with a null version, below
			if (version == null) {
				continue;
			}

			UUID latestVersion = hmLatestVersion.get(id);
			if (latestVersion == null) {
				hmLatestVersion.put(id, version);

			} else {
				int comp = version.compareTo(latestVersion);
				if (comp > 0) {
					hmLatestVersion.put(id, latestVersion);
				}
			}
		}

		for (ResourceWrapper resource: resources) {
			UUID id = resource.getResourceId();
			UUID version = resource.getVersion();

			//we'll have a null version for resource wrappers that we've added as "extra" to the exchange
			//batch using the exchange_batch_extra_resource table, as we just load the most recent version,
			//so always just add these
			if (version == null) {
				ret.add(resource);

			} else {
				//if we have a version, make sure ours is the latest version according to our map
				UUID latestVersion = hmLatestVersion.get(id);
				if (latestVersion.equals(version)) {
					ret.add(resource);
				}
			}
		}

		return ret;
	}

	private static List<ResourceWrapper> filterResources(List<ResourceWrapper> allResources,
														 Map<ResourceType, List<UUID>> resourceIdsToKeep) throws Exception {

		List<ResourceWrapper> ret = new ArrayList<>();

		for (ResourceWrapper resource: allResources) {
			UUID resourceId = resource.getResourceId();
			ResourceType resourceType = ResourceType.valueOf(resource.getResourceType());

			//the map of resource IDs tells us the resources that passed the protocol and should be passed
			//to the subscriber. However, any resources that should be deleted should be passed, whether the
			//protocol says to include it or not, since it may have previously been passed to the subscriber anyway
			if (resource.isDeleted()) {
				ret.add(resource);

			} else {

				//during testing, the resource ID is null, so handle this
				if (resourceIdsToKeep == null) {
					ret.add(resource);
					continue;
				}

				List<UUID> uuidsToKeep = resourceIdsToKeep.get(resourceType);
				if (uuidsToKeep != null
						|| uuidsToKeep.contains(resourceId)) {
					ret.add(resource);
				}
			}
		}

		return ret;
	}

}
