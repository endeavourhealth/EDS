package org.endeavourhealth.core.data.admin.models;

public enum DefinitionItemType {
    ReportFolder(0),
    Report(1),
    Query(2),
    Test(3),
    DataSource(4),
    CodeSet(5),
    ListOutput(6),
    LibraryFolder(7),
    Protocol(8),
    System(9);

    private final int value;

    DefinitionItemType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DefinitionItemType get(int value) {
        for (DefinitionItemType e : DefinitionItemType.values()) {
            if (e.value == value) {
                return e;
            }
        }
        throw new RuntimeException("No DefinitionItemType " + value);
    }
}
