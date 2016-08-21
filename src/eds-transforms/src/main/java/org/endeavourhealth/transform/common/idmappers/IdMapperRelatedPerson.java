package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.RelatedPerson;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperRelatedPerson extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) {
        RelatedPerson relatedPerson = (RelatedPerson)resource;

        super.mapResourceId(relatedPerson, serviceId, systemId);
        super.mapExtensions(relatedPerson, serviceId, systemId);

        if (relatedPerson.hasIdentifier()) {
            super.mapIdentifiers(relatedPerson.getIdentifier(), resource, serviceId, systemId);
        }
        if (relatedPerson.hasPatient()) {
            super.mapReference(relatedPerson.getPatient(), resource, serviceId, systemId);
        }
    }
}
