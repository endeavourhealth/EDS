package org.endeavourhealth.transform.fhir.schema;

import org.endeavourhealth.transform.fhir.FhirUri;

public enum ContactRelationship {

    //defined at http://hl7.org/fhir/valueset-patient-contact-relationship.html
    EMERGENCY("emergency", "Emergency"),
    FAMIL ("family", "Family"),
    GUARDIAN("guardian", "Guardian"),
    FRIEND("friend", "Friend"),
    PARTNER("partner", "Partner"),
    WORK("work", "Work"),
    CAREGIVER("caregiver", "Caregiver"),
    AGENT("agent", "Agent"),
    GUARANTOR("guarantor", "Guarantor"),
    OWNER("owner", "Owner of animal"),
    PARENT("parent", "Parent");

    private String code = null;
    private String description = null;

    public String getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }
    public String getSystem() {
        return FhirUri.VALUE_SET_CONTACT_RELATIONSHIP;
    }

    ContactRelationship(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ContactRelationship fromCode(String v) {
        for (ContactRelationship c: ContactRelationship.values()) {
            if (c.code.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
