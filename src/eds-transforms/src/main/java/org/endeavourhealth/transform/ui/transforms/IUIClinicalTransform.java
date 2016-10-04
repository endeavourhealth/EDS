package org.endeavourhealth.transform.ui.transforms;

import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;

public interface IUIClinicalTransform<T extends Resource, U extends UIResource> {
    List<U> transform(List<T> resources, ReferencedResources referencedResources);
    List<Reference> getReferences(List<T> resources);
}
