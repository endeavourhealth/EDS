package org.endeavourhealth.hl7receiver.hl7;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import org.endeavourhealth.hl7receiver.model.xml.Hl7ReceiverConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class Hl7ReceiverApplication implements ReceivingApplication {

    private static final Logger LOG = LoggerFactory.getLogger(Hl7ReceiverApplication.class);

    public Message processMessage(Message message, Map<String, Object> map) throws ReceivingApplicationException, HL7Exception {
        String encodedMessage = new DefaultHapiContext().getPipeParser().encode(message);
        System.out.println("Received message:\n" + encodedMessage + "\n\n");

        try {
            return message.generateACK();
        } catch (IOException e) {
            throw new HL7Exception(e);
        }
    }

    public boolean canProcess(Message message) {
        return true;
    }
}
