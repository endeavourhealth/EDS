package org.endeavourhealth.transform.ui.transforms.clinical;

import org.endeavourhealth.core.utility.StreamExtension;
import org.endeavourhealth.transform.common.exceptions.TransformRuntimeException;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.endeavourhealth.transform.ui.helpers.CodeHelper;
import org.endeavourhealth.transform.ui.helpers.DateHelper;
import org.endeavourhealth.transform.ui.helpers.QuantityHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIObservation;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPractitioner;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIObservationRelation;
import org.endeavourhealth.transform.ui.models.types.UIDate;
import org.endeavourhealth.transform.ui.models.types.UIQuantity;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.ResourceType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UIObservationTransform extends UIClinicalTransform<Observation, UIObservation> {

    @Override
    public List<UIObservation> transform(List<Observation> resources, ReferencedResources referencedResources) {
        return resources
                .stream()
                .map(t -> transform(t, referencedResources))
                .collect(Collectors.toList());
    }

    public static UIObservation transform(Observation observation, ReferencedResources referencedResources) {
        try {
            return new UIObservation()
                    .setId(observation.getId())
                    .setCode(CodeHelper.convert(observation.getCode()))
                    .setEffectivePractitioner(getPerformer(observation, referencedResources))
                    .setEffectiveDate(getEffectiveDateTime(observation))
                    .setRecordingPractitioner(getRecordedByExtensionValue(observation, referencedResources))
                    .setRecordedDate(getRecordedDateExtensionValue(observation))
                    .setNotes(observation.getComments())
                    .setStatus(getStatus(observation))
                    .setValue(getValue(observation))
                    .setReferenceRangeHigh(getReferenceRangeHigh(observation))
                    .setReferenceRangeLow(getReferenceRangeLow(observation))
                    .setRelated(getRelated(observation, referencedResources));

//        private String status;
//        private Date effectiveDateTime;
//        private UICodeableConcept code;
//        private Practitioner performer;
//        private UIQuantity value;
//        private UIQuantity referenceRangeLow;
//        private UIQuantity referenceRangeHigh;
//        private String comments;
//        private Encounter encounter;            *
        } catch (Exception e) {
            throw new TransformRuntimeException(e);
        }
    }

    private static UIQuantity getReferenceRangeHigh(Observation observation) {
        if (!observation.hasReferenceRange())
            return null;

        Observation.ObservationReferenceRangeComponent referenceRangeComponent = observation.getReferenceRange().get(0);

        if (!referenceRangeComponent.hasHigh())
            return null;

        return QuantityHelper.convert(referenceRangeComponent.getHigh());
    }

    private static UIQuantity getReferenceRangeLow(Observation observation) {
        if (!observation.hasReferenceRange())
            return null;

        Observation.ObservationReferenceRangeComponent referenceRangeComponent = observation.getReferenceRange().get(0);

        if (!referenceRangeComponent.hasLow())
            return null;

        return QuantityHelper.convert(referenceRangeComponent.getLow());
    }

    private static UIQuantity getValue(Observation observation) throws Exception {
        if (!observation.hasValueQuantity())
            return null;

        return QuantityHelper.convert(observation.getValueQuantity());
    }

    private static UIPractitioner getPerformer(Observation observation, ReferencedResources referencedResources) {
        Reference reference = observation
                .getPerformer()
                .stream()
                .filter(t -> ReferenceHelper.isResourceType(t, ResourceType.Practitioner))
                .collect(StreamExtension.firstOrNullCollector());

        return referencedResources.getUIPractitioner(reference);
    }

    private static String getStatus(Observation observation) {
        if (observation.getStatus() == null)
            return null;

        return observation.getStatus().toCode();
    }

    private static UIDate getEffectiveDateTime(Observation observation) throws Exception {
        if (!observation.hasEffectiveDateTimeType())
            return null;

        return DateHelper.convert(observation.getEffectiveDateTimeType());
    }

    private static List<UIObservationRelation> getRelated(Observation observation, ReferencedResources referencedResources) {
        if (!observation.hasRelated())
            return null;

        List<UIObservationRelation> related = new ArrayList<>();
        for(Observation.ObservationRelatedComponent relatedComponent : observation.getRelated()) {
            related.add(new UIObservationRelation()
            .setType(relatedComponent.getType().toCode())
            .setTarget(referencedResources.getUIObservation(relatedComponent.getTarget()))
            );
        }

        return related;
    }

    @Override
    public List<Reference> getReferences(List<Observation> resources) {
        return StreamExtension.concat(
                resources
                        .stream()
                        .filter(t -> t.hasSubject())
                        .map(t -> t.getSubject()),
                resources
                        .stream()
                        .filter(t -> t.hasRelated())
                        .flatMap(t -> t.getRelated().stream())
                        .map(t -> t.getTarget()),
                resources
                        .stream()
                        .filter(t -> t.hasEncounter())
                        .map(t -> t.getEncounter()),
                resources
                        .stream()
                        .filter(t -> t.hasPerformer())
                        .flatMap(t -> t.getPerformer().stream()),
                resources
                        .stream()
                        .map(t -> getRecordedByExtensionValue(t))
                        .filter(t -> (t != null)))
                .collect(Collectors.toList());
    }
}
