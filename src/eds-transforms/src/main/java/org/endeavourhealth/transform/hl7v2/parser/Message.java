package org.endeavourhealth.transform.hl7v2.parser;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.transform.hl7v2.parser.segments.SegmentName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Message {
    private static final String CR = "\r";
    private static final String LF = "\n";
    private static final int FIRST = 0;
    private static final String MSH_SEGMENT_NAME = "MSH";

    private String message;
    private Seperators seperators;
    private List<Segment> segments = new ArrayList<>();;

    //////////////////  Constructors  //////////////////

    public Message(String message) throws ParseException {
        Validate.notBlank(message);

        this.message = message;
        parse();
    }

    //////////////////  Accessors  //////////////////

    public boolean hasSegment(String segmentName) {
        return (getSegments(segmentName).size() > 0);
    }

    public boolean hasSegment(SegmentName segmentName) {
        return (getSegments(segmentName).size() > 0);
    }

    public Segment getSegment(SegmentName segmentName) {
        List<Segment> segments = getSegments(segmentName);
        return Helpers.getSafely(segments, FIRST);
    }

    public List<Segment> getSegments(SegmentName segmentName) {
        Validate.notNull(segmentName);
        Validate.isTrue(!segmentName.equals(SegmentName.UNNAMED));

        return getSegments(segmentName.getValue());
    }

    public Segment getSegment(String segmentName) {
        List<Segment> segments = getSegments(segmentName);
        return Helpers.getSafely(segments, FIRST);
    }

    public List<Segment> getSegments(String segmentName) {
        Validate.notBlank(segmentName);

        return this.segments
                .stream()
                .filter(t -> t.getSegmentName().equals(segmentName))
                .collect(Collectors.toList());
    }

    //////////////////  Parsers  //////////////////

    private void parse() throws ParseException {
        normaliseLineEndings();
        detectSeperators();
        parseSegments();
    }

    private void normaliseLineEndings() {
        this.message = this.message.trim();
        this.message = this.message.replace(LF, CR);

        while (true) {
            int messageLength = this.message.length();

            this.message = this.message.replace(CR + CR, CR);

            if (this.message.length() == messageLength)
                break;
        }
    }

    private void detectSeperators() throws ParseException {
        this.seperators = new Seperators();

        String firstLine = StringUtils.split(this.message, this.seperators.getLineSeperator())[FIRST];

        if (!firstLine.startsWith(MSH_SEGMENT_NAME))
            throw new ParseException("message does not start with " + MSH_SEGMENT_NAME);

        String firstLineWithoutSegmentName = StringUtils.removeStart(firstLine, MSH_SEGMENT_NAME);

        if (firstLineWithoutSegmentName.length() < 5)
            throw new ParseException(MSH_SEGMENT_NAME + " does not encoding characters");

        this.seperators
                .setFieldSeperator(firstLineWithoutSegmentName.substring(0, 1))
                .setComponentSeperator(firstLineWithoutSegmentName.substring(1, 2))
                .setRepetitionSeperator(firstLineWithoutSegmentName.substring(2, 3))
                .setEscapeCharacter(firstLineWithoutSegmentName.substring(3, 4))
                .setSubcomponentSeperator(firstLineWithoutSegmentName.substring(4, 5));

        if (!this.seperators.areSeperatorsUnique())
            throw new ParseException("Seperators are not unique");

        if (!this.message.contains(this.seperators.getFieldSeperator()))
            throw new ParseException("Field seperator does not appear to be correct");
    }

    private void parseSegments() throws ParseException {
        List<String> lines = Helpers.split(this.message, this.seperators.getLineSeperator());

        for (String line : lines)
            this.segments.add(Segment.parse(line, this.seperators));
    }
}
