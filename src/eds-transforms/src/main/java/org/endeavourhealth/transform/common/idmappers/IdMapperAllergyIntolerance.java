package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperAllergyIntolerance extends BaseIdMapper {

    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) {
        AllergyIntolerance allergyIntolerance = (AllergyIntolerance)resource;

        super.mapResourceId(allergyIntolerance, serviceId, systemId);
        super.mapExtensions(allergyIntolerance, serviceId, systemId);

        if (allergyIntolerance.hasIdentifier()) {
            super.mapIdentifiers(allergyIntolerance.getIdentifier(), resource, serviceId, systemId);
        }
        if (allergyIntolerance.hasRecorder()) {
            super.mapReference(allergyIntolerance.getRecorder(), resource, serviceId, systemId);
        }
        if (allergyIntolerance.hasPatient()) {
            super.mapReference(allergyIntolerance.getPatient(), resource, serviceId, systemId);
        }
    }
}
