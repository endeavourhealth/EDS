package org.endeavourhealth.hl7test.transforms;

import org.endeavourhealth.hl7test.transforms.framework.Message;
import org.endeavourhealth.hl7test.transforms.framework.ParseException;

public class AdtFhirTransformer {
    public static String transform(String adtMessage) throws ParseException {

        Message message = new Message(adtMessage);

        return "hello";
    }
}
