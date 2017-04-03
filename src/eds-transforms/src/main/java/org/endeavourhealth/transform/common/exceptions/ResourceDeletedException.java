package org.endeavourhealth.transform.common.exceptions;

import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class ResourceDeletedException extends TransformException {

    private ResourceType resourceType = null;
    private UUID resourceId = null;

    public ResourceDeletedException(ResourceType resourceType, UUID resourceId) {
        this(resourceType, resourceId, null);
    }
    public ResourceDeletedException(ResourceType resourceType, UUID resourceId, Throwable cause) {
        super("Trying to use deleted " + resourceType + " with ID " + resourceId, cause);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }
}
