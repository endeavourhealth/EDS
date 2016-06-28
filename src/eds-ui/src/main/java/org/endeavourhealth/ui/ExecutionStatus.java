package org.endeavourhealth.ui;

public enum ExecutionStatus {
    Executing(0),
    Succeeded(1),
    Failed(2),
    NoJobRequests(3),
    Cancelled(4);

    private final int value;

    ExecutionStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ExecutionStatus get(int value) {
        for (ExecutionStatus e : ExecutionStatus.values()) {
            if (e.value == value) {
                return e;
            }
        }
        throw new RuntimeException("No ExectionStatus " + value);
    }
}
