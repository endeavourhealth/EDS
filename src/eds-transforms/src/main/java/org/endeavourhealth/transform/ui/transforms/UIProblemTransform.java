package org.endeavourhealth.transform.ui.transforms;

import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.ui.helpers.CodeHelper;
import org.endeavourhealth.transform.ui.helpers.ExtensionHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.UICode;
import org.endeavourhealth.transform.ui.models.UIPractitioner;
import org.endeavourhealth.transform.ui.models.UIProblem;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class UIProblemTransform implements IUIClinicalTransform<Condition, UIProblem> {

    @Override
    public List<UIProblem> transform(List<Condition> resources, ReferencedResources referencedResources) {
        return resources
                .stream()
                .filter(t -> t.getMeta().hasProfile(FhirUri.PROFILE_URI_PROBLEM))
                .map(t -> transform(t, referencedResources))
                .collect(Collectors.toList());
    }

    private UIProblem transform(Condition condition, ReferencedResources referencedResources) {
        UIProblem uiProblem = (UIProblem)UIConditionTransform.transform(condition, referencedResources, true);

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

        if (expectedDuration != null)
            return null;

        return expectedDuration.getValue();
    }

    private static Date getLastReviewDate(Condition condition) {
        Extension extension = ExtensionHelper.getExtension(condition, FhirExtensionUri.PROBLEM_LAST_REVIEWED);
        DateTimeType lastReviewDate = ExtensionHelper.getExtensionValue(extension, FhirExtensionUri._PROBLEM_LAST_REVIEWED__DATE, DateTimeType.class);

        if (lastReviewDate == null)
            return null;

        return lastReviewDate.getValue();
    }

    private static UIPractitioner getLastReviewer(Condition condition, ReferencedResources referencedResources) {
        Extension extension = ExtensionHelper.getExtension(condition, FhirExtensionUri.PROBLEM_LAST_REVIEWED);
        Reference reference = ExtensionHelper.getExtensionValue(extension, FhirExtensionUri._PROBLEM_LAST_REVIEWED__DATE, Reference.class);

        if (reference != null)
            return null;

        return referencedResources.getUIPractitioner(reference);
    }

    @Override
    public List<Reference> getReferences(List<Condition> resources) {
        return new UIConditionTransform().getReferences(resources);
    }
}
