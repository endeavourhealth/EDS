package org.endeavourhealth.hl7receiver.hl7;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

class Hl7MessageHandler implements ReceivingApplication {
    private static final Logger LOG = LoggerFactory.getLogger(Hl7MessageHandler.class);

    private DbChannel dbChannel;
    private Configuration configuration;

    public Hl7MessageHandler(DbChannel dbChannel, Configuration configuration) {
        this.dbChannel = dbChannel;
        this.configuration = configuration;
    }

    public Message processMessage(Message message, Map<String, Object> map) throws ReceivingApplicationException, HL7Exception {
        String encodedMessage = new DefaultHapiContext().getPipeParser().encode(message);

        LOG.info("Received message:\n" + encodedMessage + "\n\n");

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
