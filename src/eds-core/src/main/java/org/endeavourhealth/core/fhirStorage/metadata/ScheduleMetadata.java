package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.Schedule;

public class ScheduleMetadata extends AbstractResourceMetadata {
    public ScheduleMetadata(Schedule resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(Schedule resource) {
    }
}
