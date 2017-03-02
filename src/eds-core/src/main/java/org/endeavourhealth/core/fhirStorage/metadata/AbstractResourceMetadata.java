package org.endeavourhealth.core.fhirStorage.metadata;

import org.endeavourhealth.core.fhirStorage.FhirResourceHelper;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;

public abstract class AbstractResourceMetadata implements ResourceMetadata {
    private List<String> profiles;

    @Override
    public List<String> getProfiles() {
        return profiles;
    }

    public AbstractResourceMetadata() {
    }

    protected AbstractResourceMetadata(Resource resource) {
        this.profiles = FhirResourceHelper.getProfiles(resource);
    }
}
