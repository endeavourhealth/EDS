package org.endeavourhealth.hl7test.transform.transform;

import org.endeavourhealth.hl7test.transform.parser.ParseException;
import org.endeavourhealth.hl7test.transform.parser.segments.PidSegment;
import org.hl7.fhir.instance.model.Patient;

public class PidTransform {
    public static Patient toFhir(PidSegment source) throws ParseException {
        Patient target = new Patient();




        return target;
    }
}
