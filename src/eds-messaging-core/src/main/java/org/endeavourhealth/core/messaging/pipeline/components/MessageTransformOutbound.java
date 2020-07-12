package org.endeavourhealth.core.messaging.pipeline.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.endeavourhealth.common.cache.ObjectMapperPool;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.configuration.MessageTransformOutboundConfig;
import org.endeavourhealth.core.database.dal.DalProvider;
import org.endeavourhealth.core.database.dal.audit.ExchangeBatchDalI;
import org.endeavourhealth.core.database.dal.audit.ExchangeDalI;
import org.endeavourhealth.core.database.dal.audit.QueuedMessageDalI;
import org.endeavourhealth.core.database.dal.audit.models.*;
import org.endeavourhealth.core.database.dal.ehr.ResourceDalI;
import org.endeavourhealth.core.database.dal.ehr.models.ResourceWrapper;
import org.endeavourhealth.core.database.dal.subscriberTransform.ExchangeBatchExtraResourceDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.SubscriberResourceMappingDalI;
import org.endeavourhealth.core.database.dal.subscriberTransform.models.SubscriberId;
import org.endeavourhealth.core.database.rdbms.ehr.models.AdminResourceRetrieverI;
import org.endeavourhealth.core.fhirStorage.FhirResourceHelper;
import org.endeavourhealth.core.messaging.pipeline.PipelineComponent;
import org.endeavourhealth.core.messaging.pipeline.PipelineException;
import org.endeavourhealth.core.messaging.pipeline.SubscriberBatch;
import org.endeavourhealth.core.messaging.pipeline.TransformBatch;
import org.endeavourhealth.core.xml.TransformErrorSerializer;
import org.endeavourhealth.core.xml.TransformErrorUtility;
import org.endeavourhealth.core.xml.transformError.TransformError;
import org.endeavourhealth.transform.enterprise.FhirToEnterpriseCsvTransformer;
import org.endeavourhealth.transform.subscriber.FhirToSubscriberCsvTransformer;
import org.endeavourhealth.transform.subscriber.SubscriberConfig;
import org.endeavourhealth.transform.subscriber.targetTables.SubscriberTableId;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;
import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;


public class MessageTransformOutbound extends PipelineComponent {
    private static final Logger LOG = LoggerFactory.getLogger(MessageTransformOutbound.class);

    private static final ExchangeDalI auditRepository = DalProvider.factoryExchangeDal();
    private static final ResourceDalI resourceRepository = DalProvider.factoryResourceDal();
    private static Map<String, Date> patientDOBMap = new HashMap<>();

    private MessageTransformOutboundConfig config;

    public MessageTransformOutbound(MessageTransformOutboundConfig config) {
        this.config = config;
    }


    @Override
    public void process(Exchange exchange) throws PipelineException {

        UUID exchangeId = exchange.getId();
        List<TransformBatch> transformBatches = getTransformBatches(exchange);
        if (transformBatches.isEmpty()) {
            //if there's nothing to send to any subscribers, then we'll still end up in the Transform queue
            //but with an empty list of batches. So just drop out of the transform.
            setSubscriberBatchesOnExchange(exchange, new ArrayList<>());
            return;
        }

        UUID batchId = getBatchId(transformBatches);
        UUID patientId = findPatientId(exchangeId, batchId);
        UUID serviceId = exchange.getServiceId();
        ResourceCache resourceCache = new ResourceCache(serviceId, exchangeId, batchId, patientId, transformBatches);

        //the object of the below is to populate this list of subscriber batches
        List<SubscriberBatch> subscriberBatches = new ArrayList<>();

        for (TransformBatch batch: transformBatches) {
            String subscriberConfigName = batch.getSubscriberConfigName();

            Date transformStarted = new Date();
            TransformBatch.TransformAction action = batch.getAction();
            List<ResourceWrapper> resourceWrappers = resourceCache.getResources(action, subscriberConfigName);

            try {

                //do the outbound transform
                String outboundData = performTransform(exchange, batchId, resourceWrappers, subscriberConfigName, action);

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
                    subscriberBatch.setEndpoint(subscriberConfigName);
                    subscriberBatches.add(subscriberBatch);
                }

                //audit the transformation
                saveTransformAudit(exchange.getId(), batchId, subscriberConfigName, transformStarted, null, resourceWrappers.size(), queuedMessageId);

            } catch (Exception ex) {

                //audit the exception
                try {
                    saveTransformAudit(exchange.getId(), batchId, subscriberConfigName, transformStarted, ex, resourceWrappers.size(), null);
                } catch (Exception auditEx) {
                    LOG.error("Failed to save audit of transform failure", auditEx);
                }

                String msg = "Failed to transform exchange " + exchange.getId() + " and batch " + batchId + " for " + exchange.getHeader(HeaderKeys.SenderLocalIdentifier);
                throw new PipelineException(msg, ex);
            }
        }

        setSubscriberBatchesOnExchange(exchange, subscriberBatches);
    }

    private static void setSubscriberBatchesOnExchange(Exchange exchange, List<SubscriberBatch> subscriberBatches) throws PipelineException {
        String subscriberBatchesJson = writeSubscriberBatchesToJson(subscriberBatches);
        exchange.setHeader(HeaderKeys.SubscriberBatch, subscriberBatchesJson);
    }


    /**
     * all transform batches should have the same batch ID
     */
    public static UUID getBatchId(List<TransformBatch> transformBatches) throws PipelineException {
        UUID ret = null;

        for (TransformBatch b: transformBatches) {
            if (ret == null) {
                ret = b.getBatchId();
            } else if (!ret.equals(b.getBatchId())) {
                throw new PipelineException("Transform Batches don't have the same batch ID " + ret + " vs " + b.getBatchId());
            }
        }

        return ret;
    }

    private static String writeSubscriberBatchesToJson(List<SubscriberBatch> subscriberBatches) throws PipelineException {

        try {
            return ObjectMapperPool.getInstance().writeValueAsString(subscriberBatches);

        } catch (JsonProcessingException e) {
            LOG.error("Error serializing subscriber batch JSON", e);
            throw new PipelineException("Error serializing subscriber batch JSON", e);
        }
    }


    private UUID findPatientId(UUID exchangeId, UUID batchId) throws PipelineException {

        try {
            ExchangeBatchDalI exchangeBatchDal = DalProvider.factoryExchangeBatchDal();
            ExchangeBatch exchangeBatch = exchangeBatchDal.getForExchangeAndBatchId(exchangeId, batchId);
            return exchangeBatch.getEdsPatientId();
        } catch (Exception ex) {
            throw new PipelineException("Failed to find patient UUID for batch " + batchId, ex);
        }
    }


    private String performTransform(Exchange exchange,
                                    UUID batchId,
                                    List<ResourceWrapper> filteredResources,
                                    String subscriberConfigName,
                                    TransformBatch.TransformAction action) throws Exception {

        UUID serviceId = exchange.getServiceId();
        UUID systemId = exchange.getSystemId();
        UUID exchangeId = exchange.getId();

        boolean isBulkDeleteFromSubscriber = action == TransformBatch.TransformAction.FULL_DELETE;
        SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(subscriberConfigName);

        if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV1) {
            return FhirToEnterpriseCsvTransformer.transformFromFhir(serviceId, systemId, exchangeId, batchId, filteredResources, subscriberConfig, isBulkDeleteFromSubscriber);

        } else if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV2) {
            return FhirToSubscriberCsvTransformer.transformFromFhir(serviceId, systemId, exchangeId, batchId, filteredResources, subscriberConfig, isBulkDeleteFromSubscriber);

        } else {
            throw new PipelineException("Unsupported outbound software " + subscriberConfig.getSubscriberType() + " for exchange " + exchange.getId());
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




    /**
     * returns the transform batches from the exchange header key
     */
    public static List<TransformBatch> getTransformBatches(Exchange exchange) throws PipelineException {
        String transformBatchJson = exchange.getHeader(HeaderKeys.TransformBatch);

        //depending on whether new way or old way, we may have a single batch or an array of them - handle both
        try {
            TransformBatch[] arr = ObjectMapperPool.getInstance().readValue(transformBatchJson, TransformBatch[].class);
            return Lists.newArrayList(arr);

        } catch (IOException e) {
            try {
                TransformBatch batch = ObjectMapperPool.getInstance().readValue(transformBatchJson, TransformBatch.class);
                List<TransformBatch> ret = new ArrayList<>();
                ret.add(batch);
                return ret;

            } catch (IOException ex) {
                throw new PipelineException("Error deserializing transformation batch JSON", ex);
            }
        }
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

    /**
     * object to manage and cache FHIR resources to make it more efficient to perform mutiple outbound transforms
     */
    static class ResourceCache {

        private UUID serviceId;
        private UUID exchangeId;
        private UUID batchId;
        private UUID patientId;
        private List<TransformBatch> transformBatches = null;

        private List<ResourceWrapper> deltaResourcesCache = null;
        private List<ResourceWrapper> allResourcesCache = null;

        public ResourceCache(UUID serviceId, UUID exchangeId, UUID batchId, UUID patientId, List<TransformBatch> transformBatches) {
            this.serviceId = serviceId;
            this.exchangeId = exchangeId;
            this.batchId = batchId;
            this.patientId = patientId;
            this.transformBatches = transformBatches;
        }

        public List<ResourceWrapper> getResources(TransformBatch.TransformAction action, String subscriberConfigName) throws PipelineException {
            try {
                return getResourcesImpl(action, subscriberConfigName);

            } catch (Exception ex) {
                throw new PipelineException("Failed to get resources for batch " + batchId + " and action " + action, ex);
            }
        }

        public List<ResourceWrapper> getResourcesImpl(TransformBatch.TransformAction action, String subscriberConfigName) throws Exception {

            List<ResourceWrapper> ret = null;

            if (action == TransformBatch.TransformAction.FULL_DELETE
                    || action == TransformBatch.TransformAction.FULL_LOAD) {

                //for full load of admin data we can't use a cache because the resources returned is dependent on the
                //subscriber config, so if there are different config names, the list of resources may differ
                if (patientId == null) {
                    if (allResourcesCache == null) {
                        allResourcesCache = retrieveAllAdminResources();
                    }
                    ret = new ArrayList<>(allResourcesCache); //copy the list so nothing done by the transform can change our cache
                    LOG.debug("" + action + " all " + ret.size() + " admin resources for subscriber " + subscriberConfigName);

                } else {
                    //for patient resources, just retrieve and cache the full list
                    if (allResourcesCache == null) {
                        allResourcesCache = retrieveAllPatientResources();
                    }
                    ret = new ArrayList<>(allResourcesCache); //copy the list so nothing done by the transform can change our cache
                    LOG.debug("" + action + " all " + ret.size() + " resources for patient " + patientId + " and subscriber " + subscriberConfigName);
                }

            } else {
                //for delta transactions
                if (deltaResourcesCache == null) {
                    ResourceDalI resourceDal = DalProvider.factoryResourceDal();
                    deltaResourcesCache = resourceDal.getCurrentVersionOfResourcesForBatch(serviceId, batchId);
                }
                ret = new ArrayList<>(deltaResourcesCache); //copy the list so nothing done by the transform can change our cache
            }

            //if there are no resources, then there won't be any extra resources, so no point wasting time
            //on looking for them or doing any filtering
            if (ret.isEmpty()) {
                return ret;
            }

            //filter out any patient resources that do not meet the filterElements configuration if
            //it is present for the subscriber config.  Used to filter FHIR -> subscriber output
            ret = filterPatientResources(serviceId, ret, subscriberConfigName);

            //add any resources we've previously calculated need adding
            //this must be done FOR EACH subscriber, as extra resources may be added by the first transform
            //and we need to pick them up for the subsequent ones
            addExtraResources(ret, serviceId, exchangeId, batchId, subscriberConfigName);

            //filter our any resources that have since been amended to point to other patients
            filterResourcesForOtherPatients(ret, patientId);

            return ret;
        }

        /**
         * retrieves all admin resources to send through the outbound transform
         *
         * since most services have millions of admin resources, this can't just retrieve all of the resources
         * and just send them through
         */
        private List<ResourceWrapper> retrieveAllAdminResources() throws Exception {

            LOG.debug("Retrieving all admin resources for " + serviceId + " to send to " + transformBatches.size() + " subscribers");
            List<ResourceWrapper> ret = new ArrayList<>();

            ResourceDalI resourceDal = DalProvider.factoryResourceDal();
            AdminResourceRetrieverI adminHelper = resourceDal.startRetrievingAdminResources(serviceId, 1000);

            List<SubscriberConfig> subscriberConfigs = new ArrayList<>();
            for (TransformBatch transformBatch: this.transformBatches) {
                String subscriberConfigName = transformBatch.getSubscriberConfigName();
                SubscriberConfig subscriberConfig = SubscriberConfig.readFromConfig(subscriberConfigName);
                subscriberConfigs.add(subscriberConfig);
            }

            int totalChecked = 0;
            int batchCount = 0;

            while (true) {
                List<ResourceWrapper> wrappers = adminHelper.getNextBatch();

                //if we've hit the end
                if (wrappers == null) {
                    break;
                }

                totalChecked += wrappers.size();
                batchCount ++;

                if (batchCount % 10 == 0) {
                    LOG.debug("Checked " + totalChecked + " admin resources and keeping " + ret.size());
                }

                //for each admin resource, we should work out if it has an ID in our subscriber transform DB
                //and only send for transforming if so. Admin resources are transformed only when needed by clinical data
                //so we only need to send ones that have previously been transformed (or have IDs anyway)
                for (SubscriberConfig subscriberConfig: subscriberConfigs) {

                    //we need to test if the resources are interesting to any of our subscribers
                    String subscriberConfigName = subscriberConfig.getSubscriberConfigName();
                    int batchSize = subscriberConfig.getBatchSize();

                    List<ResourceWrapper> toKeep = null;
                    if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV1) {
                        toKeep = findCompassV1ResourceIds(wrappers, subscriberConfigName, batchSize);

                    } else if (subscriberConfig.getSubscriberType() == SubscriberConfig.SubscriberType.CompassV2) {
                        toKeep = findCompassV2ResourceIds(wrappers, subscriberConfigName, batchSize);

                    } else {
                        throw new Exception("Unexpected subscriber type " + subscriberConfig.getSubscriberType() + " for " + subscriberConfigName);
                    }

                    ret.addAll(toKeep);

                    //remove wrappers we know we already want to do
                    Set<ResourceWrapper> hsToKeep = new HashSet<>(toKeep);
                    for (int i=wrappers.size()-1; i>=0; i--) {
                        ResourceWrapper wrapper = wrappers.get(i);
                        if (hsToKeep.contains(wrapper)) {
                            wrappers.remove(i);
                        }
                    }

                    if (wrappers.isEmpty()) {
                        break;
                    }
                }
            }

            LOG.debug("Finished checking all " + totalChecked + " admin resources for " + serviceId + " and found " + ret.size());
            return ret;
        }

        private List<ResourceWrapper> findCompassV2ResourceIds(List<ResourceWrapper> wrappers, String subscriberConfigName, int batchSize) throws Exception {

            List<ResourceWrapper> toKeep = new ArrayList<>();

            SubscriberResourceMappingDalI subscriberDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);
            //for compass v2, we need to hash the wrappers by resource type
            Map<ResourceType, List<ResourceWrapper>> hmByType = new HashMap<>();
            for (ResourceWrapper wrapper: wrappers) {
                ResourceType type = wrapper.getResourceTypeObj();
                List<ResourceWrapper> l = hmByType.get(type);
                if (l == null) {
                    l = new ArrayList<>();
                    hmByType.put(type, l);
                }
                l.add(wrapper);
            }

            for (ResourceType type: hmByType.keySet()) {
                List<ResourceWrapper> toCheck = hmByType.get(type);

                //to look up IDs, we need to know what target table the resources go into
                SubscriberTableId tableId = null;
                if (type == ResourceType.Organization) {
                    tableId = SubscriberTableId.ORGANIZATION;

                } else if (type == ResourceType.Location) {
                    tableId = SubscriberTableId.LOCATION;

                } else if (type == ResourceType.Practitioner) {
                    tableId = SubscriberTableId.PRACTITIONER;

                } else if (type == ResourceType.Schedule) {
                    tableId = SubscriberTableId.SCHEDULE;

                } else if (type == ResourceType.Slot) {
                    //slot resources aren't transformed by the Compass v2 transform
                    //so just discard them here
                    continue;

                } else {
                    throw new Exception("Unexpected admin resource type " + type);
                }

                List<String> batchStrs = new ArrayList<>();
                Map<String, ResourceWrapper> hmBatch = new HashMap<>();

                for (int i=0; i<toCheck.size(); i++) {
                    ResourceWrapper wrapper = toCheck.get(i);
                    String referenceStr = wrapper.getReferenceString();
                    batchStrs.add(referenceStr);
                    hmBatch.put(referenceStr, wrapper);

                    if (batchStrs.size() >= batchSize
                            || i+1 >= toCheck.size()) {

                        Map<String, SubscriberId> hmMappings = subscriberDal.findSubscriberIds(tableId.getId(), batchStrs);

                        //anything with an ID should be kept
                        for (String mappedReferenceStr: hmMappings.keySet()) {
                            ResourceWrapper mappedResource = hmBatch.get(mappedReferenceStr);
                            toKeep.add(mappedResource);
                        }

                        batchStrs = new ArrayList<>();
                        hmBatch = new HashMap<>();
                    }
                }
            }

            return toKeep;
        }

        private List<ResourceWrapper> findCompassV1ResourceIds(List<ResourceWrapper> wrappers, String subscriberConfigName, int batchSize) throws Exception {

            List<ResourceWrapper> toKeep = new ArrayList<>();

            SubscriberResourceMappingDalI subscriberDal = DalProvider.factorySubscriberResourceMappingDal(subscriberConfigName);

            //for compass v1, we need to hash the wrappers by resource type
            Map<ResourceType, List<ResourceWrapper>> hmByType = new HashMap<>();
            for (ResourceWrapper wrapper: wrappers) {
                ResourceType type = wrapper.getResourceTypeObj();
                List<ResourceWrapper> l = hmByType.get(type);
                if (l == null) {
                    l = new ArrayList<>();
                    hmByType.put(type, l);
                }
                l.add(wrapper);
            }

            for (ResourceType type: hmByType.keySet()) {
                List<ResourceWrapper> toCheck = hmByType.get(type);

                //slot resources aren't transformed in compass v1, so discard any here
                if (type == ResourceType.Slot) {
                    continue;
                }

                List<ResourceWrapper> batch = new ArrayList<>();
                for (int i = 0; i < toCheck.size(); i++) {
                    ResourceWrapper wrapper = toCheck.get(i);
                    batch.add(wrapper);

                    if (batch.size() >= batchSize
                            || i + 1 >= toCheck.size()) {

                        Map<ResourceWrapper, Long> mappedResources = new HashMap<>();
                        subscriberDal.findEnterpriseIdsOldWay(batch, mappedResources);

                        //anything with an ID should be kept
                        for (ResourceWrapper mappedResource : mappedResources.keySet()) {
                            toKeep.add(mappedResource);
                        }

                        batch = new ArrayList<>();
                    }
                }
            }

            return toKeep;
        }

        private List<ResourceWrapper> retrieveAllPatientResources() throws Exception {
            ResourceDalI resourceDal = DalProvider.factoryResourceDal();
            return resourceDal.getResourcesByPatient(serviceId, patientId); //passing in a null patient ID will get us the admin resources
        }


        private static void addExtraResources(List<ResourceWrapper> resources, UUID serviceId, UUID exchangeId,
                                              UUID batchId, String subscriberConfigName) throws Exception {

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
        }

        /**
         * resources can be moved from one patient to another, typically from the ADT feed, in which case we'll
         * have a batch for the receiving patient and possibly a batch for the old patient. If this happens,
         * then we want to make sure the batch for the old patient doesn't include any resource pointing to the new patient
         * since the FHIR->compass transforms expect all resources passed in to belong to the same patient.
         */
        private static void filterResourcesForOtherPatients(List<ResourceWrapper> resources, UUID patientId) throws Exception {

            for (int i=resources.size()-1; i>=0; i--) {
                ResourceWrapper w = resources.get(i);
                UUID wPatientId = w.getPatientId();

                //if an admin batch, we shouldn't have any patient resources at all
                if (patientId == null
                        && wPatientId != null) {
                    throw new Exception("Unexpected patient-related resource " + w.getResourceType() + " " + w.getResourceId() + " in admin batch");
                }

                //if a patient batch, we may have admin resources, but remove any resources for other patients
                if (patientId != null
                        && wPatientId != null
                        && !wPatientId.equals(patientId)) {
                    LOG.trace("Filtered out resource " + w.getResourceType() + " " + w.getResourceId() + " as belongs to patient " + w.getPatientId() + " not " + patientId);
                    resources.remove(i);
                }
            }
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

    }
}
