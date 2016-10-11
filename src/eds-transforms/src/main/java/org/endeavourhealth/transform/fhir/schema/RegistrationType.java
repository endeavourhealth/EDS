package org.endeavourhealth.transform.fhir.schema;

import org.endeavourhealth.transform.fhir.FhirValueSetUri;

public enum RegistrationType {

    EMERGENCY("E", "Emergency"),
    IMMEDIATELY_NECESSARY("IN", "Immediately Necessary"),
    REGULAR_GMS("R", "Regular/GMS"),
    TEMPORARY("T", "Temporary"),
    PRIVATE("P", "Private"),
    OTHER("O", "Other"),
    DUMMY("D", "Dummy/Synthetic");

    private String code = null;
    private String description = null;

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getSystem() {
        return FhirValueSetUri.VALUE_SET_REGISTRATION_TYPE;
    }

    RegistrationType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static RegistrationType fromCode(String v) {
        for (RegistrationType c: RegistrationType.values()) {
            if (c.getCode().equalsIgnoreCase(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
