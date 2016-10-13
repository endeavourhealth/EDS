package org.endeavourhealth.transform.fhir.schema;

import org.endeavourhealth.transform.fhir.FhirValueSetUri;

public enum EthnicCategory {

    WHITE_BRITISH("A", "British"),
    WHITE_IRISH("B", "Irish"),
    OTHER_WHITE("C", "Any other White background"),
    MIXED_CARIBBEAN("D", "White and Black Caribbean"),
    MIXED_AFRICAN("E", "White and Black African"),
    MIXED_ASIAN("F", "White and Asian"),
    OTHER_MIXED("G", "Any other mixed background"),
    ASIAN_INDIAN("H", "Indian"),
    ASIAN_PAKISTANI("J", "Pakistani"),
    ASIAN_BANGLADESHI("K", "Bangladeshi"),
    OTHER_ASIAN("L", "Any other Asian background"),
    BLACK_CARIBBEAN("M", "Caribbean"),
    BLACK_AFRICAN("N", "African"),
    OTHER_BLACK("P", "Any other Black background"),
    CHINESE("R", "Chinese"),
    OTHER("S", "Any other ethnic group"),
    NOT_STATED("Z", "Not stated");

    private String code = null;
    private String description = null;

    public String getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }
    public String getSystem() {
        return FhirValueSetUri.VALUE_SET_ETHNIC_CATEGORY;
    }

    EthnicCategory(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
