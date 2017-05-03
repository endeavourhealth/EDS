package org.endeavourhealth.transform.enterprise.transforms;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.endeavourhealth.common.cache.ParserPool;
import org.endeavourhealth.common.fhir.ReferenceComponents;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.core.data.ehr.ResourceNotFoundException;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.rdbms.transform.EnterpriseIdHelper;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.enterprise.FhirToEnterpriseCsvTransformer;
import org.endeavourhealth.transform.enterprise.outputModels.AbstractEnterpriseCsvWriter;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.hl7.fhir.instance.model.TemporalPrecisionEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTransformer.class);
    private static final ParserPool PARSER_POOL = new ParserPool();

    //private static final EnterpriseIdMapRepository idMappingRepository = new EnterpriseIdMapRepository();
    private static JCS cache = null;
    /*private static Map<String, AtomicInteger> maxIdMap = new ConcurrentHashMap<>();
    private static ReentrantLock futuresLock = new ReentrantLock();*/

    static {
        try {

            //by default the Java Caching System has a load of logging enabled, which is really slow, so turn it off
            //not longer required, since it no longer uses log4J and the new default doesn't have debug enabled
            /*org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("org.apache.jcs");
            logger.setLevel(org.apache.log4j.Level.OFF);*/

            cache = JCS.getInstance("EnterpriseResourceMap");

        } catch (CacheException ex) {
            throw new RuntimeException("Error initialising cache", ex);
        }
    }

    public void transform(List<ResourceByExchangeBatch> resources,
                          OutputContainer data,
                          AbstractEnterpriseCsvWriter csvWriter,
                          Map<String, ResourceByExchangeBatch> otherResources,
                          Long enterpriseOrganisationId,
                          Long enterprisePatientId,
                          Long enterprisePersonId,
                          String configName,
                          UUID protocolId) throws Exception {

        Map<ResourceByExchangeBatch, Long> enterpriseIds = mapIds(resources, csvWriter, shouldAlwaysTransform());

        for (ResourceByExchangeBatch resource: resources) {

            try {
                Long enterpriseId = enterpriseIds.get(resource);
                if (enterpriseId == null) {
                    continue;

                } else if (resource.getIsDeleted()) {
                    csvWriter.writeDelete(enterpriseId.longValue());

                } else {
                    Resource fhir = deserialiseResouce(resource);
                    transform(enterpriseId, fhir, data, csvWriter, otherResources, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName, protocolId);
                }
            } catch (Exception ex) {
                throw new TransformException("Exception transforming " + resource.getResourceType() + " " + resource.getResourceId(), ex);
            }
        }
    }

    public abstract boolean shouldAlwaysTransform();

    public abstract void transform(Long enterpriseId,
                                   Resource resource,
                                   OutputContainer data,
                                   AbstractEnterpriseCsvWriter csvWriter,
                                   Map<String, ResourceByExchangeBatch> otherResources,
                                   Long enterpriseOrganisationId,
                                   Long enterprisePatientId,
                                   Long enterprisePersonId,
                                   String configName,
                                   UUID protocolId) throws Exception;

    protected static Integer convertDatePrecision(TemporalPrecisionEnum precision) throws Exception {
        return Integer.valueOf(precision.getCalendarConstant());
    }



    protected static Long findEnterpriseId(AbstractEnterpriseCsvWriter csvWriter, Resource resource) throws Exception {
        String resourceType = resource.getResourceType().toString();
        String resourceId = resource.getId();
        return findEnterpriseId(csvWriter, resourceType, resourceId);
    }

    protected static Long findEnterpriseId(AbstractEnterpriseCsvWriter csvWriter, Reference reference) throws Exception {
        ReferenceComponents comps = ReferenceHelper.getReferenceComponents(reference);
        String resourceType = comps.getResourceType().toString();
        String resourceId = comps.getId();
        return findEnterpriseId(csvWriter, resourceType, resourceId);
    }

    protected static Long findEnterpriseId(AbstractEnterpriseCsvWriter csvWriter, ResourceByExchangeBatch resource) throws Exception {
        return findEnterpriseId(csvWriter, resource.getResourceType(), resource.getResourceId().toString());
    }

    public static Long findEnterpriseId(AbstractEnterpriseCsvWriter csvWriter, String resourceType, String resourceId) throws Exception {
        String enterpriseTableName = csvWriter.getFileNameWithoutExtension();
        Long ret = checkCacheForId(enterpriseTableName, resourceType, resourceId);
        if (ret == null) {
            ret = EnterpriseIdHelper.findEnterpriseId(enterpriseTableName, resourceType, resourceId);
            //ret = idMappingRepository.getEnterpriseIdMappingId(enterpriseTableName, resourceType, resourceId);
        }
        return ret;
    }

    protected static Long findOrCreateEnterpriseId(AbstractEnterpriseCsvWriter csvWriter, ResourceByExchangeBatch resource) throws Exception {
        String resourceType = resource.getResourceType();
        String resourceId = resource.getResourceId().toString();
        return findOrCreateEnterpriseId(csvWriter, resourceType, resourceId);
    }

    public static Long findOrCreateEnterpriseId(AbstractEnterpriseCsvWriter csvWriter, String resourceType, String resourceId) throws Exception {
        String enterpriseTableName = csvWriter.getFileNameWithoutExtension();
        Long ret = checkCacheForId(enterpriseTableName, resourceType, resourceId);
        if (ret == null) {
            ret = EnterpriseIdHelper.findOrCreateEnterpriseId(enterpriseTableName, resourceType, resourceId);
            /*int enterpriseId = getNextId(enterpriseTableName);
            idMappingRepository.saveEnterpriseIdMax(enterpriseTableName, Integer.valueOf(enterpriseId));
            idMappingRepository.saveEnterpriseIdMapping(enterpriseTableName, resourceType, resourceId, Integer.valueOf(enterpriseId));*/

            addIdToCache(enterpriseTableName, resourceType, resourceId, ret);
        }
        return ret;
    }

    private static String createCacheKey(String enterpriseTableName, String resourceType, String resourceId) {
        StringBuilder sb = new StringBuilder();
        sb.append(enterpriseTableName);
        sb.append(":");
        sb.append(resourceType);
        sb.append("/");
        sb.append(resourceId);
        return sb.toString();
    }

    private static Long checkCacheForId(String enterpriseTableName, String resourceType, String resourceId) throws Exception {
        return (Long)cache.get(createCacheKey(enterpriseTableName, resourceType, resourceId));
    }

    private static void addIdToCache(String enterpriseTableName, String resourceType, String resourceId, Long toCache) throws Exception {
        if (toCache == null) {
            return;
        }
        cache.put(createCacheKey(enterpriseTableName, resourceType, resourceId), toCache);
    }

    public static Resource deserialiseResouce(ResourceByExchangeBatch resourceByExchangeBatch) throws Exception {
        String json = resourceByExchangeBatch.getResourceData();
        return deserialiseResouce(json);
    }

    public static Resource deserialiseResouce(String json) throws Exception {
        try {
            return PARSER_POOL.parse(json);

        } catch (Exception ex) {
            LOG.error("Error deserialising resource", ex);
            LOG.error(json);
            throw ex;
        }

    }

    protected static Resource findResource(Reference reference,
                                           Map<String, ResourceByExchangeBatch> hmAllResources) throws Exception {
        String referenceStr = reference.getReference();

        //look in our resources map first
        ResourceByExchangeBatch ret = hmAllResources.get(referenceStr);
        if (ret != null) {
            if (ret.getIsDeleted()) {
                return null;
            } else {
                return deserialiseResouce(ret);
            }
        } else {

            //if not in our map, then hit the DB
            ReferenceComponents comps = ReferenceHelper.getReferenceComponents(reference);
            return new ResourceRepository().getCurrentVersionAsResource(comps.getResourceType(), comps.getId());
        }
    }

    protected static Long mapId(ResourceByExchangeBatch resource, AbstractEnterpriseCsvWriter csvWriter, boolean createIfNotFound) throws Exception {

        if (resource.getIsDeleted()) {
            //if it's a delete, then don't bother creating a new Enterprise ID if we've never previously sent it
            //to Enterprise, since there's no point just sending a delete
            return findEnterpriseId(csvWriter, resource);

        } else {

            if (createIfNotFound) {
                return findOrCreateEnterpriseId(csvWriter, resource);

            } else {
                return findEnterpriseId(csvWriter, resource);
            }
        }
    }

    protected static Map<ResourceByExchangeBatch, Long> mapIds(List<ResourceByExchangeBatch> resources, AbstractEnterpriseCsvWriter csvWriter, boolean createIfNotFound) throws Exception {

        Map<ResourceByExchangeBatch, Long> ids = new HashMap<>();

        String enterpriseTableName = csvWriter.getFileNameWithoutExtension();

        //first, try to find existing IDs for our resources in our memory cache
        findEnterpriseIdsInCache(enterpriseTableName, resources, ids);

        List<ResourceByExchangeBatch> resourcesToFindOnDb = new ArrayList<>();
        List<ResourceByExchangeBatch> resourcesToFindOrCreateOnDb = new ArrayList<>();

        for (ResourceByExchangeBatch resource: resources) {

            //if our memory cache contained this ID, then skip it
            if (ids.containsKey(resource)) {
                continue;
            }

            //if we didn't find an ID in memory, then we'll either want to simply find on the DB or find and create on the DB
            if (resource.getIsDeleted()
                    || !createIfNotFound) {
                resourcesToFindOnDb.add(resource);

            } else {
                resourcesToFindOrCreateOnDb.add(resource);
            }
        }

        //look up any resources we need
        if (!resourcesToFindOnDb.isEmpty()) {
            EnterpriseIdHelper.findEnterpriseIds(enterpriseTableName, resourcesToFindOnDb, ids);

            //add them to our cache
            for (ResourceByExchangeBatch resource: resourcesToFindOnDb) {
                Long enterpriseId = ids.get(resource);
                addIdToCache(enterpriseTableName, resource.getResourceType(), resource.getResourceId().toString(), enterpriseId);
            }
        }

        //lookup and create any resources we need
        if (!resourcesToFindOrCreateOnDb.isEmpty()) {
            EnterpriseIdHelper.findOrCreateEnterpriseIds(enterpriseTableName, resourcesToFindOrCreateOnDb, ids);

            //add them to our cache
            for (ResourceByExchangeBatch resource: resourcesToFindOrCreateOnDb) {
                Long enterpriseId = ids.get(resource);
                addIdToCache(enterpriseTableName, resource.getResourceType(), resource.getResourceId().toString(), enterpriseId);
            }
        }

        return ids;
    }

    private static void findEnterpriseIdsInCache(String enterpriseTableName, List<ResourceByExchangeBatch> resources, Map<ResourceByExchangeBatch, Long> ids) throws Exception {

        for (ResourceByExchangeBatch resource: resources) {
            Long cachedId = checkCacheForId(enterpriseTableName, resource.getResourceType(), resource.getResourceId().toString());
            if (cachedId != null) {
                ids.put(resource, cachedId);
            }
        }
    }




    protected Long transformOnDemand(Reference reference,
                                     OutputContainer data,
                                     Map<String, ResourceByExchangeBatch> hmAllResources,
                                     Long enterpriseOrganisationId,
                                     Long enterprisePatientId,
                                     Long enterprisePersonId,
                                     String configName,
                                     UUID protocolId) throws Exception {
        Resource fhir = null;
        try {
            fhir = findResource(reference, hmAllResources);
        } catch (ResourceNotFoundException ex) {
            //we have some data that refers to non-existant resources, so if we get that, log it
            LOG.warn("No resource found for reference " + reference.getReference());
        }

        if (fhir == null) {
            return null;
        }

        ResourceType resourceType = fhir.getResourceType();
        AbstractTransformer transformer = FhirToEnterpriseCsvTransformer.createTransformerForResourceType(resourceType);
        if (transformer == null) {
            throw new TransformException("No transformer found for resource " + reference.getReference());
        }

        AbstractEnterpriseCsvWriter csvWriter = FhirToEnterpriseCsvTransformer.findCsvWriterForResourceType(resourceType, data);
        Long enterpriseId = findOrCreateEnterpriseId(csvWriter, resourceType.toString(), fhir.getId());
        transformer.transform(enterpriseId, fhir, data, csvWriter, hmAllResources, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName, protocolId);

        return enterpriseId;
    }
}
