package org.endeavourhealth.hl7receiver.hl7;

import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.DataLayer;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class HL7ChannelForwarder implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(HL7ChannelForwarder.class);

    private Thread thread;
    private Configuration configuration;
    private DbChannel dbChannel;
    private DataLayer dataLayer;
    private volatile boolean stopRequested = false;

    public HL7ChannelForwarder(Configuration configuration, DbChannel dbChannel) throws SQLException {
        this.configuration = configuration;
        this.dbChannel = dbChannel;
        this.dataLayer = new DataLayer(configuration.getDatabaseConnection());

    }

    public void start() {
        if (thread == null)
            thread = new Thread(this);

        thread.start();
    }

    public void stop() {
        stopRequested = true;
        try {
            thread.join(10000);
        } catch (Exception e) {
            LOG.error("Error stopping channel forwarder for channel", e);
        }
    }

    @Override
    public void run() {
        try {
            boolean claimedMutex = false;
            LocalDateTime claimedDateTime = null;

            while (!stopRequested) {

                claimedMutex = dataLayer.claimChannelForwarderMutex(dbChannel.getChannelId(), configuration.getDbConfiguration().getInstanceId());

                if (claimedMutex) {
                    claimedDateTime = LocalDateTime.now();

                    while (claimedDateTime.plusSeconds(10).isAfter(LocalDateTime.now()) && (!stopRequested)) {

                        Thread.sleep(1000);

                    }
                }
            }
        }
        catch (Exception e) {
            LOG.error("Fatal exception in channel forwarder for channel " + dbChannel.getChannelName() + " for instance " + configuration.getDbConfiguration().getInstanceId(), e);
        }
    }
}
