package org.endeavourhealth.hl7receiver.hl7;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.protocol.MetadataKeys;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import ca.uhn.hl7v2.util.Terser;
import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.DataLayer;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.endeavourhealth.utilities.postgres.PgStoredProcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class Hl7MessageReceiver implements ReceivingApplication {
    private static final Logger LOG = LoggerFactory.getLogger(Hl7MessageReceiver.class);

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

    public Message processMessage(Message message, Map<String, Object> map) throws ReceivingApplicationException, HL7Exception
    {
        Integer connectionId = getConnectionId(map);

        Terser terser = new Terser(message);

        String encodedMessage = new DefaultHapiContext().getPipeParser().encode(message);

        Integer messageId;

        try
        {
            messageId = dataLayer.addMessage(connectionId, encodedMessage);
        }
        catch (PgStoredProcException e)
        {
            LOG.error("Error logging message", e);
            throw new HL7Exception(e);
        }

        try
        {
            Message response = message.generateACK();

            dataLayer.updateMessageWithOutbound(messageId, response.encode());

            return response;
        }
        catch (Exception e)
        {
            LOG.error("Error logging message", e);
            throw new HL7Exception(e);
        }
    }

    private Integer getConnectionId(Map<String, Object> map) {
        String remoteHost = (String)map.get(MetadataKeys.IN_SENDING_IP);
        Integer remotePort = (Integer)map.get(MetadataKeys.IN_SENDING_PORT);

        return connectionManager.getConnectionId(remoteHost, remotePort);
    }

    public boolean canProcess(Message message) {
        return true;
    }

}
