package org.endeavourhealth.transform.ui.transforms.clinical;

import org.endeavourhealth.common.utility.StreamExtension;
import org.endeavourhealth.transform.ui.helpers.CodeHelper;
import org.endeavourhealth.transform.ui.helpers.DateHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIImmunisation;
import org.endeavourhealth.transform.ui.models.types.UIDate;
import org.hl7.fhir.instance.model.Immunization;
import org.hl7.fhir.instance.model.Reference;

import java.rmi.server.UID;
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
                .setEffectiveDate(getDate(immunization))
                .setEffectivePractitioner(referencedResources.getUIPractitioner(immunization.getPerformer()))
                .setRecordedDate(getRecordedDateExtensionValue(immunization))
                .setRecordingPractitioner(getRecordedByExtensionValue(immunization, referencedResources))
                .setNotes(getNotes(immunization.getNote()));
    }

    private static UIDate getDate(Immunization immunization) {
        if (!immunization.hasDate())
            return null;

        return DateHelper.convert(immunization.getDateElement());
    }

    @Override
    public List<Reference> getReferences(List<Immunization> resources) {
        return StreamExtension.concat(
                resources
                        .stream()
                        .filter(t -> t.hasPatient())
                        .map(t -> t.getPatient()),
                resources
                        .stream()
                        .filter(t -> t.hasPerformer())
                        .map(t -> t.getPerformer()),
                resources
                        .stream()
                        .filter(t -> t.hasRequester())
                        .map(t -> t.getRequester()),
                resources
                        .stream()
                        .filter(t -> t.hasEncounter())
                        .map(t -> t.getEncounter()),
                resources
                        .stream()
                        .filter(t -> t.hasManufacturer())
                        .map(t -> t.getManufacturer()),
                resources
                        .stream()
                        .filter(t -> t.hasLocation())
                        .map(t -> t.getLocation()),
                resources
                        .stream()
                        .map(t -> getRecordedByExtensionValue(t))
                        .filter(t -> (t != null)))
                .collect(Collectors.toList());
    }
}
