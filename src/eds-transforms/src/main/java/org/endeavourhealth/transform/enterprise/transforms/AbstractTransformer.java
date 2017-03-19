package org.endeavourhealth.transform.enterprise.transforms;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.endeavourhealth.common.cache.ParserPool;
import org.endeavourhealth.common.fhir.ReferenceComponents;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.rdbms.transform.EnterpriseIdHelper;
import org.endeavourhealth.transform.enterprise.outputModels.AbstractEnterpriseCsvWriter;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.TemporalPrecisionEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTransformer.class);
    private static final ParserPool PARSER_POOL = new ParserPool();

    //private static final EnterpriseIdMapRepository idMappingRepository = new EnterpriseIdMapRepository();
    private static JCS cache = null;
    private static Map<String, AtomicInteger> maxIdMap = new ConcurrentHashMap<>();
    private static ReentrantLock futuresLock = new ReentrantLock();

    static {
        try {

            //by default the Java Caching System has a load of logging enabled, which is really slow, so turn it off
            //not longer required, since it no longer uses log4J and the new default doesn't have debug enabled
            /*org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("org.apache.jcs");
            logger.setLevel(org.apache.log4j.Level.OFF);*/

            cache = JCS.getInstance("EnterpriseResourceMap");

            cache.get("Something");

        } catch (CacheException ex) {
            throw new RuntimeException("Error initialising cache", ex);
        }
    }

    public abstract void transform(ResourceByExchangeBatch resource,
                                   OutputContainer data,
                                   Map<String, ResourceByExchangeBatch> otherResources,
                                   Long enterpriseOrganisationUuid) throws Exception;


    protected static Integer convertDatePrecision(TemporalPrecisionEnum precision) throws Exception {
        return Integer.valueOf(precision.getCalendarConstant());
    }



    protected static Long findEnterpriseId(AbstractEnterpriseCsvWriter csvWriter, Resource resource) throws Exception {
        String resourceType = resource.getResourceType().toString();
        UUID resourceId = UUID.fromString(resource.getId());
        return findEnterpriseId(csvWriter, resourceType, resourceId);
    }

    protected static Long findEnterpriseId(AbstractEnterpriseCsvWriter csvWriter, Reference reference) throws Exception {
        ReferenceComponents comps = ReferenceHelper.getReferenceComponents(reference);
        String resourceType = comps.getResourceType().toString();
        UUID resourceId = UUID.fromString(comps.getId());
        return findEnterpriseId(csvWriter, resourceType, resourceId);
    }

    protected static Long findEnterpriseId(AbstractEnterpriseCsvWriter csvWriter, ResourceByExchangeBatch resource) throws Exception {
        return findEnterpriseId(csvWriter, resource.getResourceType(), resource.getResourceId());
    }

    protected static Long findEnterpriseId(AbstractEnterpriseCsvWriter csvWriter, String resourceType, UUID resourceId) throws Exception {
        String enterpriseTableName = csvWriter.getFileNameWithoutExtension();
        Long ret = checkCacheForId(enterpriseTableName, resourceType, resourceId);
        if (ret == null) {
            ret = EnterpriseIdHelper.findEnterpriseId(enterpriseTableName, resourceType, resourceId);
            //ret = idMappingRepository.getEnterpriseIdMappingId(enterpriseTableName, resourceType, resourceId);
        }
        return ret;
    }

    protected static Long createEnterpriseId(AbstractEnterpriseCsvWriter csvWriter, ResourceByExchangeBatch resource) throws Exception {
        String resourceType = resource.getResourceType();
        UUID resourceId = resource.getResourceId();
        return createEnterpriseId(csvWriter, resourceType, resourceId);
    }

    protected static Long createEnterpriseId(AbstractEnterpriseCsvWriter csvWriter, String resourceType, UUID resourceId) throws Exception {
        String enterpriseTableName = csvWriter.getFileNameWithoutExtension();

        Long enterpriseId = EnterpriseIdHelper.findOrCreateEnterpriseId(enterpriseTableName, resourceType, resourceId);
        /*int enterpriseId = getNextId(enterpriseTableName);
        idMappingRepository.saveEnterpriseIdMax(enterpriseTableName, Integer.valueOf(enterpriseId));
        idMappingRepository.saveEnterpriseIdMapping(enterpriseTableName, resourceType, resourceId, Integer.valueOf(enterpriseId));*/

        addIdToCache(enterpriseTableName, resourceType, resourceId, enterpriseId);
        return enterpriseId;
    }

    private static Long checkCacheForId(String enterpriseTableName, String resourceType, UUID resourceId) throws Exception {
        return (Long)cache.get(enterpriseTableName + ":" + resourceType + "/" + resourceId);
    }
    private static void addIdToCache(String enterpriseTableName, String resourceType, UUID resourceId, Long toCache) throws Exception {
        cache.put(enterpriseTableName + ":" + resourceType + "/" + resourceId, toCache);
    }

    protected static Resource deserialiseResouce(ResourceByExchangeBatch resourceByExchangeBatch) throws Exception {

        String json = resourceByExchangeBatch.getResourceData();
        try {
            return PARSER_POOL.parse(json);

        } catch (Exception ex) {
            LOG.error("Error deserialising resource", ex);
            LOG.error(json);
            throw ex;
        }

    }

    protected static Resource findResource(Reference reference,
                                           Map<String, ResourceByExchangeBatch> hsAllResources) throws Exception {
        String referenceStr = reference.getReference();

        //look in our resources map first
        ResourceByExchangeBatch ret = hsAllResources.get(referenceStr);
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

    protected static Long mapId(ResourceByExchangeBatch resource, AbstractEnterpriseCsvWriter csvWriter) throws Exception {
        Long enterpriseId = findEnterpriseId(csvWriter, resource);

        if (resource.getIsDeleted()) {

            //if it's a delete, but we've never sent it to enterprise, return false so we know not to send it now
            if (enterpriseId == null) {
                return null;
            }

            return enterpriseId;

        } else {

            if (enterpriseId == null) {
                //if we don't have an schema ID, the resource is new, so should be an INSERT transaction
                enterpriseId = createEnterpriseId(csvWriter, resource);
            }

            return enterpriseId;
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
}
