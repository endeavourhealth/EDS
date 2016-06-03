package org.endeavourhealth.ui.utility;

import org.endeavourhealth.ui.queuing.QueueConnectionProperties;
import org.endeavourhealth.ui.queuing.controller.ControllerQueue;
import org.endeavourhealth.ui.queuing.controller.ControllerQueueRequestStartMessage;
import org.endeavourhealth.ui.queuing.controller.ControllerQueueRequestStopMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MessagingQueueProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MessagingQueueProvider.class);

    private String url = null;
    private String password = null;
    private String username = null;
    private String queueName = null;

    //singleton
    private static MessagingQueueProvider ourInstance = new MessagingQueueProvider();
    public static MessagingQueueProvider getInstance() {
        return ourInstance;
    }

    public void setConnectionProperties(String url, String username, String password, String queueName) {
        this.url = url;
        this.password = password;
        this.username = username;
        this.queueName = queueName;
    }


    public void startProcessor() throws Exception {
        QueueConnectionProperties queueConnectionProperties = new QueueConnectionProperties(url, username, password);
        ControllerQueue controllerQueue = new ControllerQueue(queueConnectionProperties, queueName);
        controllerQueue.sendMessage(ControllerQueueRequestStartMessage.CreateAsNew());
    }
    public void stopProcessor() throws Exception {
        QueueConnectionProperties queueConnectionProperties = new QueueConnectionProperties(url, username, password);
        ControllerQueue controllerQueue = new ControllerQueue(queueConnectionProperties, queueName);
        controllerQueue.sendMessage(ControllerQueueRequestStopMessage.CreateAsNew());
    }

}

