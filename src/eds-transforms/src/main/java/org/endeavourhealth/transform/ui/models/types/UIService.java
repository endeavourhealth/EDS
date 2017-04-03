package org.endeavourhealth.transform.ui.models.types;

import java.util.UUID;

public class UIService {
    private UUID serviceId;
    private UUID systemId;
    private String name;
    private String localIdentifier;

    public UUID getServiceId() {
        return serviceId;
    }

    public UIService setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public UUID getSystemId() {
        return systemId;
    }

    public UIService setSystemId(UUID systemId) {
        this.systemId = systemId;
        return this;
    }

    public String getName() {
        return name;
    }

    public UIService setName(String name) {
        this.name = name;
        return this;
    }

    public String getLocalIdentifier() {
        return localIdentifier;
    }

    public UIService setLocalIdentifier(String localIdentifier) {
        this.localIdentifier = localIdentifier;
        return this;
    }
}
