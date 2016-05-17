package org.endeavourhealth.transform.emis.csv.schema;

public enum CsvPrescription {

    ID(0),
    CARERECORDID(1),
    PracticeCode(2),
    ISSUEDATE(3),
    ISSUETYPE(4),
    READCODE(5),
    DRUG(6),
    SUPPLY(7),
    EMISCODE(8),
    STATUS(9),
    LAST_ISSUE_DATE(10),
    DIRECTIONS(11);

    private final int value;

    CsvPrescription(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static CsvPrescription get(int value) {
        for (CsvPrescription e : CsvPrescription.values()) {
            if (e.value == value) {
                return e;
            }
        }
        throw new RuntimeException("No CsvPrescription " + value);
    }
}
