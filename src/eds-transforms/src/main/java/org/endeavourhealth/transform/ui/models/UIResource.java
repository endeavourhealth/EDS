package org.endeavourhealth.transform.ui.models;

public abstract class UIResource {
    private String id;

    public String getId() {
        return id;
    }

    public UIResource setId(String id) {
        this.id = id;
        return this;
    }
}
