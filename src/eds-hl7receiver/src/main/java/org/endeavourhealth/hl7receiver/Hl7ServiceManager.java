package org.endeavourhealth.hl7receiver;

import org.endeavourhealth.hl7receiver.hl7.Hl7Channel;
import org.endeavourhealth.hl7receiver.model.db.Channel;

import java.util.ArrayList;
import java.util.List;

public class Hl7ServiceManager {

    private Configuration configuration;
    private List<Hl7Channel> channels;

    public Hl7ServiceManager(Configuration configuration) {
        this.configuration = configuration;
        this.channels = new ArrayList<>();

        createChannels();
    }

    private void createChannels() {
        for (Channel channelConfiguration : configuration.getDbConfiguration().getChannels())
            channels.add(new Hl7Channel(channelConfiguration));
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
