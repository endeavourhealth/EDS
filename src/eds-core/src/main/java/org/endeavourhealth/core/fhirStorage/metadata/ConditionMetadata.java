package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class ConditionMetadata extends AbstractResourceMetadata implements PatientCompartment {
    private UUID patientId;

    @Override
    public UUID getPatientId() {
        return patientId;
    }

    public ConditionMetadata(Condition resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(Condition resource) {
        patientId = UUID.fromString(ReferenceHelper.getReferenceId(resource.getPatient(), ResourceType.Patient));
    }
}
