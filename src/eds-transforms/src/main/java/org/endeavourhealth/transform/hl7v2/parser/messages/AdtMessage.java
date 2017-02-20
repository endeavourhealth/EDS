package org.endeavourhealth.transform.hl7v2.parser.messages;

import org.endeavourhealth.transform.hl7v2.parser.Message;
import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.Segment;
import org.endeavourhealth.transform.hl7v2.parser.segments.*;

import java.util.HashMap;
import java.util.List;

public class AdtMessage extends Message {
    public AdtMessage(String message) throws ParseException {
        super(message);
    }

    public AdtMessage(String message, HashMap<String, Class<? extends Segment>> zSegmentDefinitions) throws ParseException {
        super(message, zSegmentDefinitions);
    }

    public boolean hasMshSegment() {
        return super.hasSegment(SegmentName.MSH);
    }
    public boolean hasEvnSegment() { return super.hasSegment(SegmentName.EVN); }
    public boolean hasPidSegment() { return super.hasSegment(SegmentName.PID); }
    public boolean hasPd1Segment() { return super.hasSegment(SegmentName.PD1); }
    public boolean hasNk1Segment() { return super.hasSegment(SegmentName.NK1); }
    public boolean hasPv1Segment() { return super.hasSegment(SegmentName.PV1); }
    public boolean hasPv2Segment() { return super.hasSegment(SegmentName.PV2); }
    public boolean hasObxSegment() { return super.hasSegment(SegmentName.OBX); }

    public MshSegment getMshSegment() {
        return (MshSegment) super.getSegment(SegmentName.MSH);
    }
    public EvnSegment getEvnSegment() { return (EvnSegment) super.getSegment(SegmentName.EVN); }
    public PidSegment getPidSegment() { return (PidSegment) super.getSegment(SegmentName.PID); }
    public Pd1Segment getPd1Segment() { return (Pd1Segment) super.getSegment(SegmentName.PD1); }
    public List<Nk1Segment> getNk1Segments() { return (List<Nk1Segment>)super.getSegments(SegmentName.NK1); }
    public Pv1Segment getPv1Segment() { return (Pv1Segment) super.getSegment(SegmentName.PV1); }
    public Pv2Segment getPv2Segment() { return (Pv2Segment) super.getSegment(SegmentName.PV2); }
    public List<ObxSegment> getObxSegments() { return (List<ObxSegment>)super.getSegments(SegmentName.OBX); }
}
