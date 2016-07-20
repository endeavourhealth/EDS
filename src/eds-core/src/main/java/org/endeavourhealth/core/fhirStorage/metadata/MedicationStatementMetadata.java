package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.MedicationStatement;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class MedicationStatementMetadata extends AbstractResourceMetadata implements PatientCompartment {
    private UUID patientId;

    @Override
    public UUID getPatientId() {
        return patientId;
    }

    public MedicationStatementMetadata(MedicationStatement resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(MedicationStatement resource) {
        patientId = UUID.fromString(ReferenceHelper.getReferenceId(resource.getPatient(), ResourceType.Patient));
    }
}
