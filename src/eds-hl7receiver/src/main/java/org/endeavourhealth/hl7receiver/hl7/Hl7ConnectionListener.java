package org.endeavourhealth.hl7receiver.hl7;

import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionListener;
import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.model.db.Channel;

class Hl7ConnectionListener implements ConnectionListener {

    private Channel channel;
    private Configuration configuration;

    public Hl7ConnectionListener(Channel channel, Configuration configuration) {
        this.channel = channel;
        this.configuration = configuration;
    }

    public void connectionReceived(Connection connection) {
        System.out.println("New connection received: " + connection.getRemoteAddress().toString());
    }

    public void connectionDiscarded(Connection connection) {
        System.out.println("Lost connection from: " + connection.getRemoteAddress().toString());
    }
}