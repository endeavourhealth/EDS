package org.endeavourhealth.transform.ui.transforms;

import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.resources.UIObservation;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Reference;

import java.util.List;

public class UIObservationTransform implements IUIClinicalTransform<Observation,UIObservation> {

    @Override
    public List<UIObservation> transform(List<Observation> resources, ReferencedResources referencedResources) {
        return null;
    }

    @Override
    public List<Reference> getReferences(List<Observation> resources) {
        return null;
    }
}
