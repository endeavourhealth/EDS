package org.endeavourhealth.transform.fhir.schema;

import org.endeavourhealth.transform.fhir.FhirValueSetUri;

public enum ProblemSignificance {

    SIGNIFICANT("386134007", "Significant"),
    NOT_SIGNIFICANT("371928007", "Not significant"),
    UNSPECIIED("394847000", "Unspecified significance");

    private String code = null;
    private String description = null;

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getSystem() {
        return FhirValueSetUri.VALUE_SET_PROBLEM_SIGNIFICANCE;
    }

    ProblemSignificance(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
