package org.endeavourhealth.hl7receiver.hl7;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.HL7Service;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.DataLayer;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class Hl7Channel {
    private static final Logger LOG = LoggerFactory.getLogger(Hl7Channel.class);

    private HapiContext context;
    private HL7Service service;
    private DbChannel dbChannel;
    private Configuration configuration;
    private DataLayer dataLayer;
    private Hl7ConnectionManager connectionManager;
    private Hl7MessageReceiver messageReceiver;
    private Hl7ExceptionHandler exceptionHandler;

    private Hl7Channel() {
    }

    public Hl7Channel(DbChannel dbChannel, Configuration configuration) throws SQLException {
        Validate.notNull(dbChannel);
        Validate.notBlank(dbChannel.getChannelName());
        Validate.isTrue(dbChannel.getPortNumber() > 0);

        this.dbChannel = dbChannel;
        this.configuration = configuration;

        this.dataLayer = new DataLayer(configuration.getDatabaseConnection());

        context = new DefaultHapiContext();
        connectionManager = new Hl7ConnectionManager(configuration, dbChannel);
        messageReceiver = new Hl7MessageReceiver(configuration, dbChannel, connectionManager);
        exceptionHandler = new Hl7ExceptionHandler();
        service = context.newServer(dbChannel.getPortNumber(), false);

        service.registerApplication("*", "*", messageReceiver);
        service.registerConnectionListener(connectionManager);
        service.setExceptionHandler(exceptionHandler);
    }

    public void start() throws InterruptedException {
        LOG.info("Starting channel " + dbChannel.getChannelName() + " on port " + Integer.toString(dbChannel.getPortNumber()));
        service.startAndWait();
    }

    public void stop() {
        LOG.info("Stopping channel " + dbChannel.getChannelName() + " on port " + Integer.toString(dbChannel.getPortNumber()));
        connectionManager.closeConnections();
        service.stopAndWait();
    }
}
