package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.Patient;

import java.util.UUID;

public class PatientMetadata extends AbstractResourceMetadata implements PatientCompartment {
    private UUID patientId;
    private boolean active;
    private boolean deceased;

    @Override
    public UUID getPatientId() {
        return patientId;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isDeceased() {
        return deceased;
    }

    public PatientMetadata(Patient resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(Patient resource) {
        patientId = UUID.fromString(resource.getId());
        active = resource.getActive();
        deceased = resource.hasDeceased();
    }
}
