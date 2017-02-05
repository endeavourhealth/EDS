package org.endeavourhealth.hl7test.hl7v2.transform;

import org.endeavourhealth.hl7test.hl7v2.parser.Helpers;
import org.endeavourhealth.hl7test.hl7v2.parser.ParseException;
import org.endeavourhealth.hl7test.hl7v2.parser.segments.MshSegment;
import org.hl7.fhir.instance.model.*;

import java.time.LocalDateTime;

public class MessageHeaderTransform {
    private static final String CODE_SYSTEM_HL7V2_MESSAGE_TYPE = "http://endeavourhealth.org/fhir/v2-message-type";
    private static final String EXTENSION_HL7V2_DESTINATION_SOFTWARE = "http://endeavourhealth.org/fhir/StructureDefinition/message-header-destination-software-extension";
    private static final String EXTENSION_HL7V2_MESSAGE_CONTROL_ID = "http://endeavourhealth.org/fhir/StructureDefinition/message-header-message-control-id-extension";
    private static final String EXTENSION_HL7V2_SEQUENCE_NUMBER = "http://endeavourhealth.org/fhir/StructureDefinition/message-header-sequence-number-extension";

    public static MessageHeader fromHl7v2(MshSegment source) throws ParseException {
        MessageHeader target = new MessageHeader();

        LocalDateTime sourceMessageDateTime = source.getDateTimeOfMessage();

        if (sourceMessageDateTime != null)
            target.setTimestamp(Helpers.toDate(sourceMessageDateTime));

        target.setEvent(new Coding()
                .setCode(source.getMessageType())
                .setVersion(source.getVersionId())
                .setSystem(CODE_SYSTEM_HL7V2_MESSAGE_TYPE));

        target.getSource()
                .setName(source.getSendingFacility())
                .setSoftware(source.getSendingApplication());

        target.addDestination()
                .setName(source.getReceivingFacility())
                .addExtension(createStringExtension(EXTENSION_HL7V2_DESTINATION_SOFTWARE, source.getReceivingApplication()));

        target.addExtension(createStringExtension(EXTENSION_HL7V2_MESSAGE_CONTROL_ID, source.getMessageControlId()));

        Integer sequenceNumber = source.getSequenceNumber();

        if (sequenceNumber != null)
            target.addExtension(createIntegerExtension(EXTENSION_HL7V2_SEQUENCE_NUMBER, sequenceNumber));

        return target;
    }

    public static Extension createIntegerExtension(String uri, Integer value) {
        return new Extension()
                .setUrl(uri)
                .setValue(new IntegerType(value));
    }

    public static Extension createStringExtension(String uri, String value) {
        return new Extension()
                .setUrl(uri)
                .setValue(new StringType(value));
    }
}
