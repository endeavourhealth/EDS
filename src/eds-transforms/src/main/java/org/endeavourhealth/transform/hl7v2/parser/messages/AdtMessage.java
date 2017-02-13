package org.endeavourhealth.transform.hl7v2.parser.messages;

import org.endeavourhealth.transform.hl7v2.parser.Message;
import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.Segment;
import org.endeavourhealth.transform.hl7v2.parser.segments.*;

import java.util.List;

public class AdtMessage extends Message {
    public AdtMessage(String message) throws ParseException {
        super(message);
    }

    public boolean hasMshSegment() {
        return super.hasSegment(SegmentName.MSH);
    }
    public boolean hasEvnSegment() { return super.hasSegment(SegmentName.EVN); }
    public boolean hasPidSegment() { return super.hasSegment(SegmentName.PID); }
    public boolean hasPd1Segment() { return super.hasSegment(SegmentName.PD1); }
    public boolean hasNk1Segment() { return super.hasSegment(SegmentName.NK1); }

    public MshSegment getMshSegment() {
        return (MshSegment) super.getSegment(SegmentName.MSH);
    }
    public EvnSegment getEvnSegment() { return (EvnSegment) super.getSegment(SegmentName.EVN); }
    public PidSegment getPidSegment() { return (PidSegment) super.getSegment(SegmentName.PID); }
    public Pd1Segment getPd1Segment() { return (Pd1Segment) super.getSegment(SegmentName.PD1); }
    public List<Segment> getNk1Segments() { return super.getSegments(SegmentName.NK1); }
}
