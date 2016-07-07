package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.RelatedPerson;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperRelatedPerson extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        RelatedPerson relatedPerson = (RelatedPerson)resource;

        super.mapResourceId(relatedPerson, serviceId, systemInstanceId);
        super.mapExtensions(relatedPerson, serviceId, systemInstanceId);

        if (relatedPerson.hasIdentifier()) {
            super.mapIdentifiers(relatedPerson.getIdentifier(), serviceId, systemInstanceId);
        }
        if (relatedPerson.hasPatient()) {
            super.mapReference(relatedPerson.getPatient(), serviceId, systemInstanceId);
        }
    }
}
