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

    private final String originalMessageText;    // originalMessageText may not reflect the current state of the message
    private Seperators seperators;
    private List<Segment> segments;

    //////////////////  Constructors  //////////////////

    public Message(final String messageText) throws ParseException {
        Validate.notBlank(messageText);

        this.originalMessageText = messageText;
        parse(messageText);
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

    public List<Segment> getSegments() {
        return this.segments;
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

    private void parse(String messageText) throws ParseException {
        String cleanedMessageText = normaliseLineEndings(messageText);

        this.seperators = detectSeperators(cleanedMessageText);
        this.segments = parseSegments(cleanedMessageText, seperators);
    }

    private static String normaliseLineEndings(String message) {
        message = message.trim();
        message = message.replace(LF, CR);

        while (true) {
            int messageLength = message.length();

            message = message.replace(CR + CR, CR);

            if (message.length() == messageLength)
                break;
        }

        return message;
    }

    private static Seperators detectSeperators(String messageText) throws ParseException {
        Seperators seperators = new Seperators();

        String firstLine = StringUtils.split(messageText, seperators.getLineSeperator())[FIRST];

        if (!firstLine.startsWith(MSH_SEGMENT_NAME))
            throw new ParseException("message does not start with " + MSH_SEGMENT_NAME + " segment");

        String firstLineWithoutSegmentName = StringUtils.removeStart(firstLine, MSH_SEGMENT_NAME);

        if (firstLineWithoutSegmentName.length() < 5)
            throw new ParseException(MSH_SEGMENT_NAME + " does not encoding characters");

        seperators
                .setFieldSeperator(firstLineWithoutSegmentName.substring(0, 1))
                .setComponentSeperator(firstLineWithoutSegmentName.substring(1, 2))
                .setRepetitionSeperator(firstLineWithoutSegmentName.substring(2, 3))
                .setEscapeCharacter(firstLineWithoutSegmentName.substring(3, 4))
                .setSubcomponentSeperator(firstLineWithoutSegmentName.substring(4, 5));

        if (!seperators.areSeperatorsUnique())
            throw new ParseException("Seperators are not unique");

        if (!messageText.contains(seperators.getFieldSeperator()))
            throw new ParseException("Field seperator does not appear to be correct");

        return seperators;
    }

    private static List<Segment> parseSegments(String messageText, Seperators seperators) throws ParseException {
        List<Segment> segments = new ArrayList<>();

        List<String> lines = Helpers.split(messageText, seperators.getLineSeperator());

        for (String line : lines)
            segments.add(Segment.parse(line, seperators));

        return segments;
    }

    //////////////////  Composers  //////////////////

    public String compose() {
        return String.join(this.seperators.getLineSeperator(),
                getSegments()
                        .stream()
                        .map(t -> t.compose())
                        .collect(Collectors.toList()));
    }
}
