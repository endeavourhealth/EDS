package org.endeavourhealth.core.fhirStorage.metadata;

import org.hl7.fhir.instance.model.Organization;

public class OrganizationMetadata extends AbstractResourceMetadata {
    public OrganizationMetadata(Organization resource) {
        super(resource);
        populateMetadataFromResource(resource);
    }

    private void populateMetadataFromResource(Organization resource) {
    }
}
