package org.endeavourhealth.transform.ui.transforms;

import org.endeavourhealth.transform.ui.models.JsonEncounter;
import org.endeavourhealth.transform.ui.models.JsonPatient;
import org.endeavourhealth.transform.ui.models.JsonPractitioner;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Practitioner;

import java.util.List;
import java.util.UUID;

public class JsonTransform {
    public static JsonPatient transformPatient(Patient patient) {
        return JsonPatientTransform.transform(patient);
    }

    public static JsonEncounter transformEncounter(Encounter encounter) {
        return new JsonEncounter();
    }

    public static List<UUID> getPractitionerIds(List<Encounter> encounters) {
        return JsonEncounterTransform.getPractitionerIds(encounters);
    }

    public JsonPractitioner transformPractitioner(Practitioner practitioner) {
        return JsonPractitionerTransform.transform(practitioner);
    }
}
