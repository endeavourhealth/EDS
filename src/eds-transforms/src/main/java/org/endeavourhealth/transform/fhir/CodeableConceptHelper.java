package org.endeavourhealth.transform.fhir;

import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Coding;

public class CodeableConceptHelper {

    public static CodeableConcept createCodeableConcept(String system, String term, String code) {
        return new CodeableConcept()
                .addCoding(new Coding()
                        .setSystem(system)
                        .setDisplay(term)
                        .setCode(code));
    }

    public static CodeableConcept createCodeableConcept(String text) {
        return new CodeableConcept()
                .setText(text);
    }
}
