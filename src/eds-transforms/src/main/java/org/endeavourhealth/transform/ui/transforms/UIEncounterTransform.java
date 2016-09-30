package org.endeavourhealth.transform.ui.transforms;

import org.endeavourhealth.core.utility.StreamExtension;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.UIEncounter;
import org.endeavourhealth.transform.ui.models.UIPeriod;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.Reference;

import java.util.List;
import java.util.stream.Collectors;

public class UIEncounterTransform implements IUIClinicalTransform<Encounter, UIEncounter> {

    public List<UIEncounter> transform(List<Encounter> encounters, ReferencedResources referencedResources) {
        return encounters
                .stream()
                .map(t -> transform(t, referencedResources.getPractitioners()))
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

    private static UIEncounter transform(Encounter encounter, List<Practitioner> practitioners) {

        UIEncounter uiEncounter = new UIEncounter();

        Practitioner performedByPractitioner = getPerformedByParticipantId(encounter, practitioners);

        if (performedByPractitioner != null)
            uiEncounter.setPerformedBy(UIPractitionerTransform.transform(performedByPractitioner));

        Practitioner enteredByPractitioner = getEnteredByPractitionerId(encounter, practitioners);

        if (enteredByPractitioner != null)
            uiEncounter.setEnteredBy(UIPractitionerTransform.transform(enteredByPractitioner));

        uiEncounter.setPeriod(new UIPeriod().setStart(encounter.getPeriod().getStart()));

        return uiEncounter;
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
}
