package org.endeavourhealth.hl7test.transform.parser.messages;

import org.endeavourhealth.hl7test.transform.parser.Message;
import org.endeavourhealth.hl7test.transform.parser.ParseException;
import org.endeavourhealth.hl7test.transform.parser.segments.MshSegment;
import org.endeavourhealth.hl7test.transform.parser.segments.PidSegment;
import org.endeavourhealth.hl7test.transform.parser.segments.SegmentName;

public class AdtMessage extends Message {
    public AdtMessage(String message) throws ParseException {
        super(message);
    }

    public MshSegment getMshSegment() {
        return (MshSegment) super.getSegment(SegmentName.MSH);
    }

    public PidSegment getPidSegment() {
        return (PidSegment)super.getSegment(SegmentName.PID);
    }
}
