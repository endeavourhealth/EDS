package org.endeavourhealth.transform.tpp.transforms;

import org.endeavourhealth.transform.tpp.schema.DrugSensitivity;
import org.endeavourhealth.transform.tpp.schema.Event;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;

public class DrugSensitivityTransformer {
    public static void transform(List<DrugSensitivity> tppDrugSensitivities, Event tppEvent, Encounter fhirEncounter, List<Resource> fhirResources) {


    }
}
