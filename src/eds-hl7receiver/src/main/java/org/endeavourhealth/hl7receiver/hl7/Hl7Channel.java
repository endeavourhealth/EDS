package org.endeavourhealth.hl7receiver.hl7;


import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import ca.uhn.hl7v2.protocol.ReceivingApplicationExceptionHandler;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.DataLayer;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.endeavourhealth.utilities.postgres.PgStoredProcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class Hl7Channel implements ReceivingApplicationExceptionHandler, ReceivingApplication {
    private static final Logger LOG = LoggerFactory.getLogger(Hl7Channel.class);

    private HapiContext context;
    private HL7Service service;
    private DbChannel dbChannel;
    private Configuration configuration;
    private DataLayer dataLayer;
    private Hl7ConnectionManager connectionManager;

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
        service = context.newServer(dbChannel.getPortNumber(), false);

        service.registerApplication("*", "*", this);
        service.registerConnectionListener(connectionManager);
        service.setExceptionHandler(this);
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

    public String processException(String incomingMessage, Map<String, Object> incomingMetadata, String outgoingMessage, Exception exception) throws HL7Exception {
        exception.printStackTrace();
        return "";
    }

    public Message processMessage(Message message, Map<String, Object> map) throws ReceivingApplicationException, HL7Exception {
        Integer connectionId = getConnectionId(map);

        String encodedMessage = new DefaultHapiContext().getPipeParser().encode(message);

        try {
            dataLayer.logMessage(dbChannel.getChannelId(), connectionId, encodedMessage);
        } catch (PgStoredProcException e) {
            e.printStackTrace();
            throw new HL7Exception(e);
        }

        try {
            return message.generateACK();
        } catch (IOException e) {
            throw new HL7Exception(e);
        }
    }

    private Integer getConnectionId(Map<String, Object> map) {
        String remoteHost = (String)map.get("SENDING_IP");
        Integer remotePort = (Integer)map.get("SENDING_PORT");

        return connectionManager.getConnectionId(remoteHost, remotePort);
    }

    public boolean canProcess(Message message) {
        return true;
    }
}
