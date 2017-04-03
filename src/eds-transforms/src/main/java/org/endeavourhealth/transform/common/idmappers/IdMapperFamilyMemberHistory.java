package org.endeavourhealth.transform.common.idmappers;

import org.endeavourhealth.transform.common.exceptions.PatientResourceException;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.FamilyMemberHistory;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class IdMapperFamilyMemberHistory extends BaseIdMapper {
    @Override
    public boolean mapIds(Resource resource, UUID serviceId, UUID systemId, boolean mapResourceId) throws Exception {
        FamilyMemberHistory familyHistory = (FamilyMemberHistory)resource;

        if (familyHistory.hasIdentifier()) {
            super.mapIdentifiers(familyHistory.getIdentifier(), resource, serviceId, systemId);
        }
        if (familyHistory.hasPatient()) {
            super.mapReference(familyHistory.getPatient(), resource, serviceId, systemId);
        }

        return super.mapCommonResourceFields(familyHistory, serviceId, systemId, mapResourceId);
    }

    @Override
    public String getPatientId(Resource resource) throws PatientResourceException {

        FamilyMemberHistory familyHistory = (FamilyMemberHistory)resource;
        if (familyHistory.hasPatient()) {
            return ReferenceHelper.getReferenceId(familyHistory.getPatient(), ResourceType.Patient);
        }
        return null;
    }
}
