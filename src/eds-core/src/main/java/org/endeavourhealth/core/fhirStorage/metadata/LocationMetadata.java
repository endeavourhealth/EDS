package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.Location;

public class LocationMetadata extends AbstractResourceMetadata {
    public LocationMetadata(Location resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(Location resource) {
    }
}
