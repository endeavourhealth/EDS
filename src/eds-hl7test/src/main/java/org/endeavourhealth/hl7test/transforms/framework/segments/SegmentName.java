package org.endeavourhealth.hl7test.transforms.framework.segments;

import org.endeavourhealth.hl7test.transforms.framework.ParseException;
import org.endeavourhealth.hl7test.transforms.framework.Seperators;

public enum SegmentName {
    MSH("MSH"),
    PID("PID"),
    NOT_DEFINED("NOT-DEFINED");

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

        return NOT_DEFINED;
    }

    public static Segment instantiateSegment(String segmentName, String line, Seperators seperators) throws ParseException {
        return instantiateSegment(SegmentName.fromString(segmentName), line, seperators);
    }

    static Segment instantiateSegment(SegmentName segmentName, String line, Seperators seperators) throws ParseException {
        if (segmentName == null)
            return null;

        switch (segmentName) {
            case MSH: return new MshSegment(line, seperators);
            case PID: return new PidSegment(line, seperators);
            case NOT_DEFINED:
            default: return new Segment(line, seperators);
        }
    }
}
