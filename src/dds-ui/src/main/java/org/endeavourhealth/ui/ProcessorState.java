package org.endeavourhealth.ui;

public enum ProcessorState {
    Idle(1),
    Starting(2),
    Running(3),
    Stopping(4);

    private final int value;

    ProcessorState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ProcessorState get(int value) {
        for (ProcessorState e : ProcessorState.values()) {
            if (e.value == value) {
                return e;
            }
        }
        throw new RuntimeException("No ProcessorState " + value);
    }

}
