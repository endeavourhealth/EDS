package org.endeavourhealth.hl7test.transforms.framework.segments;

import org.endeavourhealth.hl7test.transforms.framework.Field;
import org.endeavourhealth.hl7test.transforms.framework.ParseException;
import org.endeavourhealth.hl7test.transforms.framework.Seperators;

public class MshSegment extends Segment {
    public MshSegment(String line, Seperators seperators) throws ParseException {
        super(line, seperators);
    }

    public String getFieldSeparator() {
        return this.getFieldAsString(1);
    }
    public String getEncodingCharacters() {
        return this.getFieldAsString(2);
    }
    private String getSendingApplication() {
        return this.getFieldAsString(3);
    }
    private Field getSendingApplicationField() {
        return this.getField(3);
    }
    private String sendingFacility;
    private String receivingApplication;
    private String receivingFacility;
    private String dateTimeOfMessage;
    private String Security;
    private String messageType;
    private String messageControlID;
    private String processingID;
    private String versionID;
    private String sequenceNumber;
    private String continuationPointer;
    private String acceptAcknowledgmentType;
    private String ApplicationAcknowledgmentType;
    private String CountryCode;
    private String CharacterSet;
    private String PrincipalLanguageOfMessage;
}
