package org.endeavourhealth.transform.enterprise.transforms;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.data.transform.EnterpriseIdMapRepository;
import org.endeavourhealth.core.xml.enterprise.BaseRecord;
import org.endeavourhealth.core.xml.enterprise.DatePrecision;
import org.endeavourhealth.core.xml.enterprise.EnterpriseData;
import org.endeavourhealth.core.xml.enterprise.SaveMode;
import org.endeavourhealth.transform.common.exceptions.TransformException;
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

public abstract class AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTransformer.class);

    private static final EnterpriseIdMapRepository idMappingRepository = new EnterpriseIdMapRepository();
    private static JCS cache = null;

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
                                 UUID enterpriseOrganisationUuid) throws Exception;


    protected static XMLGregorianCalendar convertDate(Date date) throws Exception {

        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
    }

    protected static DatePrecision convertDatePrecision(TemporalPrecisionEnum precision) throws Exception {
        if (precision == TemporalPrecisionEnum.YEAR) {
            return DatePrecision.YEAR;
        } else if (precision == TemporalPrecisionEnum.MONTH) {
            return DatePrecision.MONTH;
        } else if (precision == TemporalPrecisionEnum.DAY) {
            return DatePrecision.DAY;
        } else if (precision == TemporalPrecisionEnum.MINUTE) {
            return DatePrecision.MINUTE;
        } else {
            throw new TransformException("Unsupported date time precision " + precision);
        }
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

    protected static UUID findEnterpriseUuid(Resource resource) throws Exception {
        String resourceType = resource.getResourceType().toString();
        UUID resourceId = UUID.fromString(resource.getId());
        UUID ret = checkCacheForUuid(resourceType, resourceId);
        if (ret == null) {
            ret = idMappingRepository.getEnterpriseIdMappingUuid(resourceType, resourceId);
        }
        return ret;
    }

    protected static UUID findEnterpriseUuid(Reference reference) throws Exception {
        ReferenceComponents comps = ReferenceHelper.getReferenceComponents(reference);
        String resourceType = comps.getResourceType().toString();
        UUID resourceId = UUID.fromString(comps.getId());
        UUID ret = checkCacheForUuid(resourceType, resourceId);
        if (ret == null) {
            ret = idMappingRepository.getEnterpriseIdMappingUuid(resourceType, resourceId);
        }
        return ret;
    }

    protected static UUID findEnterpriseUuid(ResourceByExchangeBatch resource) throws Exception {
        UUID ret = checkCacheForUuid(resource.getResourceType(), resource.getResourceId());
        if (ret == null) {
            ret = idMappingRepository.getEnterpriseIdMappingUuid(resource.getResourceType(), resource.getResourceId());
        }
        return ret;
    }

    protected static UUID createEnterpriseUuid(ResourceByExchangeBatch resource) throws Exception {
        UUID uuid = idMappingRepository.createEnterpriseIdMappingUuid(resource.getResourceType(), resource.getResourceId());
        addUuidToCache(resource.getResourceType(), resource.getResourceId(), uuid);
        return uuid;
    }

    protected static void setEnterpriseUuid(String resourceType, UUID resourceId, UUID enterpriseId) throws Exception {
        idMappingRepository.setEnterpriseIdMapping(resourceType, resourceId, enterpriseId);
        addUuidToCache(resourceType, resourceId, enterpriseId);
    }

    private static UUID checkCacheForUuid(String resourceType, UUID resourceId) throws Exception {
        return (UUID)cache.get(resourceType + "/" + resourceId);
    }
    private static void addUuidToCache(String resourceType, UUID resourceId, UUID toCache) throws Exception {
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

    protected static void mapIdAndMode(ResourceByExchangeBatch resource, BaseRecord baseRecord) throws Exception {
        UUID enterpriseId = findEnterpriseUuid(resource);

        if (resource.getIsDeleted()) {

            //if we've no Enterprise ID, the resource was never passed to Enterprise, so don't bother telling it to delete
            if (enterpriseId != null) {
                baseRecord.setId(enterpriseId.toString());
                baseRecord.setSaveMode(SaveMode.DELETE);
            }

        } else {

            if (enterpriseId == null) {
                //if we don't have an schema ID, the resource is new, so should be an INSERT transaction
                enterpriseId = createEnterpriseUuid(resource);

                baseRecord.setId(enterpriseId.toString());
                baseRecord.setSaveMode(SaveMode.INSERT);

            } else {
                //if we have an schema ID, the resource was previously passed to Enterprise, so it's an update
                baseRecord.setId(enterpriseId.toString());
                baseRecord.setSaveMode(SaveMode.UPDATE);
            }
        }
    }
}
