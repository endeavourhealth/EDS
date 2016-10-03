package org.endeavourhealth.transform.ui.transforms;

import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.ui.helpers.CodeHelper;
import org.endeavourhealth.transform.ui.helpers.ExtensionHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.UICode;
import org.endeavourhealth.transform.ui.models.UIProblem;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.Reference;

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

        UICode signficance = getSignificance(condition);

        return uiProblem
                .setSignificance(signficance);
    }

    private static UICode getSignificance(Condition condition) {
        CodeableConcept signficance = ExtensionHelper.getExtensionValue(condition, FhirExtensionUri.PROBLEM_SIGNIFICANCE, CodeableConcept.class);
        return CodeHelper.convert(signficance.getCoding().get(0));
    }

    @Override
    public List<Reference> getReferences(List<Condition> resources) {
        return new UIConditionTransform().getReferences(resources);
    }
}
