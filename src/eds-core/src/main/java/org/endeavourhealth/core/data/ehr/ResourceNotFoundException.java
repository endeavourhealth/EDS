package org.endeavourhealth.core.data.ehr;

import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class ResourceNotFoundException extends Exception {

    private String resourceType = null;
    private UUID resourceId = null;

    public ResourceNotFoundException(String resourceType, UUID resourceId) {
        this(resourceType, resourceId, null);
    }

    public ResourceNotFoundException(ResourceType resourceType, UUID resourceId) {
        this(resourceType, resourceId, null);
    }

    public ResourceNotFoundException(String resourceType, UUID resourceId, Throwable cause) {
        super("Failed to retrieve " + resourceType + " for ID " + resourceId, cause);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public ResourceNotFoundException(ResourceType resourceType, UUID resourceId, Throwable cause) {
        super("Failed to retrieve " + resourceType + " for ID " + resourceId, cause);
        this.resourceType = resourceType.toString();
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }
}
