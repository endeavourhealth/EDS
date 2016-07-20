package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.Immunization;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class ImmunizationMetadata extends AbstractResourceMetadata implements PatientCompartment {
    private UUID patientId;

    @Override
    public UUID getPatientId() {
        return patientId;
    }

    public ImmunizationMetadata(Immunization resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(Immunization resource) {
        patientId = UUID.fromString(ReferenceHelper.getReferenceId(resource.getPatient(), ResourceType.Patient));
    }
}
