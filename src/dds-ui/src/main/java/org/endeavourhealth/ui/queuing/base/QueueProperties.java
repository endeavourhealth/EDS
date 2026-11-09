package org.endeavourhealth.ui.queuing.base;

import org.apache.commons.lang3.StringUtils;

public class QueueProperties {
    private String ipAddress;
    private String queueName;

    public QueueProperties(String ipAddress, String queueName) {
        setIpAddress(ipAddress);
        setQueueName(queueName);
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        if (StringUtils.isBlank(ipAddress))
            throw new IllegalArgumentException("IpAddress is null or blank");

        this.ipAddress = ipAddress;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        if (StringUtils.isBlank(queueName))
            throw new IllegalArgumentException("queueName is null or blank");

        this.queueName = queueName;
    }
}
