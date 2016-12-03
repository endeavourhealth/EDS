package org.endeavourhealth.hl7receiver.hl7;


import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.protocol.ReceivingApplication;

public class EdsHl7Service {

    private static final int PORT = 8009;
    private static final Boolean USE_TLS = false;
    private HapiContext context;
    private HL7Service server;
    private ReceivingApplication receiver;

    public EdsHl7Service() {
        context = new DefaultHapiContext();
        server = context.newServer(PORT, USE_TLS);
    }

    public void start() throws InterruptedException {
        receiver = new EdsReceiverApplication();
        server.registerApplication("*", "*", receiver);
        server.registerConnectionListener(new EdsConnectionListener());
        server.setExceptionHandler(new EdsExceptionHandler());
        server.startAndWait();
    }

    public void stop() {
        server.stopAndWait();
    }
}
