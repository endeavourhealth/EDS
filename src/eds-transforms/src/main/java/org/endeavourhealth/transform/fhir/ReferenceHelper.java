package org.endeavourhealth.transform.fhir;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.common.TransformException;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.ResourceType;

public class ReferenceHelper
{
    public static String createResourceReference(ResourceType resourceType, String id)
    {
        return resourceType.toString() + "/" + id;
    }

    public static Reference createReference(ResourceType resourceType, String id) throws TransformException
    {
        if (StringUtils.isBlank(id))
            throw new TransformException("Blank id when creating reference for " + resourceType.toString());

        return new Reference().setReference(createResourceReference(resourceType, id));
    }

    public static Reference createInternalReference(String id)
    {
        return new Reference().setReference("#" + id);
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

        if (!parts[0].equals(resourceType.toString()))
            return null;

        return parts[1];
    }

    public static Boolean referenceEquals(Reference reference, ResourceType resourceType, String id)
    {
        if (StringUtils.isBlank(id))
            return false;

        String referenceId = getReferenceId(reference, resourceType);

        return id.equals(referenceId);
    }
}
