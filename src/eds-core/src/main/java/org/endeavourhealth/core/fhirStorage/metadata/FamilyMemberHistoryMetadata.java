package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.FamilyMemberHistory;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class FamilyMemberHistoryMetadata extends AbstractResourceMetadata implements PatientCompartment {
    private UUID patientId;

    @Override
    public UUID getPatientId() {
        return patientId;
    }

    public FamilyMemberHistoryMetadata(FamilyMemberHistory resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(FamilyMemberHistory resource) {
        patientId = UUID.fromString(ReferenceHelper.getReferenceId(resource.getPatient(), ResourceType.Patient));
    }
}
