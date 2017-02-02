package org.endeavourhealth.hl7test.transforms;

import org.endeavourhealth.hl7test.transforms.framework.ParseException;
import org.endeavourhealth.hl7test.transforms.framework.Parser;

public class AdtFhirTransformer {
    public static String transform(String adtMessage) throws ParseException {

        Parser parser = new Parser(adtMessage);
        return parser.getFirstSegment().getSegmentName();
    }
}
