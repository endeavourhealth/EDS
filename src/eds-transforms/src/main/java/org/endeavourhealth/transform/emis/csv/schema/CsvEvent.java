package org.endeavourhealth.transform.emis.csv.schema;

public enum CsvEvent {


    ID(0),
    CARERECORDID(1),
    PracticeCode(2),
    ODATE(3),
    READCODE(4),
    READTERM(5),
    NUMRESULT(6),
    NUMRESULT2(7);

    private final int value;

    CsvEvent(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static CsvEvent get(int value) {
        for (CsvEvent e : CsvEvent.values()) {
            if (e.value == value) {
                return e;
            }
        }
        throw new RuntimeException("No CsvEvent " + value);
    }
}
