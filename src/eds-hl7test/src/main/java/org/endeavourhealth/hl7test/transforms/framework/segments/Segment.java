package org.endeavourhealth.hl7test.transforms.framework.segments;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7test.transforms.framework.Field;
import org.endeavourhealth.hl7test.transforms.framework.ParseException;
import org.endeavourhealth.hl7test.transforms.framework.Seperators;

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
    protected List<Field> fields;

    //region ** Constructors **

    public static Segment parse(String line, Seperators seperators) throws ParseException {
        Validate.notBlank(line);
        Validate.notNull(seperators);

        String segmentName = getSegmentName(line, seperators);
        return SegmentName.instantiateSegment(segmentName, line, seperators);
    }

    private Segment() {
    }

    public Segment(String line, Seperators seperators) throws ParseException {
        Validate.notBlank(line);
        Validate.notNull(seperators);

        this.line = line;
        this.seperators = seperators;

        parseSegment();
    }

    //endregion

    //region ** Accessors **
    public String getSegmentName() {
        return this.segmentName;
    }

    public Field getField(int fieldNumber) {
        int fieldIndex = fieldNumber - 1;

        if ((fieldIndex >= 0) && (fieldIndex < (fields.size() - 1)))
            return fields.get(fieldIndex);

        return null;
    }

    public String getFieldComponentAsString(int fieldNumber, int componentNumber) {
        Field field = getField(fieldNumber);

        if (field == null)
            return null;

        return "";


    }

    public String getFieldAsString(int fieldNumber) {
        Field field = getField(fieldNumber);

        if (field == null)
            return field.getAsString();

        return null;
    }

    //endregion

    //region ** Parsing **

    private static String getSegmentName(String line, Seperators seperators) throws ParseException {
        List<String> tokens = Arrays.asList(StringUtils.split(line, seperators.getFieldSeperator()));

        if (tokens.get(FIRST).length() != SEGMENT_NAME_LENGTH)
            throw new ParseException("Segment name is not three characters");

        return tokens.get(FIRST);
    }

    private void parseSegment() throws ParseException {
        this.segmentName = getSegmentName(line, seperators);

        this.fields = new ArrayList<>();

        List<String> tokens =
                Arrays.stream(StringUtils.split(line, seperators.getFieldSeperator()))
                .skip(1)
                .collect(Collectors.toList());

        if (SegmentName.MSH.getValue().equals(segmentName))
            tokens.add(FIRST, seperators.getFieldSeperator());

        for (String token : tokens)
            this.fields.add(new Field(token, this.seperators));
    }

    //endregion
}
