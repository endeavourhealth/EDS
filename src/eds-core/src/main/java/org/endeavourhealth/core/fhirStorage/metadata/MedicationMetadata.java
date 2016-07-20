package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.Medication;

public class MedicationMetadata extends AbstractResourceMetadata {
    public MedicationMetadata(Medication resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(Medication resource) {
    }
}
