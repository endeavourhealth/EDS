package org.endeavourhealth.transform.enterprise.transforms;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.data.transform.EnterpriseIdMapRepository;
import org.endeavourhealth.core.xml.enterprise.BaseRecord;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceComponents;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
                                 EnterpriseData data,
                                 Map<String, ResourceByExchangeBatch> otherResources,
                                 Integer enterpriseOrganisationUuid) throws Exception;


    protected static XMLGregorianCalendar convertDate(Date date) throws Exception {

        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
    }

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

    protected static Integer findEnterpriseId(Resource resource) throws Exception {
        String resourceType = resource.getResourceType().toString();
        UUID resourceId = UUID.fromString(resource.getId());
        return findEnterpriseId(resourceType, resourceId);
    }

    protected static Integer findEnterpriseId(Reference reference) throws Exception {
        ReferenceComponents comps = ReferenceHelper.getReferenceComponents(reference);
        String resourceType = comps.getResourceType().toString();
        UUID resourceId = UUID.fromString(comps.getId());
        return findEnterpriseId(resourceType, resourceId);
    }

    protected static Integer findEnterpriseId(ResourceByExchangeBatch resource) throws Exception {
        return findEnterpriseId(resource.getResourceType(), resource.getResourceId());
    }

    private static Integer findEnterpriseId(String resourceType, UUID resourceId) throws Exception {
        Integer ret = checkCacheForId(resourceType, resourceId);
        if (ret == null) {
            ret = idMappingRepository.getEnterpriseIdMappingId(resourceType, resourceId);
        }
        return ret;
    }



    protected static Integer createEnterpriseId(ResourceByExchangeBatch resource) throws Exception {
        String resourceType = resource.getResourceType();
        UUID resourceId = resource.getResourceId();
        int enterpriseId = getNextId(resourceType);
        idMappingRepository.saveEnterpriseIdMapping(resourceType, resourceId, new Integer(enterpriseId));
        addIdToCache(resource.getResourceType(), resource.getResourceId(), enterpriseId);
        return enterpriseId;
    }

    protected static void setEnterpriseId(String resourceType, UUID resourceId, Integer enterpriseId) throws Exception {
        idMappingRepository.saveEnterpriseIdMapping(resourceType, resourceId, enterpriseId);
        addIdToCache(resourceType, resourceId, enterpriseId);
    }

    private static Integer checkCacheForId(String resourceType, UUID resourceId) throws Exception {
        return (Integer)cache.get(resourceType + "/" + resourceId);
    }
    private static void addIdToCache(String resourceType, UUID resourceId, Integer toCache) throws Exception {
        cache.put(resourceType + "/" + resourceId, toCache);
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

    protected static boolean mapIdAndMode(ResourceByExchangeBatch resource, BaseRecord baseRecord) throws Exception {
        Integer enterpriseId = findEnterpriseId(resource);

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
                enterpriseId = createEnterpriseId(resource);

                baseRecord.setId(enterpriseId);
                baseRecord.setSaveMode(SaveMode.INSERT);

            } else {
                //if we have an schema ID, the resource was previously passed to Enterprise, so it's an update
                baseRecord.setId(enterpriseId);
                baseRecord.setSaveMode(SaveMode.UPDATE);
            }
        }

        return true;
    }

    private static int getNextId(String resourceType) {

        AtomicInteger ai = maxIdMap.get(resourceType);
        if (ai == null) {
            futuresLock.lock();

            ai = maxIdMap.get(resourceType);
            if (ai == null) {
                int lastVal = idMappingRepository.getMaxEnterpriseId(resourceType);
                ai = new AtomicInteger(lastVal);
                maxIdMap.put(resourceType, ai);
            }

            futuresLock.unlock();
        }
        return ai.incrementAndGet();
    }
}
