package org.endeavourhealth.transform.fhir.schema;

public enum ContactRelationship {

    //defined at http://hl7.org/fhir/valueset-patient-contact-relationship.html
    emergency("Emergency"),
    family ("Family"),
    guardian("Guardian"),
    friend("Friend"),
    partner("Partner"),
    work("Work"),
    caregiver("Caregiver"),
    agent("Agent"),
    guarantor("Guarantor"),
    owner("Owner of animal"),
    parent("Parent");

    private String value = null;

    public String getValue() {
        return value;
    }

    ContactRelationship(String value) {
        this.value = value;
    }

    public static ContactRelationship fromValue(String v) {
        for (ContactRelationship c: ContactRelationship.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
