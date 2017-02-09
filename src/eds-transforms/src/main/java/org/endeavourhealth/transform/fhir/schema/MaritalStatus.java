package org.endeavourhealth.transform.fhir.schema;

import org.endeavourhealth.transform.fhir.FhirValueSetUri;

public enum MaritalStatus {

    ANNULLED("A", "Annulled"),
    DIVORCED("D", "Divorced"),
    INTERLOCUTARY("I", "Interlocutory"),
    LEGALLY_SEPARATED("L", "Legally Separated"),
    MARRIED("M", "Married"),
    POLYGAMOUS("P", "Polygamous"),
    NEVER_MARRIED("S", "Never Married"),
    DOMESTIC_PARTNER("T", "Domestic Partner"),
    WIDOWED("W", "Widowed");

    private String code = null;
    private String description = null;

    public String getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }
    public String getSystem() {
        return FhirValueSetUri.VALUE_SET_MARITAL_STATUS;
    }

    MaritalStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static MaritalStatus fromCode(String v) {
        for (MaritalStatus c: MaritalStatus.values()) {
            if (c.code.equalsIgnoreCase(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
