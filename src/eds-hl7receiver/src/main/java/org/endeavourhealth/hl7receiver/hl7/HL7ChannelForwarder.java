package org.endeavourhealth.hl7receiver.hl7;

import org.endeavourhealth.core.eds.EdsSender;
import org.endeavourhealth.hl7receiver.Configuration;
import org.endeavourhealth.hl7receiver.DataLayer;
import org.endeavourhealth.hl7receiver.model.db.DbChannel;
import org.endeavourhealth.hl7receiver.model.db.DbMessage;
import org.endeavourhealth.hl7receiver.model.db.DbNotificationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class HL7ChannelForwarder implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(HL7ChannelForwarder.class);
    private static final int LOCK_RECLAIM_INTERVAL_SECONDS = 60;
    private static final int LOCK_BREAK_OTHERS_SECONDS = 360;
    private static final int THREAD_SLEEP_TIME_MILLIS = 1000;

    private Thread thread;
    private Configuration configuration;
    private DbChannel dbChannel;
    private DataLayer dataLayer;
    private volatile boolean stopRequested = false;
    private boolean firstLockAttempt = true;

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
        boolean gotLock = false;
        LocalDateTime lastLockTried = null;

        try {
            while (!stopRequested) {

                gotLock = getLock(gotLock);
                lastLockTried = LocalDateTime.now();

                while ((!stopRequested) && (LocalDateTime.now().isBefore(lastLockTried.plusSeconds(LOCK_RECLAIM_INTERVAL_SECONDS)))) {

                    if (!gotLock) {
                        Thread.sleep(THREAD_SLEEP_TIME_MILLIS);
                        continue;
                    }

                    DbMessage message = dataLayer.getNextUnnotifiedMessage(dbChannel.getChannelId(), configuration.getInstanceId());

                    if (message == null) {
                        Thread.sleep(THREAD_SLEEP_TIME_MILLIS);
                        continue;
                    }

                    sendMessage(message);

                    dataLayer.updateMessageStatus(message.getMessageId(), DbNotificationStatus.SUCCEEDED);


                }
            }
        }
        catch (Exception e) {
            LOG.error("Fatal exception in channel forwarder {} for instance {}", new Object[] { dbChannel.getChannelName(), configuration.getInstanceId(), e });
        }

        releaseLock(gotLock);
    }

    private boolean sendMessage(DbMessage dbMessage) {

        UUID messageUuid = UUID.randomUUID();

        return true;

    }

    private boolean getLock(boolean currentlyHaveLock) {
        try {
            boolean gotLock = dataLayer.getChannelForwarderLock(dbChannel.getChannelId(), configuration.getInstanceId(), LOCK_BREAK_OTHERS_SECONDS);

            if (firstLockAttempt || (currentlyHaveLock != gotLock))
                LOG.info((gotLock ? "G" : "Not g") + "ot lock on channel {} for instance {}", dbChannel.getChannelName(), configuration.getMachineName());

            firstLockAttempt = false;

            return gotLock;
        } catch (Exception e) {
            LOG.error("Exception getting lock in channel forwarder for channel {} for instance {}", new Object[] { dbChannel.getChannelName(), configuration.getMachineName(), e });
        }

        return false;
    }

    private void releaseLock(boolean currentlyHaveLock) {
        try {
            if (currentlyHaveLock)
                LOG.info("Releasing lock on channel {} for instance {}", dbChannel.getChannelName(), configuration.getMachineName());

            dataLayer.releaseChannelForwarderLock(dbChannel.getChannelId(), configuration.getInstanceId());
        } catch (Exception e) {
            LOG.error("Exception releasing lock in channel forwarder for channel {} for instance {}", new Object[] { e, dbChannel.getChannelName(), configuration.getMachineName() });
        }
    }
}
