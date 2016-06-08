package org.endeavourhealth.transform.fhir;

import org.endeavourhealth.transform.terminology.SnomedCode;
import org.hl7.fhir.instance.model.Coding;

public class CodingHelper {

    public static Coding createCoding(SnomedCode snomedCode) {
        return new Coding()
                .setSystem(FhirUri.CODE_SYSTEM_SNOMED_CT)
                .setDisplay(snomedCode.getTerm())
                .setCode(snomedCode.getConceptCode());
    }

    public static Coding createCoding(String system, String term, String code) {
        return new Coding()
                .setSystem(system)
                .setDisplay(term)
                .setCode(code);
    }

}
