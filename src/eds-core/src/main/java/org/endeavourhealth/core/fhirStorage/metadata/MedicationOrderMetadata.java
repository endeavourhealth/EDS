package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.MedicationOrder;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class MedicationOrderMetadata extends AbstractResourceMetadata implements PatientCompartment {
    private UUID patientId;
    private CodeableConcept codeableConcept;

    @Override
    public UUID getPatientId() {
        return patientId;
    }

    public CodeableConcept getCodeableConcept() { return codeableConcept; }


    public MedicationOrderMetadata(MedicationOrder resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(MedicationOrder resource) {
        try {
            patientId = UUID.fromString(ReferenceHelper.getReferenceId(resource.getPatient(), ResourceType.Patient));
            codeableConcept = resource.getMedicationCodeableConcept();
        }
        catch (Exception e) {
        }
    }
}
