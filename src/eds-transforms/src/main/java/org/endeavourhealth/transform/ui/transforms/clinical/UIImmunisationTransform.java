package org.endeavourhealth.transform.ui.transforms.clinical;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.ui.helpers.CodeHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIImmunisation;
import org.hl7.fhir.instance.model.Immunization;
import org.hl7.fhir.instance.model.Reference;

import java.util.List;
import java.util.stream.Collectors;

public class UIImmunisationTransform extends UIClinicalTransform<Immunization, UIImmunisation> {

    @Override
    public List<UIImmunisation> transform(List<Immunization> resources, ReferencedResources referencedResources) {
        return resources
                .stream()
                .map(t -> transform(t, referencedResources))
                .collect(Collectors.toList());
    }

    private static UIImmunisation transform(Immunization immunization, ReferencedResources referencedResources) {
        return new UIImmunisation()
                .setId(immunization.getId())
                .setCode(CodeHelper.convert(immunization.getVaccineCode()))
                .setEffectiveDate(immunization.getDate())
                .setEffectivePractitioner(referencedResources.getUIPractitioner(immunization.getPerformer()))
                .setRecordedDate(getRecordedDateExtensionValue(immunization))
                .setRecordingPractitioner(getRecordedByExtensionValue(immunization, referencedResources))
                .setNotes(getNotes(immunization.getNote()));
    }

    @Override
    public List<Reference> getReferences(List<Immunization> resources) {
        return null;
    }
}
