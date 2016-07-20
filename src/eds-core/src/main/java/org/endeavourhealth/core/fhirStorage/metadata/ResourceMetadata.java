package org.endeavourhealth.core.fhirStorage.metadata;

import java.util.List;
import java.util.UUID;

public interface ResourceMetadata {
    UUID getId();
    String getResourceTypeName();
    List<String> getProfiles();
}
