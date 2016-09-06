package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.Immunization;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class ImmunizationMetadata extends AbstractResourceMetadata implements PatientCompartment {
    private UUID patientId;
    private CodeableConcept codeableConcept;


    @Override
    public UUID getPatientId() {
        return patientId;
    }

    public CodeableConcept getCodeableConcept() { return codeableConcept; }


    public ImmunizationMetadata(Immunization resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(Immunization resource) {
        patientId = UUID.fromString(ReferenceHelper.getReferenceId(resource.getPatient(), ResourceType.Patient));
        CodeableConcept codes = resource.getVaccineCode();
        codeableConcept = null;
        for (Coding coding : codes.getCoding()) {
            String system = coding.getSystem();
            String code = coding.getCode();
            String display = coding.getDisplay();
            Coding newCoding = new Coding();
            newCoding.setSystem(system);
            newCoding.setCode(code);
            newCoding.setDisplay(display);

            codeableConcept.addCoding(newCoding);
        }

    }
}
