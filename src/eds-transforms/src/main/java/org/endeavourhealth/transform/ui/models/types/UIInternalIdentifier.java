package org.endeavourhealth.transform.ui.models.types;

import java.util.Objects;
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

    @Override
    public boolean equals(Object other) {
        if (other instanceof UIInternalIdentifier) {
            return (uuidValueEquals(this.serviceId, ((UIInternalIdentifier)other).serviceId)
                    && uuidValueEquals(this.systemId, ((UIInternalIdentifier)other).systemId)
                    && uuidValueEquals(this.resourceId, ((UIInternalIdentifier)other).resourceId));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.serviceId) ^ Objects.hashCode(this.systemId) ^ Objects.hashCode(this.resourceId);
    }

    private static boolean uuidValueEquals(UUID one, UUID two) {
        if ((one == null) && (two == null))
            return true;
        if ((one == null) || (two == null))
            return false;
        return one.equals(two);
    }
}
