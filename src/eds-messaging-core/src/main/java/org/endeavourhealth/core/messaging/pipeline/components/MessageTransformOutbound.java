package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.configuration.MessageTransformOutboundConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.admin.LibraryRepositoryHelper;
import org.endeavourhealth.core.database.dal.admin.ServiceDalI;
import org.endeavourhealth.core.database.dal.admin.models.Service;
import org.endeavourhealth.core.database.dal.audit.ExchangeBatchDalI;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.QueuedMessageDalI;
import org.endeavourhealth.core.database.dal.audit.models.*;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.dal.subscriberTransform.ExchangeBatchExtraResourceDalI;
import org.endeavourhealth.core.fhirStorage.FhirResourceHelper;
import org.endeavourhealth.core.fhirStorage.ServiceInterfaceEndpoint;
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
import org.endeavourhealth.transform.subscriber.FhirToSubscriberCsvTransformer;
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

        UUID exchangeId = exchange.getId();

        TransformBatch transformBatch = getTransformBatch(exchange);
        UUID batchId = transformBatch.getBatchId();
        UUID protocolId = transformBatch.getProtocolId();

        // Run the transform, creating a subscriber batch for each
        // (Holds transformed message id and destination endpoints)
        List<SubscriberBatch> subscriberBatches = new ArrayList<>();

        List<ResourceWrapper> filteredResources = null; //don't retrieve until we know we need to
        Integer resourceCount = null;

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

            try {
                UUID serviceId = exchange.getHeaderAsUuid(HeaderKeys.SenderServiceUuid);

                //retrieve our resources if necessary
                if (filteredResources == null) {
                    filteredResources = getResources(exchange, batchId, endpoint);
                }

                //add any resources we've previously calculated need adding
                //this must be done FOR EACH subscriber, as extra resources may be added by the first transform
                //and we need to pick them up for the subsequent ones
                addExtraResources(filteredResources, exchange, batchId, endpoint);
                resourceCount = new Integer(filteredResources.size());

                //if we have resources, then perform the transform
                String outboundData = null;
                if (!filteredResources.isEmpty()) {

                    //pass in a copy of the resource list to avoid any potential problems if a transform changes the list
                    List<ResourceWrapper> copy = new ArrayList<>(filteredResources);

                    //do the outbound transform
                    outboundData = transform(serviceId, exchange, batchId, software, softwareVersion, copy, endpoint, protocolId);
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

                    //LOG.trace("Written base64 length " + outboundData.length() + " for " + endpoint + " to queued message " + queuedMessageId);
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

                String msg = "Failed to transform exchange " + exchange.getId() + " and batch " + batchId + " for " + exchange.getHeader(HeaderKeys.SenderLocalIdentifier);
                throw new PipelineException(msg, ex);
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
            return FhirToEnterpriseCsvTransformer.transformFromFhir(serviceId, systemId, exchangeId, batchId, filteredResources, endpoint);

        } else if (software.equals(MessageFormat.PCR_CSV)) {

            UUID systemId = exchange.getHeaderAsUuid(HeaderKeys.SenderSystemUuid);
            String body = exchange.getBody();

            return FhirToPcrCsvTransformer.transformFromFhir(serviceId, systemId, exchangeId, batchId, filteredResources, endpoint, protocolId, body);

        } else if (software.equals(MessageFormat.SUBSCRIBER_CSV)) {

            UUID systemId = exchange.getHeaderAsUuid(HeaderKeys.SenderSystemUuid);
            return FhirToSubscriberCsvTransformer.transformFromFhir(serviceId, systemId, exchangeId, batchId, filteredResources, endpoint);

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

    public static String getSubscriberEndpoint(ServiceContract contract) throws PipelineException {

        try {
            UUID serviceId = UUID.fromString(contract.getService().getUuid());
            UUID technicalInterfaceId = UUID.fromString(contract.getTechnicalInterface().getUuid());

            String cacheKey = serviceId.toString() + ":" + technicalInterfaceId.toString();
            String endpoint = cachedEndpoints.get(cacheKey);
            if (endpoint == null) {

                ServiceDalI serviceRepository = DalProvider.factoryServiceDal();

                Service service = serviceRepository.getById(serviceId);
                List<ServiceInterfaceEndpoint> serviceEndpoints = service.getEndpointsList();
                for (ServiceInterfaceEndpoint serviceEndpoint : serviceEndpoints) {
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


    private TransformBatch getTransformBatch(Exchange exchange) throws PipelineException {
        String transformBatchJson = exchange.getHeader(HeaderKeys.TransformBatch);
        try {
            return ObjectMapperPool.getInstance().readValue(transformBatchJson, TransformBatch.class);
        } catch (IOException e) {
            throw new PipelineException("Error deserializing transformation batch JSON", e);
        }
    }


    private static List<ResourceWrapper> getResources(Exchange exchange, UUID batchId, String subscriberConfigName) throws Exception {

        //get the resources actually in the batch we're transforming
        List<ResourceWrapper> resources = getResourcesInBatch(exchange, batchId, subscriberConfigName);

        //if there are no resources, then there won't be any extra resources, so no point wasting time
        //on looking for them or doing any filtering
        if (resources.isEmpty()) {
            return resources;
        }

        //filter out any patient resources that do not meet the filterElements configuration if
        //it is present for the subscriber config.  Used to filter FHIR -> subscriber output
        resources = filterPatientResources(exchange.getServiceId(), resources, subscriberConfigName);

        return resources;
    }

    private static void addExtraResources(List<ResourceWrapper> resources, Exchange exchange, UUID batchId, String subscriberConfigName) throws Exception {

        //hash our current resources by type and ID so we know what we've already got
        //this is required for cases where we have multiple subscribers using the same subscriber_transform DB
        //as the transform for the first subscriber may detect that extra resources are required, and we need to call
        //this fn again before invoking the second transform
        Map<ResourceType, Set<UUID>> hmResourcesGot = new HashMap<>();
        for (ResourceWrapper w: resources) {
            ResourceType type = w.getResourceTypeObj();
            UUID id = w.getResourceId();

            Set<UUID> inner = hmResourcesGot.get(type);
            if (inner == null) {
                inner = new HashSet<>();
                hmResourcesGot.put(type, inner);
            }
            inner.add(id);
        }

        ResourceDalI resourceDal = DalProvider.factoryResourceDal();
        ExchangeBatchExtraResourceDalI exchangeBatchExtraResourceDalI = DalProvider.factoryExchangeBatchExtraResourceDal(subscriberConfigName);

        UUID exchangeId = exchange.getId();
        UUID serviceId = exchange.getServiceId();
        //int sizeBefore = resources.size();

        Map<ResourceType, List<UUID>> extraResourcesByType = exchangeBatchExtraResourceDalI.findExtraResources(exchangeId, batchId);
        for (ResourceType resourceType : extraResourcesByType.keySet()) {

            List<UUID> extraIdsRequired = extraResourcesByType.get(resourceType);
            Set<UUID> idsInMemory = hmResourcesGot.get(resourceType);

            for (UUID extraId : extraIdsRequired) {

                //see if it's already in memory and skip if already present
                if (idsInMemory != null
                        && idsInMemory.contains(extraId)) {
                    continue;
                }

                ResourceWrapper extraResource = resourceDal.getCurrentVersion(serviceId, resourceType.toString(), extraId);

                //if the resource is null then it means the resource has been deleted and our subscriber transform
                //is running behind. So we need to find the a non-deleted instance of the resource and send that over,
                //so everything is valid in this batch. The delete for the resource will then be sent later, when
                //we process the exchange batch containing its delete
                if (extraResource == null) {
                    List<ResourceWrapper> history = resourceDal.getResourceHistory(serviceId, resourceType.toString(), exchangeId);

                    //most recent is first, so go backwards
                    for (int i = history.size() - 1; i >= 0; i--) {
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

        //int sizeAfter = resources.size();
        //LOG.trace("Added " + (sizeAfter - sizeBefore) + " extra resources");
    }

    private static List<ResourceWrapper> getResourcesInBatch(Exchange exchange, UUID batchId, String subscriberConfigName) throws Exception {

        //get the resources actually in the batch we're transforming
        ResourceDalI resourceDal = DalProvider.factoryResourceDal();
        UUID serviceId = exchange.getServiceId();

        List<ResourceWrapper> resources = null;

        //we use a special exchange with a specific header key to bulk populate subscribers
        String sourceSystem = exchange.getHeader(HeaderKeys.SourceSystem);
        if (sourceSystem.equals(MessageFormat.DUMMY_SENDER_SOFTWARE_FOR_BULK_TRANSFORM)) {

            ExchangeBatchDalI exchangeBatchDal = DalProvider.factoryExchangeBatchDal();
            ExchangeBatch exchangeBatch = exchangeBatchDal.getForExchangeAndBatchId(exchange.getId(), batchId);
            UUID patientId = exchangeBatch.getEdsPatientId();
            resources = resourceDal.getResourcesByPatient(serviceId, patientId); //passing in a null patient ID will get us the admin resources
            LOG.debug("Transforming all " + resources.size() + " resources for patient " + patientId + " to populate " + subscriberConfigName);

        } else {

            //if not a special bulk load, then just get the CURRENT VERSION of each resource in our exchange batch
            resources = resourceDal.getCurrentVersionOfResourcesForBatch(serviceId, batchId);
            //LOG.trace("Found " + resources.size() + " resources in batch " + batchId);
        }

        return resources;
    }


    private static List<ResourceWrapper> filterPatientResources(UUID serviceId, List<ResourceWrapper> allResources, String subscriberConfigName) throws Exception {

        //check config for filterElements, if null, return straight back out as no further filtering needed
        JsonNode subscriberConfigJSON = ConfigManager.getConfigurationAsJson(subscriberConfigName, "db_subscriber");
        JsonNode filterElementsNodeJSON = subscriberConfigJSON.get("filterElements");

        //there is no resource filtering applied, so return all resources
        if (filterElementsNodeJSON == null) {
            return allResources;
        }
        List<ResourceWrapper> ret = new ArrayList<>();
        for (ResourceWrapper resource : allResources) {

            //if (resource.getResourceType().equalsIgnoreCase("Patient")) {
            //    LOG.info("Patient " + resource.getResourceId() + ", inclusion :" + includeResource(serviceId, resource, filterElementsNodeJSON));
            //}
            // perform filtering on the resource to see if it is included in the transform.
            if (includeResource(serviceId, resource, filterElementsNodeJSON)) {

                ret.add(resource);
            }
        }

        return ret;
    }

    // FHIR resources are filtered based on the subscriber configuration "filterElements" JSON
    // Only resources which match any of the resources AND any of the age ranges will be included,
    // i.e. A Patient resource who is 44.  An Immunization resource whose patient reference resource is 12. An Organization
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

                            LOG.error("Patient resource not found for resourceId=" + resourceId.toString() + " , processing patientId=" + patientId);
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
