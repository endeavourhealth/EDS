package org.endeavourhealth.hl7test.transforms;

import org.endeavourhealth.hl7test.transforms.framework.ParseException;
import org.endeavourhealth.hl7test.transforms.framework.datatypes.Cx;
import org.endeavourhealth.hl7test.transforms.framework.datatypes.Xad;
import org.endeavourhealth.hl7test.transforms.framework.datatypes.Xpn;
import org.endeavourhealth.hl7test.transforms.framework.messages.AdtMessage;
import org.endeavourhealth.hl7test.transforms.framework.segments.MshSegment;
import org.endeavourhealth.hl7test.transforms.framework.segments.PidSegment;

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

        s += "External patient ID" + pidSegment.getExternalPatientId().getId() + " " + pidSegment.getExternalPatientId().getAssigningAuthority() + "\n";

        for (Cx cx : pidSegment.getInternalPatientId())
            s += "Internal patient ID" + cx.getId() + " " + cx.getAssigningAuthority() + "\n";

        for (Xpn xpn : pidSegment.getPatientNames())
            s += "Patient name " + xpn.getFamilyName() + " " + xpn.getGivenName() + " " + xpn.getPrefix() + "\n";

        s += "Race " + pidSegment.getRace().getAsString() + "\n";

        for (Xad xad : pidSegment.getAddresses())
            s += "Address " + xad.getStreetAddress() + " " + xad.getCity() + " " + xad.getAddressType() + "\n";

        return s;
    }
}
