package org.endeavourhealth.hl7receiver.hl7;


import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionListener;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.protocol.ReceivingApplicationExceptionHandler;
import org.apache.commons.lang3.Validate;
import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.DataLayer;
import org.endeavourhealth.hl7receiver.model.application.RemoteConnection;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.endeavourhealth.utilities.postgres.PgStoredProcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Hl7Channel implements ConnectionListener, ReceivingApplicationExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(Hl7Channel.class);

    private HapiContext context;
    private HL7Service service;
    private Hl7MessageHandler messageHandler;
    private DbChannel dbChannel;
    private Configuration configuration;
    private DataLayer dataLayer;
    private ConcurrentHashMap<RemoteConnection, Integer> connections = new ConcurrentHashMap<>();

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
        service = context.newServer(dbChannel.getPortNumber(), dbChannel.isUseTls());
        messageHandler = new Hl7MessageHandler(dbChannel, configuration);

        service.registerApplication("*", "*", messageHandler);
        service.registerConnectionListener(this);
        service.setExceptionHandler(this);
    }

    public DbChannel getDbChannel() {
        return dbChannel;
    }

    public void start() throws InterruptedException {
        LOG.info("Starting channel " + dbChannel.getChannelName() + " on port " + Integer.toString(dbChannel.getPortNumber()));
        service.startAndWait();
    }

    public void stop() {
        LOG.info("Stopping channel " + dbChannel.getChannelName() + " on port " + Integer.toString(dbChannel.getPortNumber()));
        closeConnections();
        service.stopAndWait();
    }

    public void connectionReceived(Connection connection) {
        recordConnectionOpened(connection);
    }

    public void connectionDiscarded(Connection connection) {
        recordConnectionClosed(connection);
    }

    private void closeConnections() {
        for (RemoteConnection connection : connections.keySet())
            connection.getConnection().close();
    }

    private void recordConnectionOpened(Connection connection) {
        String instanceName = configuration.getInstanceName();
        String channelName = dbChannel.getChannelName();
        Integer localPort = dbChannel.getPortNumber();
        String remoteHost = connection.getRemoteAddress().toString();
        Integer remotePort = connection.getRemotePort();

        LOG.info("Connection opened on channel " + channelName + " from remote host " + remoteHost + " using remote port " + remotePort.toString());

        Integer connectionId = null;

        try {
            connectionId = dataLayer.openConnection(instanceName, channelName, localPort, remoteHost, remotePort);

        } catch (PgStoredProcException e) {
            LOG.error("Could not write new connection to database for channel " + channelName, e);
            LOG.error("Closing inbound connection");
            connection.close();
        }

        if (connectionId != null)
            this.connections.put(new RemoteConnection(connection), connectionId);
    }

    private void recordConnectionClosed(Connection connection) {
        String channelName = dbChannel.getChannelName();
        String remoteHost = connection.getRemoteAddress().toString();
        Integer remotePort = connection.getRemotePort();

        LOG.info("Connection closed on channel " + channelName + " from remote host " + remoteHost + " using remote port " + remotePort.toString());

        Integer connectionId = this.connections.remove(new RemoteConnection(connection));

        try {
            dataLayer.closeConnection(connectionId);
        } catch (Exception e) {
            LOG.error("Could not close connection on channel");
        }
    }

    public String processException(String incomingMessage, Map<String, Object> incomingMetadata, String outgoingMessage, Exception exception) throws HL7Exception {
        exception.printStackTrace();
        return "";
    }
}
