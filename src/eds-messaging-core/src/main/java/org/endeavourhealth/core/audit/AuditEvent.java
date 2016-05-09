package org.endeavourhealth.core.audit;

public enum AuditEvent {
    RECEIVE(1),
    VALIDATE(2),
    SEND(3);

    private final int value;

    AuditEvent(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
