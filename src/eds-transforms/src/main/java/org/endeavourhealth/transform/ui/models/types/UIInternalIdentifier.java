package org.endeavourhealth.transform.ui.models.types;

import java.util.UUID;

public class UIInternalIdentifier {
    private UUID serviceId;
    private UUID systemId;
    private UUID resourceId;

    public UUID getServiceId() {
        return serviceId;
    }

    public UIInternalIdentifier setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public UUID getSystemId() {
        return systemId;
    }

    public UIInternalIdentifier setSystemId(UUID systemId) {
        this.systemId = systemId;
        return this;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public UIInternalIdentifier setResourceId(UUID resourceId) {
        this.resourceId = resourceId;
        return this;
    }
}
