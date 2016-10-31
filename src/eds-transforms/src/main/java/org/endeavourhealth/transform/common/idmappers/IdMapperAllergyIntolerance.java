package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperAllergyIntolerance extends BaseIdMapper {

    @Override
    public boolean mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        AllergyIntolerance allergyIntolerance = (AllergyIntolerance)resource;

        if (allergyIntolerance.hasIdentifier()) {
            super.mapIdentifiers(allergyIntolerance.getIdentifier(), resource, serviceId, systemId);
        }
        if (allergyIntolerance.hasRecorder()) {
            super.mapReference(allergyIntolerance.getRecorder(), resource, serviceId, systemId);
        }
        if (allergyIntolerance.hasPatient()) {
            super.mapReference(allergyIntolerance.getPatient(), resource, serviceId, systemId);
        }

        return super.mapCommonResourceFields(allergyIntolerance, serviceId, systemId, mapResourceId);
    }
}
