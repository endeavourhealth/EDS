package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.MedicationOrder;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MedicationOrderMetadata extends AbstractResourceMetadata implements PatientCompartment {
    private UUID patientId;
    private List<Code> codes;

    @Override
    public UUID getPatientId() {
        return patientId;
    }

    public List<Code> getCodes() { return codes; }

    public MedicationOrderMetadata(MedicationOrder resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(MedicationOrder resource) {
        try {
            patientId = UUID.fromString(ReferenceHelper.getReferenceId(resource.getPatient(), ResourceType.Patient));
            CodeableConcept codeableConcept = resource.getMedicationCodeableConcept();
            codes = new ArrayList<>();

            for (Coding coding : codeableConcept.getCoding()) {
                String system = coding.getSystem();
                String code = coding.getCode();
                String display = coding.getDisplay();

                Code newCode = new Code();
                newCode.setSystem(system);
                newCode.setCode(code);

                codes.add(newCode);
            }

        }
        catch (Exception e) {
        }
    }
}
