package org.endeavourhealth.transform.fhir.schema;

import org.endeavourhealth.transform.fhir.FhirValueSetUri;

public enum ReferralType {

    UNKNOWN("U", "Unknown"),
    ASSESSMENT("A", "Assessment"),
    INVESTIGATION("I", "Investigation"),
    MANAGEMENT_ADVICE("M", "Management advice"),
    PATIENT_REASSURANCE("R", "Patient reassurance"),
    SELF_REFERRAL("S", "Self referral"),
    TREATMENT("T", "Treatment");

    private String code = null;
    private String description = null;

    public String getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }
    public String getSystem() {
        return FhirValueSetUri.VALUE_SET_REFERRAL_TYPE;
    }

    ReferralType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ReferralType fromCode(String v) {
        for (ReferralType c: ReferralType.values()) {
            if (c.getCode().equalsIgnoreCase(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
