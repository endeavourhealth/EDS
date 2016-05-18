package org.endeavourhealth.transform.emis.csv.schema;

public enum CsvDrugStatus {

    /**
     * Active
     *
     */
    A,

    /**
     * Cancelled
     *
     */
    C,

    /**
     * Never Active
     *
     */
    N;

    public String value() {
        return name();
    }

    public static CsvDrugStatus fromValue(String v) {
        return valueOf(v);
    }
}
