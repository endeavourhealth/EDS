package org.endeavourhealth.hl7test.transforms.framework.messages;

import org.endeavourhealth.hl7test.transforms.framework.Message;
import org.endeavourhealth.hl7test.transforms.framework.ParseException;
import org.endeavourhealth.hl7test.transforms.framework.segments.MshSegment;
import org.endeavourhealth.hl7test.transforms.framework.segments.PidSegment;
import org.endeavourhealth.hl7test.transforms.framework.segments.SegmentName;

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
