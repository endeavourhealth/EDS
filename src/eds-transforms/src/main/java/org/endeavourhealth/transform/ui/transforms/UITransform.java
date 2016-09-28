package org.endeavourhealth.transform.ui.transforms;

import org.endeavourhealth.transform.ui.models.UIEncounter;
import org.endeavourhealth.transform.ui.models.UIPatient;
import org.endeavourhealth.transform.ui.models.UIPractitioner;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Practitioner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UITransform {
    public static UIPatient transformPatient(Patient patient) {
        return UIPatientTransform.transform(patient);
    }

    public static List<UIEncounter> transformEncounters(List<Encounter> encounters, List<Practitioner> practitioners) {
        return UIEncounterTransform.transform(encounters, practitioners);
    }

    public static List<UUID> getPractitionerIds(List<Encounter> encounters) {
        return UIEncounterTransform.getPractitionerIds(encounters);
    }

    public UIPractitioner transformPractitioner(Practitioner practitioner) {
        return UIPractitionerTransform.transform(practitioner);
    }
}
