package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.Meta;
import org.hl7.fhir.instance.model.PrimitiveType;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractResourceMetadata implements ResourceMetadata {
    private UUID id;
    private String resourceTypeName;
    private List<String> profiles;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getResourceTypeName() {
        return resourceTypeName;
    }

    @Override
    public List<String> getProfiles() {
        return profiles;
    }

    public AbstractResourceMetadata() {
    }

    protected AbstractResourceMetadata(Resource resource) {
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(Resource resource) {
        id = UUID.fromString(resource.getId());
        resourceTypeName = resource.getResourceType().toString();
        Meta meta = resource.getMeta();
        profiles = meta.getProfile()
                .stream()
                .map(PrimitiveType::toString)
                .collect(Collectors.toList());
    }
}
