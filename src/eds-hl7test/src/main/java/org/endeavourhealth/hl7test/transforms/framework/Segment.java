package org.endeavourhealth.hl7test.transforms.framework;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7test.transforms.framework.namedsegments.SegmentName;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Segment {
    private static int FIRST = 0;
    private static int SEGMENT_NAME_LENGTH = 3;

    private String segment;
    private Seperators seperators;
    private String segmentName;
    protected List<Field> fields = new ArrayList<>();

    //region ** Constructors **

    public static Segment parse(String segment, Seperators seperators) throws ParseException {
        Validate.notBlank(segment);
        Validate.notNull(seperators);

        String segmentName = getSegmentName(segment, seperators);
        return SegmentName.instantiateSegment(segmentName, segment, seperators);
    }

    private Segment() {
    }

    public Segment(String segment, Seperators seperators) throws ParseException {
        Validate.notBlank(segment);
        Validate.notNull(seperators);

        this.segment = segment;
        this.seperators = seperators;

        this.parse();
    }

    //endregion

    //region ** Accessors **
    public String getSegmentName() {
        return this.segmentName;
    }

    public Field getField(int fieldNumber) {
        int fieldIndex = fieldNumber - 1;

        return Helpers.getSafely(this.fields, fieldIndex);
    }

    public String getComponentAsString(int fieldNumber, int componentNumber) {
        Field field = getField(fieldNumber);

        if (field == null)
            return null;

        return field.getComponent(componentNumber);
    }

    public LocalDateTime getFieldAsDate(int fieldNumber) throws ParseException {
        String field = getFieldAsString(fieldNumber);

        if (StringUtils.isBlank(field))
            return null;

        return Helpers.parseTS(field);
    }

    public Integer getFieldAsInteger(int fieldNumber) throws ParseException {
        String field = getFieldAsString(fieldNumber);

        if (StringUtils.isBlank(field))
            return null;

        return Integer.parseInt(field);
    }

    public String getFieldAsString(int fieldNumber) {
        Field field = getField(fieldNumber);

        if (field == null)
            return null;

        return field.getAsString();
    }

    //endregion

    //region ** Parsing **

    private static String getSegmentName(String segment, Seperators seperators) throws ParseException {
        List<String> tokens = Arrays.asList(StringUtils.split(segment, seperators.getFieldSeperator()));

        if (tokens.get(FIRST).length() != SEGMENT_NAME_LENGTH)
            throw new ParseException("Segment name is not three characters");

        return tokens.get(FIRST);
    }

    private void parse() throws ParseException {
        this.segmentName = getSegmentName(segment, seperators);

        List<String> tokens =
                Helpers.split(segment, seperators.getFieldSeperator(), true)
                .stream()
                .skip(1)
                .collect(Collectors.toList());

        if (SegmentName.MSH.getValue().equals(segmentName))
            tokens.add(FIRST, seperators.getFieldSeperator());

        for (String token : tokens)
            this.fields.add(new Field(token, this.seperators));
    }

    //endregion
}
