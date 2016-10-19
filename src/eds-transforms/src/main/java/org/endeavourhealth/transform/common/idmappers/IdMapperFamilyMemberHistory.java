package org.endeavourhealth.transform.common.idmappers;

import org.hl7.fhir.instance.model.FamilyMemberHistory;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class IdMapperFamilyMemberHistory extends BaseIdMapper {
    @Override
    public void mapIds(Resource resource, UUID serviceId, UUID systemId) throws Exception {
        FamilyMemberHistory familyHistory = (FamilyMemberHistory)resource;

        super.mapResourceId(familyHistory, serviceId, systemId);
        super.mapExtensions(familyHistory, serviceId, systemId);

        if (familyHistory.hasIdentifier()) {
            super.mapIdentifiers(familyHistory.getIdentifier(), resource, serviceId, systemId);
        }
        if (familyHistory.hasPatient()) {
            super.mapReference(familyHistory.getPatient(), resource, serviceId, systemId);
        }
    }
}
