package org.endeavourhealth.transform.ui.transforms;

import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.endeavourhealth.transform.ui.models.JsonEncounter;
import org.hl7.fhir.instance.model.Encounter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

class JsonEncounterTransform {

    public static JsonEncounter transform(Encounter encounter) {
        return new JsonEncounter();
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
