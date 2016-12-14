package org.endeavourhealth.hl7receiver.hl7;

import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionListener;
import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.DataLayer;
import org.endeavourhealth.hl7receiver.logging.Logger;
import org.endeavourhealth.hl7receiver.model.application.RemoteConnection;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.endeavourhealth.utilities.postgres.PgStoredProcException;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class HL7ConnectionManager implements ConnectionListener  {
    private static final Logger LOG = Logger.getLogger(HL7ConnectionManager.class);

    private Configuration configuration;
    private DbChannel dbChannel;
    private DataLayer dataLayer;
    private ConcurrentHashMap<RemoteConnection, Integer> connectionIds = new ConcurrentHashMap<>();

    private HL7ConnectionManager() {
    }

    public HL7ConnectionManager(Configuration configuration, DbChannel dbChannel) throws SQLException {
        this.configuration = configuration;
        this.dbChannel = dbChannel;
        this.dataLayer = new DataLayer(configuration.getDatabaseConnection());
    }

    public void connectionReceived(Connection connection) {
        recordConnectionOpened(connection);
    }

    public void connectionDiscarded(Connection connection) {
        recordConnectionClosed(connection);
    }

    public void closeConnections() {
        for (RemoteConnection connection : connectionIds.keySet())
            connection.getConnection().close();
    }

    public Integer getConnectionId(String ipAddress, int port) {
        return connectionIds.get(new RemoteConnection(ipAddress, port));
    }

    private void recordConnectionOpened(Connection connection) {
        String remoteHost = connection.getRemoteAddress().getHostAddress();
        Integer remotePort = connection.getRemotePort();

        LOG.info("Connection opened on channel " + dbChannel.getChannelName() + " from remote host " + remoteHost + " using remote port " + remotePort.toString());

        Integer connectionId = null;

        try {
            connectionId = dataLayer.openConnection(configuration.getInstanceName(), dbChannel.getChannelName(), dbChannel.getPortNumber(), remoteHost, remotePort);

        } catch (PgStoredProcException e) {
            LOG.error("Could not write new connection to database for channel " + dbChannel.getChannelName() + ". Closing inbound connection", e);
            connection.close();
        }

        if (connectionId != null)
            this.connectionIds.put(new RemoteConnection(connection), connectionId);
    }

    private void recordConnectionClosed(Connection connection) {
        String remoteHost = connection.getRemoteAddress().getHostAddress();
        Integer remotePort = connection.getRemotePort();

        LOG.info("Connection closed on channel " + dbChannel.getChannelName() + " from remote host " + remoteHost + " using remote port " + remotePort.toString());

        Integer connectionId = this.connectionIds.remove(new RemoteConnection(connection));

        try {
            dataLayer.closeConnection(connectionId);
        } catch (Exception e) {
            LOG.error("Could not close connection on channel " + dbChannel.getChannelName(), e);
        }
    }
}
