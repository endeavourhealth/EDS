package org.endeavourhealth.hl7receiver.hl7;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.primitive.CommonTS;
import ca.uhn.hl7v2.model.v23.datatype.TS;
import ca.uhn.hl7v2.util.Terser;

import java.time.LocalDateTime;
import java.time.ZoneId;

class HL7KeyFields {

    private static final String MSH_SEGMENT_NAME = "MSH";
    private static final int MSH_SENDING_APPLICATION_FIELD = 3;
    private static final int MSH_SENDING_FACILITY_FIELD = 4;
    private static final int MSH_RECEIVING_APPLICATION_FIELD = 5;
    private static final int MSH_RECEIVING_FACILITY_FIELD = 6;
    private static final int MSH_MESSAGE_DATE_TIME_FIELD = 7;
    private static final int MSH_MESSAGE_TYPE_FIELD = 9;
    private static final int MSH_MESSAGE_CONTROL_ID_FIELD = 10;
    private static final int MSH_SEQUENCE_NUMBER_FIELD = 13;

    private String encodedMessage;
    private String sendingApplication;
    private String sendingFacility;
    private String receivingApplication;
    private String receivingFacility;
    private LocalDateTime messageDateTime;
    private String messageType;
    private String messageControlId;
    private String sequenceNumber;

    public static HL7KeyFields parse(Message message) throws HL7Exception {

        Terser terser = new Terser(message);

        HL7KeyFields hl7KeyFields = new HL7KeyFields();

        hl7KeyFields.encodedMessage = new DefaultHapiContext().getPipeParser().encode(message);
        hl7KeyFields.sendingApplication = getFieldAsString(terser, MSH_SEGMENT_NAME, MSH_SENDING_APPLICATION_FIELD);
        hl7KeyFields.sendingFacility = getFieldAsString(terser, MSH_SEGMENT_NAME, MSH_SENDING_FACILITY_FIELD);
        hl7KeyFields.receivingApplication = getFieldAsString(terser, MSH_SEGMENT_NAME, MSH_RECEIVING_APPLICATION_FIELD);
        hl7KeyFields.receivingFacility = getFieldAsString(terser, MSH_SEGMENT_NAME, MSH_RECEIVING_FACILITY_FIELD);
        hl7KeyFields.messageDateTime = getFieldAsDate(terser, MSH_SEGMENT_NAME, MSH_MESSAGE_DATE_TIME_FIELD);
        hl7KeyFields.messageType = getFieldAsString(terser, MSH_SEGMENT_NAME, MSH_MESSAGE_TYPE_FIELD);
        hl7KeyFields.messageControlId = getFieldAsString(terser, MSH_SEGMENT_NAME, MSH_MESSAGE_CONTROL_ID_FIELD);
        hl7KeyFields.sequenceNumber = getFieldAsString(terser, MSH_SEGMENT_NAME, MSH_SEQUENCE_NUMBER_FIELD);

        return hl7KeyFields;
    }

    private static LocalDateTime getFieldAsDate(Terser terser, String segmentName, int fieldNumber) throws HL7Exception {
        String field = getFieldAsString(terser, segmentName, fieldNumber);

        CommonTS ts = new CommonTS(field);
        return LocalDateTime.ofInstant(ts.getValueAsDate().toInstant(), ZoneId.systemDefault());
    }

    private static String getFieldAsString(Terser terser, String segmentName, int fieldNumber) throws HL7Exception {
        Segment segment = terser.getSegment(segmentName);

        if (segment == null)
            return null;

        Type[] types = segment.getField(fieldNumber);

        if (types == null)
            return null;

        if (types.length == 0)
            return null;

        return types[0].encode();
    }

    public String getEncodedMessage() {
        return encodedMessage;
    }

    public String getSendingApplication() {
        return sendingApplication;
    }

    public String getSendingFacility() {
        return sendingFacility;
    }

    public String getReceivingApplication() {
        return receivingApplication;
    }

    public String getReceivingFacility() {
        return receivingFacility;
    }

    public LocalDateTime getMessageDateTime() {
        return messageDateTime;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getMessageControlId() {
        return messageControlId;
    }

    public String getSequenceNumber() {
        return sequenceNumber;
    }
}
