package org.endeavourhealth.transform.hl7v2.parser;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.transform.hl7v2.parser.segments.SegmentName;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Segment {
    private static int FIRST = 0;
    private static int SEGMENT_NAME_LENGTH = 3;

    private String originalSegmentText;    // originalSegmentText may not reflect the current state of the segment
    private Seperators seperators;
    private String segmentName;
    protected List<Field> fields = new ArrayList<>();

    //////////////////  Constructors  //////////////////

    public static Segment parse(String segmentText, Seperators seperators) throws ParseException {
        Validate.notBlank(segmentText);
        Validate.notNull(seperators);

        String segmentName = getSegmentName(segmentText, seperators);
        return SegmentName.instantiateSegment(segmentName, segmentText, seperators);
    }

    private Segment() {
    }

    public Segment(String segmentText, Seperators seperators) throws ParseException {
        Validate.notBlank(segmentText);
        Validate.notNull(seperators);

        this.originalSegmentText = segmentText;
        this.seperators = seperators;

        this.parse();
    }

    //////////////////  Accessors  //////////////////

    public String getSegmentName() {
        return this.segmentName;
    }

    public Field getField(int fieldNumber) {
        int fieldIndex = fieldNumber - 1;

        return Helpers.getSafely(this.fields, fieldIndex);
    }

    public List<Field> getFields() {
        return this.fields;
    }

    public <T extends Datatype> T getFieldAsDatatype(int fieldNumber, Class<T> datatype) {
        Field field = getField(fieldNumber);

        if (field == null)
            return null;

        return field.getDatatype(datatype);
    }

    public <T extends Datatype> List<T> getFieldAsDatatypes(int fieldNumber, Class<T> datatype) {
        Field field = getField(fieldNumber);

        if (field == null)
            return null;

        return field.getDatatypes(datatype);
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

        return DateParser.parse(field);
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

    public List<String> getFieldAsStringList(int fieldNumber) {
        Field field = getField(fieldNumber);

        if (field == null)
            return null;

        return field.getDatatypesAsString();
    }

    //////////////////  Parsers  //////////////////

    private static String getSegmentName(String segment, Seperators seperators) throws ParseException {
        List<String> tokens = Arrays.asList(StringUtils.split(segment, seperators.getFieldSeperator()));

        if (tokens.get(FIRST).length() != SEGMENT_NAME_LENGTH)
            throw new ParseException("Segment name is not three characters");

        return tokens.get(FIRST);
    }

    private void parse() throws ParseException {
        this.segmentName = getSegmentName(originalSegmentText, seperators);

        List<String> tokens =
                Helpers.split(originalSegmentText, seperators.getFieldSeperator())
                .stream()
                .skip(1)
                .collect(Collectors.toList());

        if (SegmentName.MSH.getValue().equals(segmentName))
            tokens.add(FIRST, seperators.getFieldSeperator());

        for (String token : tokens)
            this.fields.add(new Field(token, this.seperators));
    }

    //////////////////  Composers  //////////////////

    public String compose() {
        return this.getSegmentName()
                + this.seperators.getFieldSeperator()
                + String.join(this.seperators.getFieldSeperator(),
                this
                        .getFields()
                        .stream()
                        .map(t -> t.compose())
                        .collect(Collectors.toList()));
    }
}
