package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.MedicationOrder;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class MedicationOrderMetadata extends AbstractResourceMetadata implements PatientCompartment {
    private UUID patientId;

    @Override
    public UUID getPatientId() {
        return patientId;
    }

    public MedicationOrderMetadata(MedicationOrder resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(MedicationOrder resource) {
        patientId = UUID.fromString(ReferenceHelper.getReferenceId(resource.getPatient(), ResourceType.Patient));
    }
}
