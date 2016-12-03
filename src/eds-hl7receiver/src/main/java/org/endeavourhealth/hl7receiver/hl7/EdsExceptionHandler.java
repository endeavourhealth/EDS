package org.endeavourhealth.hl7receiver.hl7;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.protocol.ReceivingApplicationExceptionHandler;

import java.util.Map;

public class EdsExceptionHandler implements ReceivingApplicationExceptionHandler{

    public String processException(String incomingMessage, Map<String, Object> incomingMetadata, String outgoingMessage, Exception exception) throws HL7Exception {
        exception.printStackTrace();
        return "";
    }
}
