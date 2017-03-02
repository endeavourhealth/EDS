package org.endeavourhealth.core.fhirStorage;

import org.hl7.fhir.instance.model.Resource;

import java.util.ArrayList;
import java.util.List;

public class FhirResponse {
    private List<Resource> resources;

    public List<Resource> getResources() {
        return resources;
    }

    public FhirResponse(Resource resource) {
        this.resources = new ArrayList<>();
        this.resources.add(resource);
    }

    public FhirResponse(List<Resource> resources) {
        this.resources = resources;
    }
}
