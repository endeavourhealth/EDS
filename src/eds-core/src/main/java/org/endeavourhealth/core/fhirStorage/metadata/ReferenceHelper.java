package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.ResourceType;

public class ReferenceHelper {
    public static String getReferenceId(Reference reference) {
        return getReferenceId(reference, null);
    }

    public static String getReferenceId(Reference reference, ResourceType resourceType)
    {
        if (reference == null)
            return null;

        String[] parts = reference.getReference().split("\\/");

        if ((parts == null) || (parts.length == 0))
            return null;

        if (parts.length != 2)
            throw new IllegalArgumentException("Invalid reference string.");

        if (resourceType != null) {
            if (!parts[0].equals(resourceType.toString())) {
                return null;
            }
        }

        return parts[1];
    }

    public static ResourceType getReferenceType(Reference reference) {
        if (reference == null)
            return null;

        String[] parts = reference.getReference().split("\\/");
        String resourceTypeStr = parts[0];
        return ResourceType.valueOf(resourceTypeStr);
    }
}
