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
import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.model.db.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Hl7Channel {

    private static final Logger LOG = LoggerFactory.getLogger(Hl7Channel.class);
    private HapiContext context;
    private HL7Service service;
    private Hl7MessageHandler messageHandler;
    private Hl7ConnectionListener connectionListener;
    private Hl7ExceptionHandler exceptionHandler;
    private Channel channel;
    private Configuration configuration;

    public Hl7Channel(Channel channel, Configuration configuration) {
        Validate.notNull(channel);
        Validate.notBlank(channel.getChannelName());
        Validate.isTrue(channel.getPortNumber() > 0);

        this.channel = channel;
        this.configuration = configuration;

        context = new DefaultHapiContext();
        service = context.newServer(channel.getPortNumber(), channel.isUseTls());
    }

    public Channel getChannel() {
        return channel;
    }

    public void start() throws InterruptedException {
        LOG.info("Starting channel " + channel.getChannelName() + " on port " + Integer.toString(channel.getPortNumber()));

        messageHandler = new Hl7MessageHandler(channel, configuration);
        connectionListener = new Hl7ConnectionListener(channel, configuration);
        exceptionHandler = new Hl7ExceptionHandler(channel, configuration);

        service.registerApplication("*", "*", messageHandler);
        service.registerConnectionListener(connectionListener);
        service.setExceptionHandler(exceptionHandler);
        service.startAndWait();
    }

    public void stop() {
        LOG.info("Stopping channel " + channel.getChannelName() + " on port " + Integer.toString(channel.getPortNumber()));

        service.stopAndWait();
    }
}
