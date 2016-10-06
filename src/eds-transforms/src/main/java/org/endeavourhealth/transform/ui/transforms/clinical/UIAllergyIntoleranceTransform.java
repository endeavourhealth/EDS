package org.endeavourhealth.transform.ui.transforms.clinical;

import org.endeavourhealth.core.utility.StreamExtension;
import org.endeavourhealth.transform.ui.helpers.CodeHelper;
import org.endeavourhealth.transform.ui.helpers.DateHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIAllergyIntolerance;
import org.endeavourhealth.transform.ui.models.types.UIDate;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UIAllergyIntoleranceTransform extends UIClinicalTransform<AllergyIntolerance, UIAllergyIntolerance> {

    @Override
    public List<UIAllergyIntolerance> transform(List<AllergyIntolerance> resources, ReferencedResources referencedResources) {
        return resources
                .stream()
                .map(t -> transform(t, referencedResources))
                .collect(Collectors.toList());
    }

    private static UIAllergyIntolerance transform(AllergyIntolerance allergyIntolerance, ReferencedResources referencedResources) {

        return new UIAllergyIntolerance()
                .setId(allergyIntolerance.getId())
                .setCode(CodeHelper.convert(allergyIntolerance.getSubstance()))
                .setEffectivePractitioner(referencedResources.getUIPractitioner(allergyIntolerance.getRecorder()))
                .setEffectiveDate(getOnsetDate(allergyIntolerance))
                .setRecordingPractitioner(getRecordedByExtensionValue(allergyIntolerance, referencedResources))
                .setRecordedDate(getRecordedDate(allergyIntolerance))
                .setNotes(getNotes(allergyIntolerance.getNote()));
    }

    private static UIDate getRecordedDate(AllergyIntolerance allergyIntolerance) {

        if (!allergyIntolerance.hasRecordedDate())
            return null;

        return DateHelper.convert(allergyIntolerance.getRecordedDateElement());
    }

    private static UIDate getOnsetDate(AllergyIntolerance allergyIntolerance) {
        if (!allergyIntolerance.hasOnset())
            return null;

        return DateHelper.convert(allergyIntolerance.getOnsetElement());
    }

    @Override
    public List<Reference> getReferences(List<AllergyIntolerance> resources) {
        return StreamExtension.concat(
                resources
                        .stream()
                        .filter(t -> t.hasRecorder())
                        .map(t -> t.getRecorder()),
                resources
                        .stream()
                        .filter(t -> t.hasPatient())
                        .map(t -> t.getPatient()),
                resources
                        .stream()
                        .filter(t -> t.hasReporter())
                        .map(t -> t.getReporter()),
                resources
                        .stream()
                        .map(t -> getRecordedByExtensionValue(t))
                        .filter(t -> (t != null)))
                .collect(Collectors.toList());
    }
}
