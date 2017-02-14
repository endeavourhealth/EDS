package org.endeavourhealth.transform.hl7v2.parser.segments;

import org.apache.commons.lang3.NotImplementedException;
import org.endeavourhealth.transform.hl7v2.parser.Segment;

public enum SegmentName {
    AL1("AL1"),
    EVN("EVN"),
    MSH("MSH"),
    NK1("NK1"),
    OBX("OBX"),
    PD1("PD1"),
    PID("PID"),
    PV1("PV1"),
    PV2("PV2");

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

        return null;
    }

    public static Class<? extends Segment> getSegmentClass(String segmentName) {
        return getSegmentClass(SegmentName.fromString(segmentName));
    }

    public static Class<? extends Segment> getSegmentClass(SegmentName segmentName) {
        if (segmentName == null)
            return null;

        switch (segmentName) {
            case AL1: return Al1Segment.class;
            case EVN: return EvnSegment.class;
            case MSH: return MshSegment.class;
            case NK1: return Nk1Segment.class;
            case OBX: return ObxSegment.class;
            case PD1: return Pd1Segment.class;
            case PID: return PidSegment.class;
            case PV1: return Pv1Segment.class;
            case PV2: return Pv2Segment.class;
            default: throw new NotImplementedException("SegmentClass not defined");
        }
    }
}
