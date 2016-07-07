package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperAllergyIntolerance extends BaseIdMapper {

    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        AllergyIntolerance allergyIntolerance = (AllergyIntolerance)resource;

        super.mapResourceId(allergyIntolerance, serviceId, systemInstanceId);
        super.mapExtensions(allergyIntolerance, serviceId, systemInstanceId);

        if (allergyIntolerance.hasIdentifier()) {
            super.mapIdentifiers(allergyIntolerance.getIdentifier(), serviceId, systemInstanceId);
        }
        if (allergyIntolerance.hasRecorder()) {
            super.mapReference(allergyIntolerance.getRecorder(), serviceId, systemInstanceId);
        }
        if (allergyIntolerance.hasPatient()) {
            super.mapReference(allergyIntolerance.getPatient(), serviceId, systemInstanceId);
        }
    }
}
