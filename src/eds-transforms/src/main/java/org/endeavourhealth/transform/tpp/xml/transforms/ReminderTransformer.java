package org.endeavourhealth.transform.tpp.xml.transforms;

import org.endeavourhealth.transform.tpp.xml.schema.Event;
import org.endeavourhealth.transform.tpp.xml.schema.Reminder;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;

public class ReminderTransformer {

    public static void transform(List<Reminder> tppReminders, Event tppEvent, Encounter fhirEncounter, List<Resource> fhirResources) {


    }
}
