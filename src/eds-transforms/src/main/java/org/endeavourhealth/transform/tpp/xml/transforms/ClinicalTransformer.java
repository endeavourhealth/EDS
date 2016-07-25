package org.endeavourhealth.transform.tpp.xml.transforms;

import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.tpp.xml.schema.Clinical;
import org.endeavourhealth.transform.tpp.xml.schema.Event;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;

public class ClinicalTransformer {

    public static void transform(Clinical tppClinical, List<Resource> fhirResources) throws TransformException {
        for (Event tppEvent: tppClinical.getEvent()) {
            EventTransformer.transform(tppEvent, fhirResources);
        }
    }
}
