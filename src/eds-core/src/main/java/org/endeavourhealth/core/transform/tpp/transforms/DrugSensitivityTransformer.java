package org.endeavourhealth.core.transform.tpp.transforms;

import org.endeavourhealth.core.transform.tpp.schema.CarePlan;
import org.endeavourhealth.core.transform.tpp.schema.DrugSensitivity;
import org.endeavourhealth.core.transform.tpp.schema.Event;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;

public class DrugSensitivityTransformer {
    public static void transform(List<DrugSensitivity> tppDrugSensitivities, Event tppEvent, Encounter fhirEncounter, List<Resource> fhirResources) {


    }
}
