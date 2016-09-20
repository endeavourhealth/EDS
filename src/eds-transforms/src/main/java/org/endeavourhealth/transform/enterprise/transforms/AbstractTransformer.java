package org.endeavourhealth.transform.enterprise.transforms;

import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.core.data.ehr.models.ResourceByExchangeBatch;
import org.endeavourhealth.core.data.transform.EnterpriseIdMapRepository;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.enterprise.schema.BaseRecord;
import org.endeavourhealth.transform.enterprise.schema.DatePrecision;
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

public class AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTransformer.class);

    protected static final String INSERT = "insert";
    protected static final String UPDATE = "update";
    protected static final String DELETE = "delete";

    private static final EnterpriseIdMapRepository idMappingRepository = new EnterpriseIdMapRepository();

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
            if (coding.getSystem().equals(FhirUri.CODE_SYSTEM_SNOMED_CT)) {
                return Long.parseLong(coding.getCode());
            }
        }

        return null;
    }

    protected static UUID findEnterpriseUuid(Resource resource) {
        String resourceType = resource.getResourceType().toString();
        UUID uuid = UUID.fromString(resource.getId());
        return idMappingRepository.getEnterpriseIdMappingUuid(resourceType, uuid);
    }

    protected static UUID findEnterpriseUuid(Reference reference) {
        ReferenceComponents comps = ReferenceHelper.getReferenceComponents(reference);
        String resourceType = comps.getResourceType().toString();
        UUID uuid = UUID.fromString(comps.getId());
        return idMappingRepository.getEnterpriseIdMappingUuid(resourceType, uuid);
    }

    protected static UUID findEnterpriseUuid(ResourceByExchangeBatch resource) {
        return idMappingRepository.getEnterpriseIdMappingUuid(resource.getResourceType(), resource.getResourceId());
    }

    protected static UUID createEnterpriseUuid(ResourceByExchangeBatch resource) {
        return idMappingRepository.createEnterpriseIdMappingUuid(resource.getResourceType(), resource.getResourceId());
    }

    protected static Resource deserialiseResouce(ResourceByExchangeBatch resourceByExchangeBatch) throws Exception {

        String json = resourceByExchangeBatch.getResourceData();
        try {
            return new JsonParser().parse(json);

        } catch (Exception ex) {
            LOG.error(ex.getMessage());
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

    protected static void mapIdAndMode(ResourceByExchangeBatch resource, BaseRecord baseRecord) {
        UUID enterpriseId = findEnterpriseUuid(resource);

        if (resource.getIsDeleted()) {

            //if we've no Enterprise ID, the resource was never passed to Enterprise, so don't bother telling it to delete
            if (enterpriseId != null) {
                baseRecord.setId(enterpriseId.toString());
                baseRecord.setMode(DELETE);
            }

        } else {

            if (enterpriseId == null) {
                //if we don't have an enterprise ID, the resource is new, so should be an INSERT transaction
                enterpriseId = createEnterpriseUuid(resource);

                baseRecord.setId(enterpriseId.toString());
                baseRecord.setMode(INSERT);

            } else {
                //if we have an enterprise ID, the resource was previously passed to Enterprise, so it's an update
                baseRecord.setId(enterpriseId.toString());
                baseRecord.setMode(UPDATE);
            }
        }
    }
}
