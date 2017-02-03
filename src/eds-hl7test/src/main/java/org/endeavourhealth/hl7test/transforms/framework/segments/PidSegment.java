package org.endeavourhealth.hl7test.transforms.framework.segments;

import org.endeavourhealth.hl7test.transforms.framework.ParseException;
import org.endeavourhealth.hl7test.transforms.framework.Seperators;

public class PidSegment extends Segment {
    public PidSegment(String line, Seperators seperators) throws ParseException {
        super(line, seperators);
    }
}
