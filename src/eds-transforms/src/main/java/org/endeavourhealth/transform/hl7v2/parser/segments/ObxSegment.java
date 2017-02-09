package org.endeavourhealth.transform.hl7v2.parser.segments;

import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.Segment;
import org.endeavourhealth.transform.hl7v2.parser.Seperators;

public class ObxSegment extends Segment {
    public ObxSegment(String segment, Seperators seperators) throws ParseException {
        super(segment, seperators);
    }


}
