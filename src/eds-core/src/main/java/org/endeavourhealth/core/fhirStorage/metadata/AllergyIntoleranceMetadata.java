package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class AllergyIntoleranceMetadata extends AbstractResourceMetadata implements PatientCompartment {
    private UUID patientId;
    private CodeableConcept codeableConcept;

    @Override
    public UUID getPatientId() {
        return patientId;
    }

    public CodeableConcept getCodeableConcept() { return codeableConcept; }

    public AllergyIntoleranceMetadata(AllergyIntolerance resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(AllergyIntolerance resource) {
        patientId = UUID.fromString(ReferenceHelper.getReferenceId(resource.getPatient(), ResourceType.Patient));
        codeableConcept = resource.getSubstance();
    }
}
