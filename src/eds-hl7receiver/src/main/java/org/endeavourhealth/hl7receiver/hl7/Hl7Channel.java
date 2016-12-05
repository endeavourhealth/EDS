package org.endeavourhealth.hl7receiver.hl7;


import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7receiver.model.db.Channel;

public class Hl7Channel {

    private HapiContext context;
    private HL7Service server;
    private ReceivingApplication receiver;
    private Channel configuration;

    public Hl7Channel(Channel channel) {
        Validate.notNull(channel);

        context = new DefaultHapiContext();
        server = context.newServer(channel.getPortNumber(), channel.isUseTls());
    }

    public Channel getConfiguration() {
        return configuration;
    }

    public void start() throws InterruptedException {
        receiver = new Hl7ReceiverApplication();
        server.registerApplication("*", "*", receiver);
        server.registerConnectionListener(new Hl7ConnectionListener());
        server.setExceptionHandler(new Hl7ExceptionHandler());
        server.startAndWait();
    }

    public void stop() {
        server.stopAndWait();
    }
}
