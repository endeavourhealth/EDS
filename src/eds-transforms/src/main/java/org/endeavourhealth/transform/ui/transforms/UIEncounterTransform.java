package org.endeavourhealth.transform.ui.transforms;

import org.endeavourhealth.core.utility.StreamExtension;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.endeavourhealth.transform.ui.helpers.ReferencedResources;
import org.endeavourhealth.transform.ui.models.UIEncounter;
import org.endeavourhealth.transform.ui.models.UIPeriod;
import org.endeavourhealth.transform.ui.models.UIPractitioner;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.Reference;

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

        UIEncounter uiEncounter = new UIEncounter();

        UIPractitioner performedByPractitioner = getPerformedByParticipantId(encounter, referencedResources);

        if (performedByPractitioner != null)
            uiEncounter.setPerformedBy(performedByPractitioner);

        UIPractitioner enteredByPractitioner = getEnteredByPractitionerId(encounter, referencedResources);

        if (enteredByPractitioner != null)
            uiEncounter.setEnteredBy(enteredByPractitioner);

        uiEncounter.setPeriod(new UIPeriod().setStart(encounter.getPeriod().getStart()));

        return uiEncounter;
    }

    private static UIPractitioner getPerformedByParticipantId(Encounter encounter, ReferencedResources referencedResources) {
        for (Encounter.EncounterParticipantComponent component : encounter.getParticipant())
            if (component.getType().size() > 0)
                if (component.getType().get(0).getCoding().size() > 0)
                    if (component.getType().get(0).getCoding().get(0).getCode().equals("PPRF"))
                        return referencedResources.getUIPractitioner(component.getIndividual());

        return null;
    }

    private static UIPractitioner getEnteredByPractitionerId(Encounter encounter, ReferencedResources referencedResources) {
        for (Encounter.EncounterParticipantComponent component : encounter.getParticipant())
            if (component.getType().size() > 0)
                if (component.getType().get(0).getText() == ("Entered by"))
                    return referencedResources.getUIPractitioner(component.getIndividual());

        return null;
    }
}
