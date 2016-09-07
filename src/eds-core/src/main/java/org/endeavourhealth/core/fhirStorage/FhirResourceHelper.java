package org.endeavourhealth.core.fhirStorage;

import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.PrimitiveType;
import org.hl7.fhir.instance.model.Resource;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FhirResourceHelper {
    public static UUID getResourceId(Resource resource) {
        return UUID.fromString(resource.getId());
    }

    public static String getResourceType(Resource resource) {
        return resource.getResourceType().toString();
    }

    public static Date getLastUpdated(Resource resource) {
        Meta meta = resource.getMeta();
        return meta.getLastUpdated();
    }

    public static List<String> getProfiles(Resource resource) {
        Meta meta = resource.getMeta();

        if (!meta.hasProfile()) {
            return null;
        }

        return meta.getProfile()
                .stream()
                .map(PrimitiveType::toString)
                .collect(Collectors.toList());
    }

    public static void updateMetaTags(Resource resource, UUID version, Date createdAt) {
        Meta meta = resource.getMeta();
        meta.setVersionId(version.toString());
        meta.setLastUpdated(createdAt);
    }
}
