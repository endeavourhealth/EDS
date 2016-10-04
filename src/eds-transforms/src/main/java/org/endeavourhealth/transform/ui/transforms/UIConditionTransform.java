package org.endeavourhealth.transform.ui.transforms;

import org.endeavourhealth.transform.common.exceptions.TransformRuntimeException;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.ui.helpers.CodeHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.resources.UICondition;
import org.endeavourhealth.transform.ui.models.resources.UIPractitioner;
import org.endeavourhealth.transform.ui.models.resources.UIProblem;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.Reference;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class UIConditionTransform extends UIClinicalTransform<Condition, UICondition> {

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
                    .setId(condition.getId())
                    .setRecordedBy(getRecordedByExtensionValue(condition, referencedResources))
                    .setRecordedDate(condition.getDateRecorded())
                    .setAsserter(getAsserter(condition, referencedResources))
                    .setCode(CodeHelper.convert(condition.getCode()))
                    .setClinicalStatus(condition.getClinicalStatus())
                    .setVerificationStatus(getConditionVerificationStatus(condition))
                    .setOnsetDate(getOnsetDate(condition))
                    .setAbatementDate(getAbatementDate(condition))
                    .setHasAbated(getAbatement(condition))
                    .setNotes(condition.getNotes());

//            private UIEncounter encounter;     *
//            private UICodeableConcept code;
//            private String clinicalStatus;
//            private String verificationStatus;
//            private Date onset;
//            private Date abatement;
//            private Boolean hasAbated;
//            private String notes;
//            private UIProblem partOfProblem;   *

        } catch (Exception e) {
            throw new TransformRuntimeException(e);
        }
    }

    private static String getConditionVerificationStatus(Condition condition) {
        if (condition.getVerificationStatus() == null)
            return null;

        return condition.getVerificationStatus().toCode();
    }

    private static UIPractitioner getAsserter(Condition condition, ReferencedResources referencedResources) {
        if (!condition.hasAsserter())
            return null;

        return referencedResources.getUIPractitioner(condition.getAsserter());
    }

    private static Date getOnsetDate(Condition condition) throws Exception {
        if (condition.hasOnsetDateTimeType())
            return ((DateTimeType) condition.getOnset()).getValue();

        return null;
    }

    private static Boolean getAbatement(Condition condition) throws Exception {
        if (condition.hasAbatementBooleanType())
            return condition.getAbatementBooleanType().getValue();
        else if (condition.hasAbatementDateTimeType())
            return true;

        return false;
    }

    private static Date getAbatementDate(Condition condition) throws Exception {
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
