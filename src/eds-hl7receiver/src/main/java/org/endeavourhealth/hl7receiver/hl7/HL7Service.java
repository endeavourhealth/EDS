package org.endeavourhealth.hl7receiver.hl7;

import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.logging.Logger;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HL7Service {

    private static final Logger LOG = Logger.getLogger(HL7Service.class);
    private Configuration configuration;
    private List<HL7Channel> channels;

    public HL7Service(Configuration configuration) throws SQLException {
        this.configuration = configuration;
        this.channels = new ArrayList<>();

        createChannels();
    }

    private void createChannels() throws SQLException {
        for (DbChannel dbChannel : configuration.getDbConfiguration().getDbChannels())
            channels.add(new HL7Channel(dbChannel, configuration));
    }

    public void start() throws InterruptedException {
        for (HL7Channel channel : channels)
            channel.start();
    }

    public void stop() {
        for (HL7Channel channel : channels)
            channel.stop();
    }
}
