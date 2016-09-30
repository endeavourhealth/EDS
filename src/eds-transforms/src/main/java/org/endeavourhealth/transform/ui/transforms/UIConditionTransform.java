package org.endeavourhealth.transform.ui.transforms;

import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.UICondition;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.Reference;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UIConditionTransform implements IUIClinicalTransform<Condition, UICondition> {

    public List<UICondition> transform(List<Condition> conditions, ReferencedResources referencedResources) {
        return new ArrayList<>();
    }

    public List<Reference> getReferences(List<Condition> conditions) {
        return Stream.concat(
                    conditions
                            .stream()
                            .filter(t -> t.hasAsserter())
                            .map(t -> t.getAsserter()),
                    conditions
                            .stream()
                            .flatMap(t -> t.getExtension().stream())
                            .filter(t -> t.getUrl() == FhirExtensionUri.RECORDED_BY)
                            .map(t -> (Reference)t.getValue()))
                .collect(Collectors.toList());
    }
}
