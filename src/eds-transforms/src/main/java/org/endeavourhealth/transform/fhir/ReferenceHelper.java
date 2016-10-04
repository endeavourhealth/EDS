package org.endeavourhealth.transform.fhir;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.List;

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


    public static Reference createReferenceExternal(Resource resource) throws TransformException {
        return createReference(resource.getResourceType(), resource.getId());
    }

    public static Reference createReferenceInline(Resource resource) throws TransformException {
        return new Reference(resource);
    }



    public static String getReferenceId(Reference reference) {
        return getReferenceId(reference, null);
    }
    public static String getReferenceId(Reference reference, ResourceType resourceType)
    {
        ReferenceComponents referenceComponents = getReferenceComponents(reference);

        if (referenceComponents == null)
            return null;

        if (!referenceComponents.getResourceType().equals(resourceType))
            return null;

        return referenceComponents.getId();
    }

    public static ReferenceComponents getReferenceComponents(Reference reference) {
        if (reference == null)
            return null;

        String[] parts = reference.getReference().split("\\/");

        if ((parts == null) || (parts.length == 0))
            return null;

        if (parts.length != 2)
            throw new IllegalArgumentException("Invalid reference string.");

        ResourceType resourceType = ResourceType.valueOf(parts[0]);
        return new ReferenceComponents(resourceType, parts[1]);
    }

    public static boolean isResourceType(Reference reference, ResourceType resourceType) {
        ReferenceComponents referenceComponents = getReferenceComponents(reference);

        if (referenceComponents == null)
            return false;

        return referenceComponents.getResourceType().equals(resourceType);
    }

    public static ResourceType getResourceType(Reference reference) {
        ReferenceComponents comps = getReferenceComponents(reference);
        return comps.getResourceType();
    }

    public static <T extends Resource> Reference findAndCreateReference(Class<T> type, List<Resource> resources) throws TransformException {
        T resource = ResourceHelper.findResourceOfType(type, resources);
        if (resource != null) {
            return createReferenceExternal(resource);
        } else {
            return null;
        }
    }


}
