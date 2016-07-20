package org.endeavourhealth.core.fhirStorage;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.core.fhirStorage.exceptions.FhirStorageException;
import org.endeavourhealth.core.fhirStorage.exceptions.UnprocessableEntityException;
import org.endeavourhealth.core.fhirStorage.exceptions.VersionConflictException;
import org.hl7.fhir.instance.model.Resource;

import java.util.UUID;

public class Validate {
    public static void resourceId(Resource resource) throws UnprocessableEntityException {
        if (StringUtils.isBlank(resource.getId())) {
            throw new UnprocessableEntityException("Resource ID is empty");
        }
    }

    public static void isSameVersion(UUID original, UUID replacement) throws VersionConflictException {
        if (!original.equals(replacement))
            throw new VersionConflictException(original.toString(), replacement.toString());
    }

    public static void hasVersion(UUID versionkey) throws UnprocessableEntityException {
        if (versionkey == null)
            throw new IllegalArgumentException("versionkey id null");
    }

/*
    public static void key(Key key) throws FhirStorageException {
        if (key.hasResourceId()) {
            Validate.resourceId(key.getResourceId());
        }
        if (key.hasVersionId()) {
            Validate.versionId(key.getVersionId());
        }
        if (key.hasResourceTypeName()) {
            Validate.resourceTypeName(key.getResourceTypeName());
        }
    }

    public static void resourceId(String resourceId) throws FhirStorageException {
        if (StringUtils.isBlank(resourceId)) {
            throw new FhirStorageException("Resource ID is empty");
        }

        IdType id = new IdType(resourceId);
        if (!id.isIdPartValid()) {
            throw new FhirStorageException(resourceId + " is not a valid value for an id");
        }
    }

    public static void versionId(String versionId) throws FhirStorageException {
        if (StringUtils.isBlank(versionId)) {
            throw new FhirStorageException("Version ID is empty.");
        }
    }
*/
    public static void resourceTypeName(String name) throws FhirStorageException {
        /*
        ResourceType.valueOf()
        if (ModelInfo.SupportedResources.Contains(name))
            return;

        //  Test for the most common mistake first: wrong casing of the resource name
        var correct = ModelInfo.SupportedResources.FirstOrDefault(s => s.ToUpperInvariant() == name.ToUpperInvariant());
        if (correct != null)
        {
            throw Error.NotFound("Wrong casing of collection name, try '{0}' instead", correct);
        }
        else
        {
            throw Error.NotFound("Unknown resource collection '{0}'", name);
        }
        */
    }

}
