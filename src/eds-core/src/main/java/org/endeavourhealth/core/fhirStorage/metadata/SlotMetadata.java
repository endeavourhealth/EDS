package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.Slot;

public class SlotMetadata extends AbstractResourceMetadata {
    public SlotMetadata(Slot resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(Slot resource) {
    }
}
