package org.endeavourhealth.ui.database;

public enum TableSaveMode {
    INSERT(1),
    UPDATE(2),
    DELETE(3);

    private final int value;

    TableSaveMode(int value) {
        this.value = value;
    }
}
