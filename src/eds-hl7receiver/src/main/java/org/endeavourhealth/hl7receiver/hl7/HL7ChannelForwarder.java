package org.endeavourhealth.hl7receiver.hl7;

import org.endeavourhealth.core.postgres.PgStoredProcException;
import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.DataLayer;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class HL7ChannelForwarder implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(HL7ChannelForwarder.class);
    private static final int LOCK_RECLAIM_INTERVAL_SECONDS = 60;
    private static final int LOCK_BREAK_OTHERS_SECONDS = 360;

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
        LOG.info("Starting channel forwarder {}", dbChannel.getChannelName());

        if (thread == null)
            thread = new Thread(this);

        thread.start();
    }

    public void stop() {
        stopRequested = true;
        try {
            LOG.info("Stopping channel forwarder {}", dbChannel.getChannelName());
            thread.join(10000);
        } catch (Exception e) {
            LOG.error("Error stopping channel forwarder for channel", e);
        }
    }

    @Override
    public void run() {
        try {
            boolean gotLock = false;
            LocalDateTime lastLockTried = null;

            while (!stopRequested) {

                gotLock = getLock();
                lastLockTried = LocalDateTime.now();

                while ((!stopRequested) && (LocalDateTime.now().isAfter(lastLockTried.plusSeconds(LOCK_RECLAIM_INTERVAL_SECONDS)))) {

                    if (gotLock) {

                    } else {
                        Thread.sleep(1000);
                    }

                }
            }
        }
        catch (Exception e) {
            LOG.error("Fatal exception in channel forwarder for channel " + dbChannel.getChannelName() + " for instance " + configuration.getDbConfiguration().getInstanceId(), e);
        }

        releaseLock();
    }

    private boolean getLock() throws PgStoredProcException {
        return dataLayer.claimChannelForwarderMutex(dbChannel.getChannelId(), configuration.getDbConfiguration().getInstanceId(), LOCK_BREAK_OTHERS_SECONDS);
    }

    private void releaseLock() {
        try {
            dataLayer.releaseChannelForwarderMutex(dbChannel.getChannelId(), configuration.getDbConfiguration().getInstanceId());
        } catch (Exception e) {
            LOG.error("Exception releasing lock in channel forwarder for channel " + dbChannel.getChannelName() + " for instance " + configuration.getDbConfiguration().getInstanceId(), e);
        }
    }
}
