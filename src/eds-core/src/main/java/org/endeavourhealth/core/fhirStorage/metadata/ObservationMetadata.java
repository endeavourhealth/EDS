package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class ObservationMetadata extends AbstractResourceMetadata implements PatientCompartment {
    private UUID patientId;

    @Override
    public UUID getPatientId() {
        return patientId;
    }

    public ObservationMetadata(Observation resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(Observation resource) {
        patientId = UUID.fromString(ReferenceHelper.getReferenceId(resource.getSubject(), ResourceType.Patient));
    }
}
