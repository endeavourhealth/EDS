package org.endeavourhealth.transform.terminology;

import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Coding;

import java.util.List;

public abstract class TerminologyService {




    public static SnomedCode translateRead2ToSnomed(String code) {
        //TODO - terminology service needs completing
        return null;
    }
    public static SnomedCode translateCtv3ToSnomed(String code) {
        //TODO - terminology service needs completing
        return null;
    }
    public static SnomedCode translateEmisSnomedToSnomed(String code) {
        //TODO - terminology service needs completing
        return null;
    }
    public static SnomedCode translateEmisPreparationToSnomed(String code) {
        //TODO - terminology service needs completing
        return null;
    }

    /**
     * checks the first Coding element in the CodeableConcept and adds a second Coding if it
     * needs to be mapped to SNOMED CT
     */
    public static void translateToSnomed(CodeableConcept codeableConcept) throws TransformException {
        List<Coding> codingList = codeableConcept.getCoding();
        if (codingList.isEmpty()) {
            return;
        }

        Coding coding = codingList.get(0);
        String system = coding.getSystem();
        if (system.equals(FhirUri.CODE_SYSTEM_SNOMED_CT)) {
            //no mapping required
        } else if (system.equals(FhirUri.CODE_SYSTEM_CTV3)) {
            SnomedCode mapping = TerminologyService.translateCtv3ToSnomed(coding.getCode());
            codeableConcept.addCoding(mapping.toCoding());
        } else if (system.equals(FhirUri.CODE_SYSTEM_READ2)) {
            SnomedCode mapping = TerminologyService.translateRead2ToSnomed(coding.getCode());
            codeableConcept.addCoding(mapping.toCoding());
        } else if (system.equals(FhirUri.CODE_SYSTEM_EMISPREPARATION)) {
            SnomedCode mapping = TerminologyService.translateEmisPreparationToSnomed(coding.getCode());
            codeableConcept.addCoding(mapping.toCoding());
        } else if (system.equals(FhirUri.CODE_SYSTEM_EMISSNOMED)) {
            SnomedCode mapping = TerminologyService.translateEmisSnomedToSnomed(coding.getCode());
            codeableConcept.addCoding(mapping.toCoding());
        } else {
            throw new TransformException("Unexpected coding system [" + system + "]");
        }
    }
}

