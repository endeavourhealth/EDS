package org.endeavourhealth.core.fhirStorage;

import org.endeavourhealth.core.fhirStorage.exceptions.UnprocessableEntityException;
import org.endeavourhealth.core.fhirStorage.exceptions.VersionConflictException;
import org.apache.commons.lang3.StringUtils;
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
}
