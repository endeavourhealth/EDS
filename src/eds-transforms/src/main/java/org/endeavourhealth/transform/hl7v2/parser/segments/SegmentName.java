package org.endeavourhealth.transform.hl7v2.parser.segments;

import org.endeavourhealth.transform.hl7v2.parser.ParseException;
import org.endeavourhealth.transform.hl7v2.parser.Segment;
import org.endeavourhealth.transform.hl7v2.parser.Seperators;

public enum SegmentName {
    MSH("MSH"),
    PID("PID"),
    UNNAMED("UNNAMED");

    private String value;

    SegmentName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SegmentName fromString(String value) {
        if (value == null)
            return null;

        for (SegmentName segmentName : SegmentName.values())
            if (value.equals(segmentName.value))
                return segmentName;

        return UNNAMED;
    }

    public static Segment instantiateSegment(String segmentName, String segment, Seperators seperators) throws ParseException {
        return instantiateSegment(SegmentName.fromString(segmentName), segment, seperators);
    }

    static Segment instantiateSegment(SegmentName segmentName, String segment, Seperators seperators) throws ParseException {
        if (segmentName == null)
            return null;

        switch (segmentName) {
            case MSH: return new MshSegment(segment, seperators);
            case PID: return new PidSegment(segment, seperators);
            case UNNAMED:
            default: return new Segment(segment, seperators);
        }
    }
}
