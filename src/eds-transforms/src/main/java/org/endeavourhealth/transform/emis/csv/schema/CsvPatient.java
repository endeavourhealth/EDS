package org.endeavourhealth.transform.emis.csv.schema;

public enum CsvPatient {

    ID(0),
    CARERECORDID(1),
    PracticeCode(2),
    AGE(3),
    DATEOFBIRTH(4),
    SEX(5),
    REGDATE(6),
    REGSTATUS(7),
    DEREGDATE(8),
    DEREGREASON(9),
    DATEOFDEATH(10),
    MOSAICCODE(11),
    NHSNUMBER(12),
    POSTCODE(13),
    YYYYMMOFBIRTH(14),
    REGGP(15),
    USUALGP(16),
    FORENAME(17),
    SURNAME(18);

    private final int value;

    CsvPatient(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static CsvPatient get(int value) {
        for (CsvPatient e : CsvPatient.values()) {
            if (e.value == value) {
                return e;
            }
        }
        throw new RuntimeException("No CsvPatient " + value);
    }
}
