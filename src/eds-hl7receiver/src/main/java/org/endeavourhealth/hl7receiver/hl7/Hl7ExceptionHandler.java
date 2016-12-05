package org.endeavourhealth.hl7receiver.hl7;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.protocol.ReceivingApplicationExceptionHandler;
import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.model.db.Channel;

import java.util.Map;

class Hl7ExceptionHandler implements ReceivingApplicationExceptionHandler {

    private Channel channel;
    private Configuration configuration;

    public Hl7ExceptionHandler(Channel channel, Configuration configuration) {
        this.channel = channel;
        this.configuration = configuration;
    }

    public String processException(String incomingMessage, Map<String, Object> incomingMetadata, String outgoingMessage, Exception exception) throws HL7Exception {
        exception.printStackTrace();
        return "";
    }
}