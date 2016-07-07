package org.endeavourhealth.transform.fhir;

import org.hl7.fhir.instance.model.ResourceType;

public class ReferenceComponents {
    private ResourceType resourceType = null;
    private String id = null;

    public ReferenceComponents(ResourceType resourceType, String id) {
        this.resourceType = resourceType;
        this.id = id;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public String getId() {
        return id;
    }
}
