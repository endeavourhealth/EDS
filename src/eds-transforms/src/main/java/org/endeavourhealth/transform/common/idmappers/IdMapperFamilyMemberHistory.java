package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.DiagnosticReport;
import org.hl7.fhir.instance.model.FamilyMemberHistory;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperFamilyMemberHistory extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemInstanceId) {
        FamilyMemberHistory familyHistory = (FamilyMemberHistory)resource;

        super.mapResourceId(familyHistory, serviceId, systemInstanceId);
        super.mapExtensions(familyHistory, serviceId, systemInstanceId);

        if (familyHistory.hasIdentifier()) {
            super.mapIdentifiers(familyHistory.getIdentifier(), serviceId, systemInstanceId);
        }
        if (familyHistory.hasPatient()) {
            super.mapReference(familyHistory.getPatient(), serviceId, systemInstanceId);
        }
    }
}
