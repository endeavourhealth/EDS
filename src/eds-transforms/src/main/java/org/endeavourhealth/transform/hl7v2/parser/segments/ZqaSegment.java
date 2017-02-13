package org.endeavourhealth.transform.hl7v2.parser.segments;

import org.endeavourhealth.transform.hl7v2.parser.*;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.*;

public class ZqaSegment extends Segment {
    public ZqaSegment(String segment, Seperators seperators) throws ParseException {
        super(segment, seperators);
    }

    //Repeatable Segment

    public int getSetId() throws ParseException { return this.getFieldAsInteger(1); }
    public String getQuestionnaireId() { return this.getFieldAsString(2); }
    public Zqa getQuestionAndAnswer() { return this.getFieldAsDatatype(3, Zqa.class); }
    public String getCombinedAnswer() { return this.getFieldAsString(8); }
}
