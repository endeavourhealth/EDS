package org.endeavourhealth.transform.fhir.schema;

import org.endeavourhealth.transform.fhir.FhirValueSetUri;

public enum ReferralPriority {

    ROUTINE("1", "Routine"),
    URGENT("2", "Urgent"),
    TWO_WEEK_WAIT("3", "Two Week Wait"),
    SOON("4", "Soon");

    private String code = null;
    private String description = null;

    public String getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }
    public String getSystem() {
        return FhirValueSetUri.VALUE_SET_REFERRAL_PRIORITY;
    }

    ReferralPriority(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ReferralPriority fromCode(String v) {
        for (ReferralPriority c: ReferralPriority.values()) {
            if (c.getCode().equalsIgnoreCase(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
