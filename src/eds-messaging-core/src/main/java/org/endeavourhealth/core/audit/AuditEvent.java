package org.endeavourhealth.core.audit;

public enum AuditEvent {
    RECEIVE(1),
    VALIDATE(2),
    TRANSFORM_START(3),
    TRANSFORM_END(4),
    SEND(5);

    private final int value;

    AuditEvent(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AuditEvent fromString(String v) {
        for (AuditEvent c: AuditEvent.values()) {
            if (c.toString().equalsIgnoreCase(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public static AuditEvent fromValue(int value) {
        for (AuditEvent c: AuditEvent.values()) {
            if (c.value == value) {
                return c;
            }
        }
        throw new IllegalArgumentException("" + value);
    }
}
