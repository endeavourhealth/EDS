package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.configuration.MessageTransformOutboundConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.QueuedMessageDalI;
import org.endeavourhealth.core.database.dal.audit.models.Exchange;
import org.endeavourhealth.core.database.dal.audit.models.ExchangeSubscriberTransformAudit;
import org.endeavourhealth.core.database.dal.audit.models.HeaderKeys;
import org.endeavourhealth.core.database.dal.audit.models.QueuedMessageType;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.dal.subscriberTransform.ExchangeBatchExtraResourceDalI;
import org.endeavourhealth.core.fhirStorage.FhirResourceHelper;
import org.endeavourhealth.core.fhirStorage.JsonServiceInterfaceEndpoint;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.messaging.pipeline.SubscriberBatch;
import org.endeavourhealth.core.messaging.pipeline.TransformBatch;
import org.endeavourhealth.core.xml.QueryDocument.ServiceContract;
import org.endeavourhealth.core.xml.QueryDocument.TechnicalInterface;
import org.endeavourhealth.core.xml.TransformErrorSerializer;
import org.endeavourhealth.core.xml.TransformErrorUtility;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.common.MessageFormat;
import org.endeavourhealth.transform.enterprise.FhirToEnterpriseCsvTransformer;
import org.endeavourhealth.transform.pcr.FhirToPcrCsvTransformer;
import org.endeavourhealth.transform.vitrucare.FhirToVitruCareXmlTransformer;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;
import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class MessageTransformOutbound extends PipelineComponent {
	private static final Logger LOG = LoggerFactory.getLogger(MessageTransformOutbound.class);

	private static final ExchangeDalI auditRepository = DalProvider.factoryExchangeDal();
	private static final ResourceDalI resourceRepository = DalProvider.factoryResourceDal();
	private static Map<String, Date> patientDOBMap = new HashMap<>();
	private static Map<String, String> cachedEndpoints = new ConcurrentHashMap<>();

	private MessageTransformOutboundConfig config;

	public MessageTransformOutbound(MessageTransformOutboundConfig config) {
		this.config = config;
	}


	@Override
	public void process(Exchange exchange) throws PipelineException {
		// Get the transformation data from the exchange
		// List of resources and subscriber service contracts
		TransformBatch transformBatch = getTransformBatch(exchange);
		UUID exchangeId = exchange.getId();
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

			String software = technicalInterface.getMessageFormat();
			String softwareVersion = technicalInterface.getMessageFormatVersion();
			Date transformStarted = new Date();

			Integer resourceCount = null;
			String outboundData = null;
			try {
				UUID serviceId = exchange.getHeaderAsUuid(HeaderKeys.SenderServiceUuid);

				//retrieve our resources. Do this for each protocol, rather than once, so there's no risk of
				//losing resources if a transform removes elements from the list
				List<ResourceWrapper> filteredResources = getResources(serviceId, exchangeId, batchId, resourceIds, endpoint);
				resourceCount = new Integer(filteredResources.size());

				//if we have resources, then perform the transform
				if (!filteredResources.isEmpty()) {
					outboundData = transform(serviceId, exchange, batchId, software, softwareVersion, filteredResources, endpoint, protocolId);
				}

				//if we've got data to send to our subscriber, then store it
				UUID queuedMessageId = null;
				if (!Strings.isNullOrEmpty(outboundData)) {

					// Store transformed message
					queuedMessageId = UUID.randomUUID();

					try {
						QueuedMessageDalI queuedMessageDal = DalProvider.factoryQueuedMessageDal();
						queuedMessageDal.save(queuedMessageId, outboundData, QueuedMessageType.OutboundData);
					} catch (Exception ex) {
						throw new PipelineException("Failed to save queued message", ex);
					}

					SubscriberBatch subscriberBatch = new SubscriberBatch();
					subscriberBatch.setQueuedMessageId(queuedMessageId);
					subscriberBatch.setEndpoint(endpoint);
					subscriberBatch.setSoftware(software);
					subscriberBatch.setSoftwareVersion(softwareVersion);
					subscriberBatch.setTechnicalInterfaceId(UUID.fromString(technicalInterfaceUuidStr));

					subscriberBatches.add(subscriberBatch);
				}

				//audit the transformation
				saveTransformAudit(exchangeId, batchId, endpoint, transformStarted, null, resourceCount, queuedMessageId);

			} catch (Exception ex) {

				//audit the exception
				try {
					saveTransformAudit(exchangeId, batchId, endpoint, transformStarted, ex, resourceCount, null);
				} catch (Exception auditEx) {
					LOG.error("Failed to save audit of transform failure", auditEx);
				}

				throw new PipelineException("Failed to transform exchange " + exchange.getId() + " and batch " + batchId, ex);
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

	private String transform(UUID serviceId,
							 Exchange exchange,
							 UUID batchId,
							 String software,
							 String softwareVersion,
							 List<ResourceWrapper> filteredResources,
							 String endpoint,
							 UUID protocolId) throws Exception {

		UUID exchangeId = exchange.getId();

		if (software.equals(MessageFormat.ENTERPRISE_CSV)) {

			UUID systemId = exchange.getHeaderAsUuid(HeaderKeys.SenderSystemUuid);

			//have to pass in the exchange body now
			String body = exchange.getBody();
			return FhirToEnterpriseCsvTransformer.transformFromFhir(serviceId, systemId, exchangeId, batchId, filteredResources, endpoint, protocolId, body);

		} else if (software.equals(MessageFormat.PCR_CSV)) {

			UUID systemId = exchange.getHeaderAsUuid(HeaderKeys.SenderSystemUuid);
			String body = exchange.getBody();

			return FhirToPcrCsvTransformer.transformFromFhir(serviceId, systemId, exchangeId, batchId, filteredResources, endpoint, protocolId, body);

		} else if (software.equals(MessageFormat.VITRUICARE_XML)) {
			return FhirToVitruCareXmlTransformer.transformFromFhir(serviceId, batchId, filteredResources, endpoint);

		} else if (software.equals(MessageFormat.JSON_API)) {
			//this is a pull-request message format, so there's no outbound transformation required
			return null;

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

			String cacheKey = serviceId.toString() + ":" + technicalInterfaceId.toString();
			String endpoint = cachedEndpoints.get(cacheKey);
			if (endpoint == null) {

				ServiceDalI serviceRepository = DalProvider.factoryServiceDal();

				Service service = serviceRepository.getById(serviceId);
				List<JsonServiceInterfaceEndpoint> serviceEndpoints = ObjectMapperPool.getInstance().readValue(service.getEndpoints(), new TypeReference<List<JsonServiceInterfaceEndpoint>>() {});
				for (JsonServiceInterfaceEndpoint serviceEndpoint: serviceEndpoints) {
					if (serviceEndpoint.getTechnicalInterfaceUuid().equals(technicalInterfaceId)) {
						endpoint = serviceEndpoint.getEndpoint();

						//concurrent map can't store null values, so only add to the cache if non-null
						if (endpoint != null) {
							cachedEndpoints.put(cacheKey, endpoint);
						}
						break;
					}
				}
			}

			return endpoint;

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


	private static List<ResourceWrapper> getResources(UUID serviceId, UUID exchangeId, UUID batchId, Map<ResourceType, List<UUID>> resourceIds, String subscriberConfigName) throws Exception {

		//get the resources actually in the batch we're transforming
		ResourceDalI resourceDal = DalProvider.factoryResourceDal();

		//changed to use a fn that returns the most recent version of the resources we're interested in, rather than
		//whatever they were like when we did our transform. Means we don't need to worry about sending older
		//versions of our resources to subscribers.
		//List<ResourceWrapper> resources = resourceDal.getResourcesForBatch(serviceId, batchId);
		List<ResourceWrapper> resources = resourceDal.getCurrentVersionOfResourcesForBatch(serviceId, batchId);

		//if there are no resources, then there won't be any extra resources, so no point wasting time
		//on looking for them or doing any filtering
		if (resources.isEmpty()) {
			return resources;
		}

		//then add in any EXTRA resources we've previously calculated we'll need to include
		ExchangeBatchExtraResourceDalI exchangeBatchExtraResourceDalI = DalProvider.factoryExchangeBatchExtraResourceDal(subscriberConfigName);
		Map<ResourceType, List<UUID>> extraReourcesByType = exchangeBatchExtraResourceDalI.findExtraResources(exchangeId, batchId);
		for (ResourceType resourceType: extraReourcesByType.keySet()) {
			List<UUID> extraIds = extraReourcesByType.get(resourceType);
			for (UUID extraId: extraIds) {
				ResourceWrapper extraResource = resourceDal.getCurrentVersion(serviceId, resourceType.toString(), extraId);

				//if the resource is null then it means the resource has been deleted and our subscriber transform
				//is running behind. So we need to find the a non-deleted instance of the resource and send that over,
				//so everything is valid in this batch. The delete for the resource will then be sent later, when
				//we process the exchange batch containing its delete
				if (extraResource == null) {
					List<ResourceWrapper> history = resourceDal.getResourceHistory(serviceId, resourceType.toString(), exchangeId);

					//most recent is first, so go backwards
					for (int i=history.size()-1; i>=0; i--) {
						ResourceWrapper historyItem = history.get(i);
						if (!historyItem.isDeleted()) {
							extraResource = historyItem;
							break;
						}
					}

					//if the resource is STILL null, then we've never received a non-deleted instance of it, so just skip sending it to the subscriber
					if (extraResource == null) {
						continue;
					}
				}

				resources.add(extraResource);
			}
		}

		//then filter to remove duplicates and ones the data set has filtered out
		resources = filterResources(resources, resourceIds);
		//resources = pruneOlderDuplicates(resources);

		//finally, filter out any patient resources that do not meet the filterElements configuration if
		//it is present for the subscriber config.  Used to filter FHIR -> subscriber output
		resources = filterPatientResources(serviceId, resources, subscriberConfigName);

		return resources;
	}

	/**
	 * we can end up with multiple instances of the same resource in a batch (or at least the Emis test data can)
	 * so strip out all but the latest version of each resource, so we're not wasting time sending over
	 * data that will immediately be overwritten and also we don't need to make sure to process them in order
	 *
	 * no longer required, as the new getCurrentVersionOfResourcesForBatch(..) already filters out duplicates
	 */
	/*private static List<ResourceWrapper> pruneOlderDuplicates(List<ResourceWrapper> resources) {

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
	}*/

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

	private static List<ResourceWrapper> filterPatientResources(UUID serviceId, List<ResourceWrapper> allResources, String subscriberConfigName) throws Exception {

		//check config for filterElements, if null, return straight back out as no further filtering needed
		JsonNode subscriberConfigJSON
				= ConfigManager.getConfigurationAsJson(subscriberConfigName, "db_subscriber");
		JsonNode filterElementsNodeJSON = subscriberConfigJSON.get("filterElements");

		//there is no resource filtering applied, so return all resources
		if (filterElementsNodeJSON == null) {
			return allResources;
		}

		List<ResourceWrapper> ret = new ArrayList<>();
		for (ResourceWrapper resource: allResources) {

			// perform filtering on patient resources.  Non patient resources are always included
			if (isPatientResource(resource) && !includeResource(serviceId, resource, filterElementsNodeJSON))
			{ continue; }

			//resource is either non patient or passes patient filter criteria so add
			ret.add(resource);
		}

		return ret;
	}

	// FHIR resources are filtered based on the subscriber configuration "filterElements" JSON
	// Only resources which match any of the resources AND any of the age ranges will be included,
	// i.e. A Patient resource who is 44.  An Immunization resource whose patient reference resource is 12.
	//
	//  JSON filter structure:
	//	"filterElements": {
    //		"patients": {
	//			"ageRangeYears": ["0-19", "40-74"]
	//		},
	//		"resources": ["Patient", "Observation", "Immunization", "Condition", "MedicationStatement", "MedicationOrder", "AllergyIntolerance"]
	//	}
	private static boolean includeResource(UUID serviceId, ResourceWrapper resource, JsonNode filterElementsNodeJSON) throws Exception {

		ResourceType resourceType = ResourceType.valueOf(resource.getResourceType());

		//is there a FHIR resource filter?
		boolean resourceInclude = false;
		JsonNode filterElementsResourcesNodeJSON = filterElementsNodeJSON.get("resources");
		if (filterElementsResourcesNodeJSON != null) {

			if (filterElementsResourcesNodeJSON.isArray()) {
				for (final JsonNode resourceNode : filterElementsResourcesNodeJSON) {

					ResourceType resourceTypeFilter = ResourceType.valueOf(resourceNode.asText());
					if (resourceTypeFilter != null) {

						//the type of resource matches the filter so it's an inclusion at this point of the filtering
						if (resourceType == resourceTypeFilter) {
							resourceInclude = true;
							break;
						}
					}
				}
			}
			//resource failed to match a filter, so return false here as there is no point doing further filter checks
			if (!resourceInclude)
				return false;
		}

		//is there a patient filter?
		JsonNode filterElementsPatientNodeJSON = filterElementsNodeJSON.get("patients");
		if (filterElementsPatientNodeJSON != null) {

			JsonNode filterElementsPatientAgeRangeYearNodeJSON = filterElementsPatientNodeJSON.get("ageRangeYears");
			if (filterElementsPatientAgeRangeYearNodeJSON != null) {

				//check if actual Patient resource
				if (resourceType == ResourceType.Patient) {

					//this is an actual Patient resource, so validate the resource against the filter
					Patient fhirPatient = (Patient) FhirResourceHelper.deserialiseResouce(resource);
					Date dateOfBirth = fhirPatient.getBirthDate();
					if (dateOfBirth != null) {

						//cache the patient dob for further lookup
						UUID patientId = resource.getPatientId();
						patientDOBMap.put(patientId.toString(), dateOfBirth);

						//check to see if patient falls within age range filter
						resourceInclude = isDOBInAgeRange(filterElementsPatientAgeRangeYearNodeJSON, dateOfBirth);

					} else {
						return false;  //null DOB, so return false to not include the resource
					}
				} else {

					//check patient DOB cache first, else lookup Patient resource from the DB
					UUID patientId = resource.getPatientId();
					Date dateOfBirth = patientDOBMap.get(patientId.toString());
					if (dateOfBirth == null) {

						//this is not a Patient resource so we need to get the Patient DOB from the DB
						//for the resource and validate against that.  The patient_id equals the resource_id for Patient resources.
						UUID resourceId = resource.getResourceId();
						Patient fhirPatient
								= (Patient) resourceRepository.getCurrentVersionAsResource(serviceId, ResourceType.Patient, patientId.toString());
						if (fhirPatient == null) {

							LOG.error("Patient resource not found for resourceId="+resourceId.toString()+" , processing patientId="+patientId);
							return false;
						}

						dateOfBirth = fhirPatient.getBirthDate();

						//null DOB, so return false to not include the resource
						if (dateOfBirth == null)
							return false;

						//cache patient dob for future use
						patientDOBMap.put(patientId.toString(), dateOfBirth);
					}

					resourceInclude = isDOBInAgeRange(filterElementsPatientAgeRangeYearNodeJSON, dateOfBirth);
				}
			}
		}

		return resourceInclude;
	}

	private static boolean isDOBInAgeRange(JsonNode filterElementsPatientAgeRangeYearNodeJSON, Date dateOfBirth) {

		Years age = Years.yearsBetween(new LocalDate(dateOfBirth), new LocalDate());
		int yearsOld = age.getYears();
		if (filterElementsPatientAgeRangeYearNodeJSON.isArray()) {
			for (final JsonNode ageRangeNode : filterElementsPatientAgeRangeYearNodeJSON) {
				String rangeYears = ageRangeNode.asText();  //from-to
				String ageFrom = rangeYears.substring(0, rangeYears.indexOf("-"));
				String ageTo = rangeYears.substring(rangeYears.indexOf("-") + 1, rangeYears.length());

				if (yearsOld >= Integer.parseInt(ageFrom) && yearsOld <= Integer.parseInt(ageTo)) {

					//patient matches age range filter so include the resource
					return true;
				}
			}
		}

		return false;
	}

	private static boolean isPatientResource(ResourceWrapper resource) {

		return (resource.getPatientId() != null);
	}


	private static void saveTransformAudit(UUID exchangeId, UUID batchId, String subscriberConfigName, Date started,
										   Exception transformError, Integer resourceCount, UUID queuedMessageId) throws Exception {

		ExchangeSubscriberTransformAudit audit = new ExchangeSubscriberTransformAudit();
		audit.setExchangeId(exchangeId);
		audit.setExchangeBatchId(batchId);
		audit.setSubscriberConfigName(subscriberConfigName);
		audit.setStarted(started);
		audit.setEnded(new Date());
		audit.setQueuedMessageId(queuedMessageId);
		audit.setNumberResourcesTransformed(resourceCount);

		if (transformError != null) {
			TransformError errorWrapper = new TransformError();
			TransformErrorUtility.addTransformError(errorWrapper, transformError, new HashMap<>());

			String xml = TransformErrorSerializer.writeToXml(errorWrapper);
			audit.setErrorXml(xml);
		}

		auditRepository.save(audit);
	}
}
