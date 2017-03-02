package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class EncounterMetadata extends AbstractResourceMetadata implements PatientCompartment {
    private UUID patientId;

    @Override
    public UUID getPatientId() {
        return patientId;
    }

    public EncounterMetadata(Encounter resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(Encounter resource) {
        patientId = UUID.fromString(ReferenceHelper.getReferenceId(resource.getPatient(), ResourceType.Patient));
    }
}
