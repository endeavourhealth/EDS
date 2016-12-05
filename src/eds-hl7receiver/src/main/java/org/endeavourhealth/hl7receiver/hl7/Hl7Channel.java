package org.endeavourhealth.hl7receiver.hl7;


import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionListener;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationExceptionHandler;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7receiver.Main;
import org.endeavourhealth.hl7receiver.model.db.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Hl7Channel {

    private static final Logger LOG = LoggerFactory.getLogger(Hl7Channel.class);
    private HapiContext context;
    private HL7Service server;
    private ReceivingApplication receiver;
    private Channel configuration;

    public Hl7Channel(Channel channel) {
        Validate.notNull(channel);
        Validate.notBlank(channel.getChannelName());
        Validate.isTrue(channel.getPortNumber() > 0);

        this.configuration = channel;

        context = new DefaultHapiContext();
        server = context.newServer(channel.getPortNumber(), channel.isUseTls());
    }

    public Channel getConfiguration() {
        return configuration;
    }

    public void start() throws InterruptedException {
        LOG.info("Starting channel " + configuration.getChannelName() + " on port " + Integer.toString(configuration.getPortNumber()));

        receiver = new Hl7ReceiverApplication();
        server.registerApplication("*", "*", receiver);
        server.registerConnectionListener(new Hl7ConnectionListener());
        server.setExceptionHandler(new Hl7ExceptionHandler());
        server.startAndWait();
    }

    public void stop() {
        LOG.info("Stopping channel " + configuration.getChannelName() + " on port " + Integer.toString(configuration.getPortNumber()));

        server.stopAndWait();
    }

    ////////////////////////////////////////////////////////////////////
    // ConnectionListener
    ////////////////////////////////////////////////////////////////////
    class Hl7ConnectionListener implements ConnectionListener {
        public void connectionReceived(Connection connection) {
            System.out.println("New connection received: " + connection.getRemoteAddress().toString());
        }

        public void connectionDiscarded(Connection connection) {
            System.out.println("Lost connection from: " + connection.getRemoteAddress().toString());
        }
    }

    ////////////////////////////////////////////////////////////////////
    // ExceptionHandler
    ////////////////////////////////////////////////////////////////////
    class Hl7ExceptionHandler implements ReceivingApplicationExceptionHandler {

        public String processException(String incomingMessage, Map<String, Object> incomingMetadata, String outgoingMessage, Exception exception) throws HL7Exception {
            exception.printStackTrace();
            return "";
        }
    }
}
