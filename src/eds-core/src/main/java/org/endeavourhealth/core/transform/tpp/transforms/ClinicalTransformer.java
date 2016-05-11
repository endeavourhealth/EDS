package org.endeavourhealth.core.transform.tpp.transforms;

import org.endeavourhealth.core.transform.tpp.schema.Clinical;
import org.endeavourhealth.core.transform.tpp.schema.Event;
import org.endeavourhealth.core.transform.tpp.schema.NonClinical;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;

public class ClinicalTransformer {

    public static void transform(Clinical tppClinical, List<Resource> fhirResources) {
        for (Event tppEvent: tppClinical.getEvent()) {
            EventTransformer.transform(tppEvent, fhirResources);
        }
    }
}
