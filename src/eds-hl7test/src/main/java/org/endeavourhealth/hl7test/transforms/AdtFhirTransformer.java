package org.endeavourhealth.hl7test.transforms;

import org.endeavourhealth.hl7test.transforms.framework.ParseException;
import org.endeavourhealth.hl7test.transforms.framework.datatypes.Cx;
import org.endeavourhealth.hl7test.transforms.framework.messages.AdtMessage;
import org.endeavourhealth.hl7test.transforms.framework.segments.MshSegment;
import org.endeavourhealth.hl7test.transforms.framework.segments.PidSegment;

import java.util.List;

public class AdtFhirTransformer {
    public static String transform(String adtMessage) throws ParseException {

        AdtMessage message = new AdtMessage(adtMessage);

        MshSegment mshSegment = message.getMshSegment();
        PidSegment pidSegment = message.getPidSegment();

        String s = "field char:  " + mshSegment.getFieldSeparator() + "\n";
        s += "encoding chars:  " + mshSegment.getEncodingCharacters() + "\n";
        s += "sending application:  " + mshSegment.getSendingApplication() + "\n";
        s += "receiving application:  " + mshSegment.getReceivingApplication() + "\n";
        s += "message name:  " + mshSegment.getMessageType() + "\n";



        List<Cx> patientIds = pidSegment.getInternalPatientIdField();

        for (Cx cx : patientIds) {
            s += "External patient ID" + cx.getId() + " " + cx.getAssigningAuthority() + "\n";
        }

        return s;
    }
}
