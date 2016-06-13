package org.endeavourhealth.transform.fhir.schema;

public enum RegistrationType {

    E("Emergency"),
    IN("Immediately Necessary"),
    R("Regular/GMS"),
    T("Temporary"),
    P("Private"),
    O("Other"),
    D("Dummy/Synthetic");

    private String value = null;

    public String getValue() {
        return value;
    }

    RegistrationType(String value) {
        this.value = value;
    }
}
