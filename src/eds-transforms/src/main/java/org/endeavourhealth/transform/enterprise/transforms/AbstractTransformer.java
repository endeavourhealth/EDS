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

import java.util.Map;

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

    public abstract void transform(ResourceByExchangeBatch resource,
                                   OutputContainer data,
                                   AbstractEnterpriseCsvWriter csvWriter,
                                   Map<String, ResourceByExchangeBatch> otherResources,
                                   Long enterpriseOrganisationId,
                                   Long enterprisePatientId,
                                   Long enterprisePersonId,
                                   String configName) throws Exception;

    public abstract void transform(Long enterpriseId,
                                   Resource resource,
                                   OutputContainer data,
                                   AbstractEnterpriseCsvWriter csvWriter,
                                   Map<String, ResourceByExchangeBatch> otherResources,
                                   Long enterpriseOrganisationId,
                                   Long enterprisePatientId,
                                   Long enterprisePersonId,
                                   String configName) throws Exception;

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

    private static Long checkCacheForId(String enterpriseTableName, String resourceType, String resourceId) throws Exception {
        return (Long)cache.get(enterpriseTableName + ":" + resourceType + "/" + resourceId);
    }
    private static void addIdToCache(String enterpriseTableName, String resourceType, String resourceId, Long toCache) throws Exception {
        cache.put(enterpriseTableName + ":" + resourceType + "/" + resourceId, toCache);
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


    /*private static int getNextId(String enterpriseTableName) {

        AtomicInteger ai = maxIdMap.get(enterpriseTableName);
        if (ai == null) {
            futuresLock.lock();

            try {
                ai = maxIdMap.get(enterpriseTableName);
                if (ai == null) {
                    int lastVal = idMappingRepository.getMaxEnterpriseId(enterpriseTableName);
                    ai = new AtomicInteger(lastVal);
                    maxIdMap.put(enterpriseTableName, ai);
                }
            } finally {
                futuresLock.unlock();
            }
        }
        return ai.incrementAndGet();
    }*/


    protected Long transformOnDemand(Reference reference,
                                     OutputContainer data,
                                     Map<String, ResourceByExchangeBatch> hmAllResources,
                                     Long enterpriseOrganisationId,
                                     Long enterprisePatientId,
                                     Long enterprisePersonId,
                                     String configName) throws Exception {
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
        transformer.transform(enterpriseId, fhir, data, csvWriter, hmAllResources, enterpriseOrganisationId, enterprisePatientId, enterprisePersonId, configName);

        return enterpriseId;
    }
}
