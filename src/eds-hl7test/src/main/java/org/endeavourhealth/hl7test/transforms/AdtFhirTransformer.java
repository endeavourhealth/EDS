package org.endeavourhealth.hl7test.transforms;

import org.endeavourhealth.hl7test.transforms.framework.Message;
import org.endeavourhealth.hl7test.transforms.framework.ParseException;
import org.endeavourhealth.hl7test.transforms.framework.namedsegments.MshSegment;

public class AdtFhirTransformer {
    public static String transform(String adtMessage) throws ParseException {

        Message message = new Message(adtMessage);

        MshSegment segment = (MshSegment)message.getSegment("MSH");

        return "Sending application " + segment.getSendingApplication()
                + "\n" + segment.getDateTimeOfMessage().toString()
                + "\n" + segment.getMessageType();
    }
}
