package org.endeavourhealth.transform.emis.csv.schema;

public enum CsvSex {

    /**
     * Unknown
     *
     */
    U,

    /**
     * Male
     *
     */
    M,

    /**
     * Female
     *
     */
    F,

    /**
     * Indeterminate
     *
     */
    I;

    public String value() {
        return name();
    }

    public static CsvSex fromValue(String v) {
        return valueOf(v);
    }
}
