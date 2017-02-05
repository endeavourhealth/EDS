package org.endeavourhealth.hl7test.hl7v2.transform;

import org.endeavourhealth.hl7test.hl7v2.parser.ParseException;
import org.endeavourhealth.hl7test.hl7v2.parser.segments.PidSegment;
import org.hl7.fhir.instance.model.Patient;

public class PatientTransform {
    public static Patient fromHl7v2(PidSegment source) throws ParseException {
        Patient target = new Patient();




        return target;
    }
}
