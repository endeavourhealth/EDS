package org.endeavourhealth.transform.fhir;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.common.TransformException;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public static Reference createReference(Resource resource) throws TransformException {
        return createReference(resource.getResourceType(), resource.getId());
    }

    /*public static Reference createPractitionerReference(String id) throws TransformException {
        return createReference(ResourceType.Practitioner, id);
    }
    public static Reference createEncounterReference(String id) throws TransformException {
        return createReference(ResourceType.Encounter, id);
    }
    public static Reference createPatientReference(String id) throws TransformException {
        return createReference(ResourceType.Patient, id);
    }
    public static Reference createLocationReference(String id) throws TransformException {
        return createReference(ResourceType.Location, id);
    }*/




    public static <T extends Resource> Reference createReference(Class<T> type, List<Resource> resources) throws TransformException {
        T resource = ResourceHelper.findResourceOfType(type, resources);
        if (resource != null) {
            return createReference(resource);
        } else {
            return null;
        }
    }


}
