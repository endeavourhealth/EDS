package org.endeavourhealth.transform.ui.transforms.clinical;

import org.endeavourhealth.common.fhir.FhirExtensionUri;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.transform.ui.helpers.CodeHelper;
import org.endeavourhealth.transform.ui.helpers.DateHelper;
import org.endeavourhealth.transform.ui.helpers.ExtensionHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPractitioner;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIProblem;
import org.endeavourhealth.transform.ui.models.types.UICode;
import org.endeavourhealth.transform.ui.models.types.UIDate;
import org.hl7.fhir.instance.model.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UIProblemTransform extends UIClinicalTransform<Condition, UIProblem> {

    @Override
    public List<UIProblem> transform(List<Condition> resources, ReferencedResources referencedResources) {
        return resources
                .stream()
                .filter(t -> t.getMeta().hasProfile(FhirUri.PROFILE_URI_PROBLEM))
                .map(t -> transform(t, referencedResources))
                .collect(Collectors.toList());
    }

    private UIProblem transform(Condition condition, ReferencedResources referencedResources) {
        UIProblem uiProblem = (UIProblem) UIConditionTransform.transform(condition, referencedResources, true);

        return uiProblem
                .setExpectedDuration(getExpectedDuration(condition))
                .setLastReviewDate(getLastReviewDate(condition))
                .setLastReviewer(getLastReviewer(condition, referencedResources))
                .setSignificance(getSignificance(condition));

//        UICondition fields plus
//
//        private Integer expectedDuration;
//        private Date lastReviewDate;
//        private UIPractitioner lastReviewer;
//        private UICode significance;
//        private UIProblem relatedProblem;     *
//        private String relationshipType;      *
    }

    private static UICode getSignificance(Condition condition) {
        CodeableConcept signficance = ExtensionHelper.getExtensionValue(condition, FhirExtensionUri.PROBLEM_SIGNIFICANCE, CodeableConcept.class);
        return CodeHelper.convert(signficance.getCoding().get(0));
    }

    private static Integer getExpectedDuration(Condition condition) {
        IntegerType expectedDuration = ExtensionHelper.getExtensionValue(condition, FhirExtensionUri.PROBLEM_EXPECTED_DURATION, IntegerType.class);

        if (expectedDuration == null) {
            return null;
        } else {
            return expectedDuration.getValue();
        }
    }

    private static UIDate getLastReviewDate(Condition condition) {
        Extension extension = ExtensionHelper.getExtension(condition, FhirExtensionUri.PROBLEM_LAST_REVIEWED);
        DateType lastReviewDate = ExtensionHelper.getExtensionValue(extension, FhirExtensionUri._PROBLEM_LAST_REVIEWED__DATE, DateType.class);

        if (lastReviewDate == null)
            return null;

        return DateHelper.convert(lastReviewDate);
    }

    private static Reference getLastReviewerReference(Condition condition) {
        Extension extension = ExtensionHelper.getExtension(condition, FhirExtensionUri.PROBLEM_LAST_REVIEWED);
        return ExtensionHelper.getExtensionValue(extension, FhirExtensionUri._PROBLEM_LAST_REVIEWED__PERFORMER, Reference.class);
    }

    private static UIPractitioner getLastReviewer(Condition condition, ReferencedResources referencedResources) {
        Reference reference = getLastReviewerReference(condition);

        if (reference != null)
            return null;

        return referencedResources.getUIPractitioner(reference);
    }

    @Override
    public List<Reference> getReferences(List<Condition> resources) {
        return Stream.concat(new UIConditionTransform()
                        .getReferences(resources)
                        .stream(),
                resources
                        .stream()
                        .map(t -> getLastReviewerReference(t))
                        .filter(t -> (t != null)))
                .collect(Collectors.toList());
    }
}
