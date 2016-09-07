package org.endeavourhealth.transform.emis.csv.schema.careRecord;

public enum ObservationType {

    VALUE("Value"),
    OBSERVATION("Observation"),
    INVESTIGATION("Investigation"),
    ALLERGY("Allergy"),
    DOCUMENT("Document"),
    TEST_REQUEST("Test Request"),
    REFERRAL("Referral"),
    IMMUNISATION("Immunisation"),
    FAMILY_HISTORY("Family history"),
    ANNOTATED_IMAGE("Annotated Image");


    private String value;

    ObservationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ObservationType fromValue(String v) {
        for (ObservationType c: ObservationType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
