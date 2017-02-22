package org.endeavourhealth.transform.terminology;

import org.endeavourhealth.common.fhir.FhirUri;

public class SnomedCode {

    private String conceptCode = null;
    private String term = null;

    public SnomedCode(String conceptCode, String term) {
        this.conceptCode = conceptCode;
        this.term = term;
    }

    public String getConceptCode() {
        return conceptCode;
    }

    public String getTerm() {
        return term;
    }

    public String getSystem() {
        return FhirUri.CODE_SYSTEM_SNOMED_CT;
    }
}
