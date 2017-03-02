package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.Specimen;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class SpecimenMetadata extends AbstractResourceMetadata implements PatientCompartment {
    private UUID patientId;

    @Override
    public UUID getPatientId() {
        return patientId;
    }

    public SpecimenMetadata(Specimen resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(Specimen resource) {
        patientId = UUID.fromString(ReferenceHelper.getReferenceId(resource.getSubject(), ResourceType.Patient));
    }
}
