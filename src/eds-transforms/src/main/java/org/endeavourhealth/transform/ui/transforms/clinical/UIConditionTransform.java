package org.endeavourhealth.transform.ui.transforms.clinical;

import org.endeavourhealth.core.utility.StreamExtension;
import org.endeavourhealth.transform.common.exceptions.TransformRuntimeException;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.ui.helpers.CodeHelper;
import org.endeavourhealth.transform.ui.helpers.DateHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UICondition;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPractitioner;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIProblem;
import org.endeavourhealth.transform.ui.models.types.UIDate;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.DateTimeType;
import org.hl7.fhir.instance.model.Reference;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UIConditionTransform extends UIClinicalTransform<Condition, UICondition> {

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
                    .setCode(CodeHelper.convert(condition.getCode()))
                    .setEffectivePractitioner(getAsserter(condition, referencedResources))
                    .setEffectiveDate(getOnsetDateTime(condition))
                    .setRecordingPractitioner(getRecordedByExtensionValue(condition, referencedResources))
                    .setRecordedDate(getDateRecorded(condition))
                    .setClinicalStatus(condition.getClinicalStatus())
                    .setVerificationStatus(getConditionVerificationStatus(condition))
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

    private static UIDate getDateRecorded(Condition condition) {
        if (!condition.hasDateRecorded())
            return null;

        return DateHelper.convert(condition.getDateRecordedElement());
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

    private static UIDate getOnsetDateTime(Condition condition) throws Exception {
        if (!condition.hasOnsetDateTimeType())
            return DateHelper.getUnknownDate();

        return DateHelper.convert(condition.getOnsetDateTimeType());
    }

    private static Boolean getAbatement(Condition condition) throws Exception {
        if (condition.hasAbatementBooleanType())
            return condition.getAbatementBooleanType().getValue();
        else if (condition.hasAbatementDateTimeType())
            return true;

        return false;
    }

    private static UIDate getAbatementDate(Condition condition) throws Exception {
        if (condition.hasAbatement())
            if (condition.hasAbatementDateTimeType())
                return DateHelper.convert(condition.getAbatementDateTimeType());

        return null;
    }

    @Override
    public List<Reference> getReferences(List<Condition> resources) {
        return StreamExtension.concat(
                resources
                        .stream()
                        .map(t -> t.getPatient()),
                resources
                        .stream()
                        .filter(t -> t.hasEncounter())
                        .map(t -> t.getEncounter()),
                resources
                        .stream()
                        .filter(t -> t.hasAsserter())
                        .map(t -> t.getAsserter()),
                resources
                        .stream()
                        .map(t -> getRecordedByExtensionValue(t))
                        .filter(t -> (t != null)),
                resources
                        .stream()
                        .filter(t -> t.hasStage())
                        .filter(t -> t.getStage().hasAssessment())
                        .flatMap(t -> t.getStage().getAssessment().stream()))
                .collect(Collectors.toList());
    }
}
