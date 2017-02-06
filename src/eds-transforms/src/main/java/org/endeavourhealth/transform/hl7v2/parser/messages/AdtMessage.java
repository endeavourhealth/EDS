package org.endeavourhealth.transform.hl7v2.parser.messages;

import org.endeavourhealth.transform.hl7v2.parser.Message;
import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.segments.MshSegment;
import org.endeavourhealth.transform.hl7v2.parser.segments.PidSegment;
import org.endeavourhealth.transform.hl7v2.parser.segments.SegmentName;

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
