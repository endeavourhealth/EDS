package org.endeavourhealth.transform.fhir;

import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.terminology.SnomedCode;
import org.endeavourhealth.transform.terminology.TerminologyService;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Coding;

import java.util.List;

public class CodeableConceptHelper {

    public static CodeableConcept createCodeableConcept(String system, String term, String code) {
        return new CodeableConcept().addCoding(CodingHelper.createCoding(system, term, code));
    }

    public static CodeableConcept createCodeableConcept(String text) {
        return new CodeableConcept().setText(text);
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
            codeableConcept.addCoding(CodingHelper.createCoding(mapping));
        } else if (system.equals(FhirUri.CODE_SYSTEM_READ2)) {
            SnomedCode mapping = TerminologyService.translateRead2ToSnomed(coding.getCode());
            codeableConcept.addCoding(CodingHelper.createCoding(mapping));
        } else if (system.equals(FhirUri.CODE_SYSTEM_EMISPREPARATION)) {
            SnomedCode mapping = TerminologyService.translateEmisPreparationToSnomed(coding.getCode());
            codeableConcept.addCoding(CodingHelper.createCoding(mapping));
        } else if (system.equals(FhirUri.CODE_SYSTEM_EMISSNOMED)) {
            SnomedCode mapping = TerminologyService.translateEmisSnomedToSnomed(coding.getCode());
            codeableConcept.addCoding(CodingHelper.createCoding(mapping));
        } else {
            throw new TransformException("Unexpected coding system [" + system + "]");
        }


    }
}
