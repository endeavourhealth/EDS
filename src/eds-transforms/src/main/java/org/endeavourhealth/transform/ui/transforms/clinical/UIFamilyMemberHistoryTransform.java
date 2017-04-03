package org.endeavourhealth.transform.ui.transforms.clinical;

import org.endeavourhealth.transform.ui.helpers.CodeHelper;
import org.endeavourhealth.transform.ui.helpers.DateHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIFamilyMemberHistory;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIFamilyMemberHistoryCondition;
import org.endeavourhealth.transform.ui.models.types.UICodeableConcept;
import org.endeavourhealth.transform.ui.models.types.UIDate;
import org.hl7.fhir.instance.model.FamilyMemberHistory;
import org.hl7.fhir.instance.model.Reference;

import java.util.List;
import java.util.stream.Collectors;

public class UIFamilyMemberHistoryTransform extends UIClinicalTransform<FamilyMemberHistory, UIFamilyMemberHistory> {

    @Override
    public List<UIFamilyMemberHistory> transform(List<FamilyMemberHistory> resources, ReferencedResources referencedResources) {
        return resources
                .stream()
                .map(t -> transform(t, referencedResources))
                .collect(Collectors.toList());
    }

    private static UIFamilyMemberHistory transform(FamilyMemberHistory familyMemberHistory, ReferencedResources referencedResources) {

        return new UIFamilyMemberHistory()
                .setId(familyMemberHistory.getId())
                .setCode(CodeHelper.convert(familyMemberHistory.getRelationship()))
                .setRecordingPractitioner(getRecordedByExtensionValue(familyMemberHistory, referencedResources))
                .setRecordedDate(getRecordedDate(familyMemberHistory))
                .setNotes(getNotes(familyMemberHistory.getNote()))
                .setConditions(getConditions(familyMemberHistory.getCondition()));
    }

    private static UIDate getRecordedDate(FamilyMemberHistory familyMemberHistory) {
        if (!familyMemberHistory.hasDate())
            return null;

        return DateHelper.convert(familyMemberHistory.getDateElement());
    }

    private static List<UIFamilyMemberHistoryCondition> getConditions(List<FamilyMemberHistory.FamilyMemberHistoryConditionComponent> conditionComponents) {
        return conditionComponents.stream()
            .filter(t -> t.hasCode())
            .map(t ->
                new UIFamilyMemberHistoryCondition()
                    .setCode(CodeHelper.convert(t.getCode()))
                    .setOutcome(CodeHelper.convert(t.getOutcome()))
            )
            .collect(Collectors.toList());
    }

    @Override
    public List<Reference> getReferences(List<FamilyMemberHistory> resources) {
        return resources
                        .stream()
                        .filter(t -> t.hasPatient())
                        .map(t -> t.getPatient())
                .collect(Collectors.toList());
    }
}
