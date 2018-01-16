package org.endeavourhealth.ui.json;

import java.util.UUID;

public class JsonProtocol {
    private UUID id = null;
    private String name = null;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
