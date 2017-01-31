package org.endeavourhealth.hl7test.transforms.framework;

public class Field {
    private String field;
    private Seperators seperators;

    public Field(String field, Seperators seperators) {
        this.field = field;
        this.seperators = seperators;
    }
}
