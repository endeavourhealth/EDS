package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.Substance;

public class SubstanceMetadata extends AbstractResourceMetadata {
    public SubstanceMetadata(Substance resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(Substance resource) {
    }
}
