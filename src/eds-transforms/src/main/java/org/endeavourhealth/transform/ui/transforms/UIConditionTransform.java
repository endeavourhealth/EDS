package org.endeavourhealth.transform.ui.transforms;

import org.endeavourhealth.transform.common.exceptions.TransformRuntimeException;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.ui.helpers.CodeHelper;
import org.endeavourhealth.transform.ui.helpers.ExtensionHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.UICode;
import org.endeavourhealth.transform.ui.models.UICondition;
import org.endeavourhealth.transform.ui.models.UIProblem;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.Reference;

import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class UIConditionTransform implements IUIClinicalTransform<Condition, UICondition> {

    @Override
    public List<UICondition> transform(List<Condition> resources, ReferencedResources referencedResources) {
        return resources
                .stream()
                .filter(t -> (!t.getMeta().hasProfile(FhirUri.PROFILE_URI_PROBLEM)))
                .map(t -> transform(t, referencedResources, false))
                .collect(Collectors.toList());
    }

    static UICondition transform(Condition condition, ReferencedResources referencedResources, boolean createProblem) {
        try {
            UICondition uiCondition = new UICondition();

            if (createProblem)
                uiCondition = new UIProblem();

            return uiCondition
                    .setCode(CodeHelper.convert(condition.getCode()))
                    .setOnsetDate(getOnsetDate(condition))
                    .setEndDate(getEndedDate(condition))
                    .setHasEnded(getHasEnded(condition))
                    .setNotes(condition.getNotes())
                    .setDateRecorded(condition.getDateRecorded());
        } catch (Exception e) {
            throw new TransformRuntimeException(e);
        }
    }

    private static Date getOnsetDate(Condition condition) throws Exception {
        if (condition.hasOnsetDateTimeType())
            return ((DateTimeType) condition.getOnset()).getValue();

        return null;
    }

    private static Boolean getHasEnded(Condition condition) throws Exception {
        if (condition.hasAbatementBooleanType())
            return condition.getAbatementBooleanType().getValue();
        else if (condition.hasAbatementDateTimeType())
            return true;

        return false;
    }

    private static Date getEndedDate(Condition condition) throws Exception {
        if (condition.hasAbatement())
            if (condition.hasAbatementDateTimeType())
                return condition.getAbatementDateTimeType().getValue();

        return null;
    }

    @Override
    public List<Reference> getReferences(List<Condition> resources) {
        return Stream.concat(
                resources
                        .stream()
                        .filter(t -> t.hasAsserter())
                        .map(t -> t.getAsserter()),
                resources
                        .stream()
                        .flatMap(t -> t.getExtension().stream())
                        .filter(t -> t.getUrl() == FhirExtensionUri.RECORDED_BY)
                        .map(t -> (Reference)t.getValue()))
                .collect(Collectors.toList());
    }
}
