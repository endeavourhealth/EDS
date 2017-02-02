package org.endeavourhealth.transform.enterprise.transforms;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.data.transform.EnterpriseIdMapRepository;
import org.endeavourhealth.transform.enterprise.outputModels.AbstractEnterpriseCsvWriter;
import org.endeavourhealth.transform.enterprise.outputModels.OutputContainer;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceComponents;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTransformer.class);

    private static final EnterpriseIdMapRepository idMappingRepository = new EnterpriseIdMapRepository();
    private static JCS cache = null;
    private static Map<String, AtomicInteger> maxIdMap = new ConcurrentHashMap<>();
    private static ReentrantLock futuresLock = new ReentrantLock();

    static {
        try {

            //by default the Java Caching System has a load of logging enabled, which is really slow, so turn it off
            org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("org.apache.jcs");
            logger.setLevel(org.apache.log4j.Level.OFF);

            cache = JCS.getInstance("EnterpriseResourceMap");
        } catch (CacheException ex) {
            throw new RuntimeException("Error initialising cache", ex);
        }
    }

    public abstract void transform(ResourceByExchangeBatch resource,
                                   OutputContainer data,
                                   Map<String, ResourceByExchangeBatch> otherResources,
                                   Integer enterpriseOrganisationUuid) throws Exception;

    /*public abstract void transform(ResourceByExchangeBatch resource,
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 Integer enterpriseOrganisationUuid) throws Exception;*/


    /*protected static XMLGregorianCalendar convertDate(Date date) throws Exception {

        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
    }*/

    protected static Integer convertDatePrecision(TemporalPrecisionEnum precision) throws Exception {
        return new Integer(precision.getCalendarConstant());
    }


    protected static Long findSnomedConceptId(CodeableConcept code) {
        for (Coding coding: code.getCoding()) {
            if (coding.getSystem().equals(FhirUri.CODE_SYSTEM_SNOMED_CT)
                    || coding.getSystem().equals(FhirUri.CODE_SYSTEM_EMISSNOMED)) {
                return Long.parseLong(coding.getCode());
            }
        }

        return null;
    }

    protected static String findSnomedConceptText(CodeableConcept code) {
        for (Coding coding: code.getCoding()) {
            if (coding.getSystem().equals(FhirUri.CODE_SYSTEM_SNOMED_CT)
                    || coding.getSystem().equals(FhirUri.CODE_SYSTEM_EMISSNOMED)) {
                return coding.getDisplay();
            }
        }

        return null;
    }

    protected static String findOriginalCode(CodeableConcept code) {
        for (Coding coding: code.getCoding()) {
            if (coding.getSystem().equals(FhirUri.CODE_SYSTEM_READ2)
                    || coding.getSystem().equals(FhirUri.CODE_SYSTEM_EMIS_CODE)) {
                return coding.getCode();
            }
        }

        return null;
    }

    protected static Integer findEnterpriseId(AbstractEnterpriseCsvWriter csvWriter, Resource resource) throws Exception {
        String resourceType = resource.getResourceType().toString();
        UUID resourceId = UUID.fromString(resource.getId());
        return findEnterpriseId(csvWriter, resourceType, resourceId);
    }

    protected static Integer findEnterpriseId(AbstractEnterpriseCsvWriter csvWriter, Reference reference) throws Exception {
        ReferenceComponents comps = ReferenceHelper.getReferenceComponents(reference);
        String resourceType = comps.getResourceType().toString();
        UUID resourceId = UUID.fromString(comps.getId());
        return findEnterpriseId(csvWriter, resourceType, resourceId);
    }

    protected static  Integer findEnterpriseId(AbstractEnterpriseCsvWriter csvWriter, ResourceByExchangeBatch resource) throws Exception {
        return findEnterpriseId(csvWriter, resource.getResourceType(), resource.getResourceId());
    }

    private static Integer findEnterpriseId(AbstractEnterpriseCsvWriter csvWriter, String resourceType, UUID resourceId) throws Exception {
        String enterpriseTableName = csvWriter.getFileNameWithoutExtension();
        Integer ret = checkCacheForId(enterpriseTableName, resourceType, resourceId);
        if (ret == null) {
            ret = idMappingRepository.getEnterpriseIdMappingId(enterpriseTableName, resourceType, resourceId);
        }
        return ret;
    }



    protected static Integer createEnterpriseId(AbstractEnterpriseCsvWriter csvWriter, ResourceByExchangeBatch resource) throws Exception {
        String resourceType = resource.getResourceType();
        UUID resourceId = resource.getResourceId();
        return createEnterpriseId(csvWriter, resourceType, resourceId);
    }

    protected static Integer createEnterpriseId(AbstractEnterpriseCsvWriter csvWriter, String resourceType, UUID resourceId) throws Exception {
        String enterpriseTableName = csvWriter.getFileNameWithoutExtension();
        int enterpriseId = getNextId(enterpriseTableName);
        idMappingRepository.saveEnterpriseIdMax(enterpriseTableName, new Integer(enterpriseId));
        idMappingRepository.saveEnterpriseIdMapping(enterpriseTableName, resourceType, resourceId, new Integer(enterpriseId));
        addIdToCache(enterpriseTableName, resourceType, resourceId, enterpriseId);
        return enterpriseId;
    }

    /*protected static <T extends BaseRecord> Integer findEnterpriseId(T enterpriseTable, Resource resource) throws Exception {
        String resourceType = resource.getResourceType().toString();
        UUID resourceId = UUID.fromString(resource.getId());
        return findEnterpriseId(enterpriseTable, resourceType, resourceId);
    }

    protected static <T extends BaseRecord> Integer findEnterpriseId(T enterpriseTable, Reference reference) throws Exception {
        ReferenceComponents comps = ReferenceHelper.getReferenceComponents(reference);
        String resourceType = comps.getResourceType().toString();
        UUID resourceId = UUID.fromString(comps.getId());
        return findEnterpriseId(enterpriseTable, resourceType, resourceId);
    }

    protected static <T extends BaseRecord> Integer findEnterpriseId(T enterpriseTable, ResourceByExchangeBatch resource) throws Exception {
        return findEnterpriseId(enterpriseTable, resource.getResourceType(), resource.getResourceId());
    }

    private static <T extends BaseRecord> Integer findEnterpriseId(T enterpriseTable, String resourceType, UUID resourceId) throws Exception {
        String enterpriseTableName = enterpriseTable.getClass().getSimpleName();
        Integer ret = checkCacheForId(enterpriseTableName, resourceType, resourceId);
        if (ret == null) {
            ret = idMappingRepository.getEnterpriseIdMappingId(enterpriseTableName, resourceType, resourceId);
        }
        return ret;
    }



    protected static <T extends BaseRecord> Integer createEnterpriseId(T enterpriseTable, ResourceByExchangeBatch resource) throws Exception {
        String resourceType = resource.getResourceType();
        UUID resourceId = resource.getResourceId();
        return createEnterpriseId(enterpriseTable, resourceType, resourceId);
    }

    protected static <T extends BaseRecord> Integer createEnterpriseId(T enterpriseTable, String resourceType, UUID resourceId) throws Exception {
        String enterpriseTableName = enterpriseTable.getClass().getSimpleName();
        int enterpriseId = getNextId(resourceType);
        idMappingRepository.saveEnterpriseIdMax(enterpriseTableName, new Integer(enterpriseId));
        idMappingRepository.saveEnterpriseIdMapping(enterpriseTableName, resourceType, resourceId, new Integer(enterpriseId));
        addIdToCache(enterpriseTableName, resourceType, resourceId, enterpriseId);
        return enterpriseId;
    }*/



    private static Integer checkCacheForId(String enterpriseTableName, String resourceType, UUID resourceId) throws Exception {
        return (Integer)cache.get(enterpriseTableName + ":" + resourceType + "/" + resourceId);
    }
    private static void addIdToCache(String enterpriseTableName, String resourceType, UUID resourceId, Integer toCache) throws Exception {
        cache.put(enterpriseTableName + ":" + resourceType + "/" + resourceId, toCache);
    }

    protected static Resource deserialiseResouce(ResourceByExchangeBatch resourceByExchangeBatch) throws Exception {

        String json = resourceByExchangeBatch.getResourceData();
        try {
            return new JsonParser().parse(json);

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

    protected static Integer mapId(ResourceByExchangeBatch resource, AbstractEnterpriseCsvWriter csvWriter) throws Exception {
        Integer enterpriseId = findEnterpriseId(csvWriter, resource);

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



    /*protected static boolean mapIdAndMode(ResourceByExchangeBatch resource, BaseRecord baseRecord) throws Exception {
        Integer enterpriseId = findEnterpriseId(baseRecord, resource);

        if (resource.getIsDeleted()) {

            //if we've no Enterprise ID, the resource was never passed to Enterprise, so don't bother telling it to delete
            if (enterpriseId != null) {
                baseRecord.setId(enterpriseId);
                baseRecord.setSaveMode(SaveMode.DELETE);

            } else {
                //if it's a delete, but we've never sent it to enterprise, return false so we know not to send it now
                return false;
            }

        } else {

            if (enterpriseId == null) {
                //if we don't have an schema ID, the resource is new, so should be an INSERT transaction
                enterpriseId = createEnterpriseId(baseRecord, resource);
            }
            baseRecord.setId(enterpriseId);

            baseRecord.setSaveMode(SaveMode.UPSERT);
        }

        return true;
    }*/

    private static int getNextId(String enterpriseTableName) {

        AtomicInteger ai = maxIdMap.get(enterpriseTableName);
        if (ai == null) {
            futuresLock.lock();

            ai = maxIdMap.get(enterpriseTableName);
            if (ai == null) {
                int lastVal = idMappingRepository.getMaxEnterpriseId(enterpriseTableName);
                ai = new AtomicInteger(lastVal);
                maxIdMap.put(enterpriseTableName, ai);
            }

            futuresLock.unlock();
        }
        return ai.incrementAndGet();
    }
}
