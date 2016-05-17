package org.endeavourhealth.transform.emis.csv.schema;

public enum CsvMetadata {

    AUDITDATE(0),
    RUNDATE(1),
    PATIENTCOUNT(2),
    EVENTCOUNT(3),
    PRESCRIPTIONCOUNT(4);

    private final int value;

    CsvMetadata(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static CsvMetadata get(int value) {
        for (CsvMetadata e : CsvMetadata.values()) {
            if (e.value == value) {
                return e;
            }
        }
        throw new RuntimeException("No CsvMetadata " + value);
    }
}
