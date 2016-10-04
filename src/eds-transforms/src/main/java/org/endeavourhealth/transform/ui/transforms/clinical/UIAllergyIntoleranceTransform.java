package org.endeavourhealth.transform.ui.transforms.clinical;

import org.endeavourhealth.transform.ui.helpers.CodeHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.resources.UIAllergyIntolerance;
import org.hl7.fhir.instance.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.Reference;

import java.util.List;
import java.util.stream.Collectors;

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
                .setRecordedBy(getRecordedByExtensionValue(allergyIntolerance, referencedResources))
                .setRecordedDate(allergyIntolerance.getRecordedDate())
                .setCode(CodeHelper.convert(allergyIntolerance.getSubstance()));
    }

    @Override
    public List<Reference> getReferences(List<AllergyIntolerance> resources) {
        return null;
    }
}
