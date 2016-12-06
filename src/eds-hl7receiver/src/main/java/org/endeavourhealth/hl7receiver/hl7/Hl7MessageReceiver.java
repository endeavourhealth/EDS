package org.endeavourhealth.hl7receiver.hl7;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.DataLayer;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.endeavourhealth.utilities.postgres.PgStoredProcException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class Hl7MessageReceiver implements ReceivingApplication {

    private Configuration configuration;
    private DbChannel dbChannel;
    private Hl7ConnectionManager connectionManager;
    private DataLayer dataLayer;

    private Hl7MessageReceiver() {
    }

    public Hl7MessageReceiver(Configuration configuration, DbChannel dbChannel, Hl7ConnectionManager connectionManager) throws SQLException {
        this.configuration = configuration;
        this.dbChannel = dbChannel;
        this.connectionManager = connectionManager;

        this.dataLayer = new DataLayer(configuration.getDatabaseConnection());
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
