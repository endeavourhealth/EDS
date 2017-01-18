package org.endeavourhealth.hl7receiver.model.db;

public enum DbNotificationStatus {
    NOT_ATTEMPTED(1),
    FAILED(-1),
    SUCCEEDED(9);

    private int value;

    DbNotificationStatus(int value) {
        this.value = value;
    }

    public static DbNotificationStatus fromValue(int value) {
        for (DbNotificationStatus notificationStatus: DbNotificationStatus.values())
            if (notificationStatus.intValue() == value)
                return notificationStatus;

        throw new IllegalArgumentException(Integer.toString(value));
    }

    public int intValue() {
        return this.value;
    }
}
