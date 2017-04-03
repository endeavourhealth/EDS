package org.endeavourhealth.transform.common.exceptions;

import org.hl7.fhir.instance.model.ResourceType;

import java.util.UUID;

public class UnknownResourceException extends TransformException {

    public UnknownResourceException(ResourceType referencedResourceType, String referencedId, ResourceType resourceType, String resourceId, UUID serviceId, UUID systemId) {
        super("Reference to unrecognised " + referencedResourceType + " " + referencedId + " in " + resourceType + " " + resourceId + " for service " + serviceId + " and system " + systemId);
    }
}
