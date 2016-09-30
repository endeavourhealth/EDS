package org.endeavourhealth.transform.ui.transforms;

import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.hl7.fhir.instance.model.Reference;

import java.util.List;

public interface IUIClinicalTransform<T, U> {
    List<U> transform(List<T> resources, ReferencedResources referencedResources);
    List<Reference> getReferences(List<T> resources);
}
