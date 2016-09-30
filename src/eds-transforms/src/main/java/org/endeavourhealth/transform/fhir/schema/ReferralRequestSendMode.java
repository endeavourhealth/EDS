package org.endeavourhealth.transform.fhir.schema;

import org.endeavourhealth.transform.fhir.FhirValueSetUri;

public enum ReferralRequestSendMode {

    //defined at http://endeavourhealth.org/fhir/ValueSet/primarycare-referral-request-mode
    ACUTE("T", "Telephone"),
    REPEAT("W", "Written"),
    REPEAT_DISPENSING("V", "Verbal"),
    AUTOMATIC("C", "Choose and Book");

    private String code = null;
    private String description = null;

    public String getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }
    public String getSystem() {
        return FhirValueSetUri.VALUE_SET_REFERRAL_REQUEST_SEND_MODE;
    }


    ReferralRequestSendMode(String code, String description) {
        this.code = code;
        this.description = description;
    }


    public static ReferralRequestSendMode fromCode(String v) {
        for (ReferralRequestSendMode c: ReferralRequestSendMode.values()) {
            if (c.getCode().equalsIgnoreCase(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public static ReferralRequestSendMode fromDescription(String v) {
        for (ReferralRequestSendMode c: ReferralRequestSendMode.values()) {
            if (c.getDescription().equalsIgnoreCase(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
