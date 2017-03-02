package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.Practitioner;

public class PractitionerMetadata extends AbstractResourceMetadata {
    public PractitionerMetadata(Practitioner resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(Practitioner resource) {
    }
}
