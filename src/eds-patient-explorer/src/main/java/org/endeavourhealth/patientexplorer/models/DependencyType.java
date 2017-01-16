package org.endeavourhealth.patientexplorer.models;

public enum DependencyType {
    IsChildOf(0),
    IsContainedWithin(1),
    Uses(2);

    private final int value;

    DependencyType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DependencyType get(int value) {
        for (DependencyType e : DependencyType.values()) {
            if (e.value == value) {
                return e;
            }
        }
        throw new RuntimeException("No DependencyType " + value);
    }
}
