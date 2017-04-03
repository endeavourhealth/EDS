package org.endeavourhealth.transform.common.idmappers;

import org.endeavourhealth.transform.common.exceptions.PatientResourceException;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

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

    @Override
    public String getPatientId(Resource resource) throws PatientResourceException {

        AllergyIntolerance allergyIntolerance = (AllergyIntolerance)resource;
        if (allergyIntolerance.hasPatient()) {
            return ReferenceHelper.getReferenceId(allergyIntolerance.getPatient(), ResourceType.Patient);
        }
        return null;
    }
}
