package org.endeavourhealth.core.transform.tpp.transforms;

import org.endeavourhealth.core.transform.tpp.schema.Event;
import org.endeavourhealth.core.transform.tpp.schema.PatientPlan;
import org.endeavourhealth.core.transform.tpp.schema.RepeatMedication;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;

public class PatientPlanTransformer {

    public static void transform(List<PatientPlan> tppPatientPlans, Event tppEvent, Encounter fhirEncounter, List<Resource> fhirResources) {


    }
}
