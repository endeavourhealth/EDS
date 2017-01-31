package org.endeavourhealth.hl7test.transforms.framework;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Segment {
    private static int FIRST = 0;
    private static int SEGMENT_NAME_LENGTH = 3;

    private String line;
    private Seperators seperators;
    private String segmentName;
    private List<Field> fields;

    public Segment(String line, Seperators seperators) throws ParseException {
        if (StringUtils.isBlank(line))
            throw new ParseException("line is blank");

        if (seperators == null)
            throw new ParseException("seperators is null");

        this.line = line;
        this.seperators = seperators;

        parseSegment();
    }

    private void parseSegment() throws ParseException {
        this.fields = new ArrayList<Field>();

        List<String> tokens = Arrays.asList(StringUtils.split(line, seperators.getFieldSeperator()));

        if (tokens.get(FIRST).length() != SEGMENT_NAME_LENGTH)
            throw new ParseException("Segment name is not three characters");

        this.segmentName = tokens.get(FIRST);

        tokens = tokens
                .stream()
                .skip(1)
                .collect(Collectors.toList());

        for (String token : tokens)
            this.fields.add(new Field(token, this.seperators));
    }

    public String getSegmentName() {
        return this.segmentName;
    }
}
