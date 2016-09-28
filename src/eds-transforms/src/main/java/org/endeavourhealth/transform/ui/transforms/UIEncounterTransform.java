package org.endeavourhealth.transform.ui.transforms;

import org.endeavourhealth.core.utility.StreamExtension;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.endeavourhealth.transform.ui.helpers.DateHelper;
import org.endeavourhealth.transform.ui.models.UIEncounter;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Practitioner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

class UIEncounterTransform {

    public static List<UIEncounter> transform(List<Encounter> encounters, List<Practitioner> practitioners) {
        List<UIEncounter> uiEncounters = new ArrayList<>();

        for (Encounter encounter : encounters) {
            uiEncounters.add(transform(encounter, practitioners));
        }

        return uiEncounters;
    }

    private static UIEncounter transform(Encounter encounter, List<Practitioner> practitioners) {

        UIEncounter uiEncounter = new UIEncounter();

        Practitioner performedByPractitioner = getPerformedByParticipantId(encounter, practitioners);

        if (performedByPractitioner != null)
            uiEncounter.setPerformedBy(UIPractitionerTransform.transform(performedByPractitioner));

        Practitioner enteredByPractitioner = getEnteredByPractitionerId(encounter, practitioners);

        if (enteredByPractitioner != null)
            uiEncounter.setEnteredBy(UIPractitionerTransform.transform(enteredByPractitioner));

        uiEncounter.setDate(encounter.getPeriod().getStart());
        
        uiEncounter.setDisplayDate(getStartDisplayDate(encounter));

        return uiEncounter;
    }

    private static String getStartDisplayDate(Encounter encounter) {
        return DateHelper.format(encounter.getPeriod().getStart());
    }

    private static Practitioner getPerformedByParticipantId(Encounter encounter, List<Practitioner> practitioners) {
        for (Encounter.EncounterParticipantComponent component : encounter.getParticipant())
            if (component.getType().size() > 0)
                if (component.getType().get(0).getCoding().size() > 0)
                    if (component.getType().get(0).getCoding().get(0).getCode().equals("PPRF"))
                        return practitioners
                                .stream()
                                .filter(t -> t.getId().equals(ReferenceHelper.getReferenceId(component.getIndividual())))
                                .collect(StreamExtension.firstOrNullCollector());
        return null;
    }

    private static Practitioner getEnteredByPractitionerId(Encounter encounter, List<Practitioner> practitioners) {
        for (Encounter.EncounterParticipantComponent component : encounter.getParticipant())
            if (component.getType().size() > 0)
                if (component.getType().get(0).getText() == ("Entered by"))
                    return practitioners
                        .stream()
                        .filter(t -> t.getId().equals(ReferenceHelper.getReferenceId(component.getIndividual())))
                        .collect(StreamExtension.firstOrNullCollector());

        return null;
    }

    public static List<UUID> getPractitionerIds(List<Encounter> encounters) {
        List<UUID> result = new ArrayList<>();

        for (Encounter encounter : encounters) {
            if (encounter == null)
                continue;

            for (Encounter.EncounterParticipantComponent participantComponent : encounter.getParticipant())
                if (participantComponent.getIndividual() != null)
                    result.add(UUID.fromString(ReferenceHelper.getReferenceId(participantComponent.getIndividual())));
        }

        return result
                .stream()
                .distinct()
                .collect(Collectors.toList());
    }
}
