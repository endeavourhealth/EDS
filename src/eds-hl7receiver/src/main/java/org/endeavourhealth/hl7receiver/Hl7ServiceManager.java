package org.endeavourhealth.hl7receiver;

import org.endeavourhealth.hl7receiver.hl7.Hl7Channel;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Hl7ServiceManager {

    private static final Logger LOG = LoggerFactory.getLogger(Hl7ServiceManager.class);
    private Configuration configuration;
    private List<Hl7Channel> channels;

    public Hl7ServiceManager(Configuration configuration) throws SQLException {
        this.configuration = configuration;
        this.channels = new ArrayList<>();

        createChannels();
    }

    private void createChannels() throws SQLException {
        for (DbChannel dbChannel : configuration.getDbConfiguration().getDbChannels())
            channels.add(new Hl7Channel(dbChannel, configuration));
    }

    public void start() throws InterruptedException {
        for (Hl7Channel channel : channels)
            channel.start();
    }

    public void stop() {
        for (Hl7Channel channel : channels)
            channel.stop();
    }
}
