package org.endeavourhealth.transform.ui.transforms;

import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.ui.helpers.CodeHelper;
import org.endeavourhealth.transform.ui.helpers.ExtensionHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.resources.UIAppointment;
import org.endeavourhealth.transform.ui.models.resources.UIEncounter;
import org.endeavourhealth.transform.ui.models.resources.UIOrganisation;
import org.endeavourhealth.transform.ui.models.types.UICode;
import org.endeavourhealth.transform.ui.models.types.UICodeableConcept;
import org.endeavourhealth.transform.ui.models.types.UIPeriod;
import org.endeavourhealth.transform.ui.models.resources.UIPractitioner;
import org.hl7.fhir.instance.model.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class UIEncounterTransform implements IUIClinicalTransform<Encounter, UIEncounter> {

    public List<UIEncounter> transform(List<Encounter> encounters, ReferencedResources referencedResources) {
        return encounters
                .stream()
                .map(t -> transform(t, referencedResources))
                .collect(Collectors.toList());
    }

    public List<Reference> getReferences(List<Encounter> encounters) {
        return encounters
                .stream()
                .flatMap(t -> t.getParticipant().stream())
                .filter(t -> t.hasIndividual())
                .map(t -> t.getIndividual())
                .collect(Collectors.toList());
    }

    private static UIEncounter transform(Encounter encounter, ReferencedResources referencedResources) {

        return new UIEncounter()
                .setId(encounter.getId())
                .setStatus(getStatus(encounter))
                .setPerformedBy(getPerformedByParticipantId(encounter, referencedResources))
                .setRecordedBy(getRecordedByPractitionerId(encounter, referencedResources))
                .setPeriod(getPeriod(encounter.getPeriod()))
                .setServiceProvider(getServiceProvider(encounter, referencedResources))
                .setRecordedDate(getRecordedDate(encounter))
                .setEncounterSource(getEncounterSource(encounter));

//        private String status;
//        private UIAppointment appointment;        *
//        private UIPractitioner performedBy;
//        private UIPractitioner recordedBy;
//        private UIPeriod period;
//        private UIOrganisation serviceProvider;
//        private Date recordedDate;
//        private UICode encounterSource;
    }

    private static UICodeableConcept getEncounterSource(Encounter encounter) {
        CodeableConcept encounterSource = ExtensionHelper.getExtensionValue(encounter, FhirExtensionUri.ENCOUNTER_SOURCE, CodeableConcept.class);
        return CodeHelper.convert(encounterSource);
    }

    private static Date getRecordedDate(Encounter encounter) {
        DateTimeType recordedDate = ExtensionHelper.getExtensionValue(encounter, FhirExtensionUri.RECORDED_DATE, DateTimeType.class);

        if (recordedDate == null)
            return null;

        return recordedDate.getValue();
    }

    private static UIOrganisation getServiceProvider(Encounter encounter, ReferencedResources referencedResources) {
        return referencedResources.getUIOrganisation(encounter.getServiceProvider());
    }

    private static UIPeriod getPeriod(Period period) {
        return new UIPeriod().setStart(period.getStart());
    }

    private static String getStatus(Encounter encounter) {
        if (!encounter.hasStatus())
            return null;

        return encounter.getStatus().toCode();
    }

    private static UIPractitioner getPerformedByParticipantId(Encounter encounter, ReferencedResources referencedResources) {
        for (Encounter.EncounterParticipantComponent component : encounter.getParticipant())
            if (component.getType().size() > 0)
                if (component.getType().get(0).getCoding().size() > 0)
                    if (component.getType().get(0).getCoding().get(0).getCode().equals("PPRF"))
                        return referencedResources.getUIPractitioner(component.getIndividual());

        return null;
    }

    private static UIPractitioner getRecordedByPractitionerId(Encounter encounter, ReferencedResources referencedResources) {
        for (Encounter.EncounterParticipantComponent component : encounter.getParticipant())
            if (component.getType().size() > 0)
                if (component.getType().get(0).getText() == ("Entered by"))
                    return referencedResources.getUIPractitioner(component.getIndividual());

        return null;
    }
}
