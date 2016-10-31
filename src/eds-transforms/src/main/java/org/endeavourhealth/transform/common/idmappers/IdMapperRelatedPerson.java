package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.RelatedPerson;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperRelatedPerson extends BaseIdMapper {
    @Override
    public boolean mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        RelatedPerson relatedPerson = (RelatedPerson)resource;

        if (relatedPerson.hasIdentifier()) {
            super.mapIdentifiers(relatedPerson.getIdentifier(), resource, serviceId, systemId);
        }
        if (relatedPerson.hasPatient()) {
            super.mapReference(relatedPerson.getPatient(), resource, serviceId, systemId);
        }

        return super.mapCommonResourceFields(relatedPerson, serviceId, systemId, mapResourceId);
    }
}
